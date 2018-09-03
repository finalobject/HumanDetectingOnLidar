/*!
  \file
  \brief
  \author Satofumi KAMIMURA

  $Id: urg_sensor.c,v cb2bcd29e090 2015/08/10 09:49:47 jun $

  \todo Run correctly when a Mx measurement command is in operation and a second Mx command is received
*/

#include "urg_sensor.h"
#include "urg_errno.h"
#include "urg_utils.h"
#include <stddef.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#if defined(URG_MSC)
#define snprintf _snprintf
#endif


enum {
    URG_FALSE = 0,
    URG_TRUE = 1,

    BUFFER_SIZE = 64 + 2 + 6,

    EXPECTED_END = -1,

    RECEIVE_DATA_TIMEOUT,
    RECEIVE_DATA_COMPLETE,      /*!<  Data was received correctly */

    PP_RESPONSE_LINES = 10,
    VV_RESPONSE_LINES = 7,
    II_RESPONSE_LINES = 9,

    MAX_TIMEOUT = 140,
};


static const char NOT_CONNECTED_MESSAGE[] = "not connected.";
static const char RECEIVE_ERROR_MESSAGE[] = "receive error.";


//!  Calculates the checksum value
static char scip_checksum(const char buffer[], int size)
{
    unsigned char sum = 0x00;
    int i;

    for (i = 0; i < size; ++i) {
        sum += buffer[i];
    }

    // Refer to the SCIP specification for details
    return (sum & 0x3f) + 0x30;
}


static int set_errno_and_return(urg_t *urg, int urg_errno)
{
    urg->last_errno = urg_errno;
    return urg_errno;
}


// Returns the number of lines received
static int scip_response(urg_t *urg, const char* command,
                         const int expected_ret[], int timeout,
                         char *receive_buffer, int receive_buffer_max_size)
{
    char *p = receive_buffer;
    char buffer[BUFFER_SIZE];
    int filled_size = 0;
    int line_number = 0;
    int ret = URG_UNKNOWN_ERROR;

    int write_size = (int)strlen(command);
    int n = connection_write(&urg->connection, command, write_size);

    if (n != write_size) {
        return set_errno_and_return(urg, URG_SEND_ERROR);
    }

    if (p) {
        *p = '\0';
    }

    do {
        n = connection_readline(&urg->connection, buffer, BUFFER_SIZE, timeout);
        if (n < 0) {
            return set_errno_and_return(urg, URG_NO_RESPONSE);

        } else if (p && (line_number > 0)
                   && (n < (receive_buffer_max_size - filled_size))) {
	    // Since echoback is verified upon completion, do not store it now
            memcpy(p, buffer, n);
            p += n;
            *p++ = '\0';
            filled_size += n;
        }

        if (line_number == 0) {
	    // Check if the echoback is complete
            if (strncmp(buffer, command, write_size - 1)) {
                return set_errno_and_return(urg, URG_INVALID_RESPONSE);
            }
        } else if (n > 0 && !(line_number == 1 && n == 1)) {
	    // Checks the checksum value for all lines except the echoback (in SCIP1.1 this is ignored)
            char checksum = buffer[n - 1];
            if ((checksum != scip_checksum(buffer, n - 1)) &&
                (checksum != scip_checksum(buffer, n - 2))) {
                return set_errno_and_return(urg, URG_CHECKSUM_ERROR);
            }
        }

        // Checks the status response to define the return value
        if (line_number == 1) {
            if (n == 1) {
	        // In case of SCIP1.1 it is always correct
                ret = 0;

            } else if (n != 3) {
                return set_errno_and_return(urg, URG_INVALID_RESPONSE);

            } else {
                int i;
                int actual_ret = strtol(buffer, NULL, 10);
                for (i = 0; expected_ret[i] != EXPECTED_END; ++i) {
                    if (expected_ret[i] == actual_ret) {
                        ret = 0;
                        break;
                    }
                }
            }
        }

        ++line_number;
    } while (n > 0);

    return (ret < 0) ? ret : (line_number - 1);
}

static void clear_urg_communication_buffer(urg_t *urg, int timeout)
{
    char buffer[BUFFER_SIZE];
    int n;

    connection_write(&urg->connection, "\n", 1);

    do {
        n = connection_readline(&urg->connection,
                                buffer, BUFFER_SIZE, timeout);
    } while (n >= 0);
}

static void ignore_receive_data(urg_t *urg, int timeout)
{
    char buffer[BUFFER_SIZE];
    int n;

    if (urg->is_sending == URG_FALSE) {
        return;
    }

    do {
        n = connection_readline(&urg->connection,
                                buffer, BUFFER_SIZE, timeout);
    } while (n >= 0);

    urg->is_sending = URG_FALSE;
}


static void ignore_receive_data_with_qt(urg_t *urg, int timeout)
{
    if ((urg->is_sending == URG_FALSE) && (urg->is_laser_on == URG_FALSE)) {
        return;
    }

    connection_write(&urg->connection, "QT\n", 3);
    urg->is_laser_on = URG_FALSE;
    ignore_receive_data(urg, timeout);
}


static int change_sensor_baudrate(urg_t *urg,
                                  long current_baudrate, long next_baudrate)
{
    enum { SS_COMMAND_SIZE = 10 };
    char buffer[SS_COMMAND_SIZE];
    int ss_expected[] = { 0, 3, 4, EXPECTED_END };
    int ret;

    if (current_baudrate == next_baudrate) {
        // If the current baudrate and next baudrate are the same, return
        return set_errno_and_return(urg, URG_NO_ERROR);
    }

    // Use "SS" command to change the baudrate
    snprintf(buffer, SS_COMMAND_SIZE, "SS%06ld\n", next_baudrate);
    ret = scip_response(urg, buffer, ss_expected, urg->timeout, NULL, 0);

    // 0F is a correct response for Ethernet sensors
    if (ret == -15) {
        return set_errno_and_return(urg, URG_NO_ERROR);
    }
    if (ret <= 0) {
        return set_errno_and_return(urg, URG_INVALID_PARAMETER);
    }

    // If the result is correct, change the host's baudrate
    ret = connection_set_baudrate(&urg->connection, next_baudrate);

    ignore_receive_data(urg, MAX_TIMEOUT);

    // Waits a bit for the sensor to change baudrate
    urg_delay(150);

    return set_errno_and_return(urg, ret);
}


// Sets the baudrate and connects to the sensor
static int connect_urg_device(urg_t *urg, long baudrate)
{
    long try_baudrate[] = { 19200, 57600, 115200, 250000, 500000, 750000 };
    int try_times = sizeof(try_baudrate) / sizeof(try_baudrate[0]);
    int i;

    // Fixes the baudrate list to have the given value first
    for (i = 0; i < try_times; ++i) {
        if (try_baudrate[i] == baudrate) {
            try_baudrate[i] = try_baudrate[0];
            try_baudrate[0] = baudrate;
            break;
        }
    }

    for (i = 0; i < try_times; ++i) {
        enum { RECEIVE_BUFFER_SIZE = 4 };
        int qt_expected[] = { 0, EXPECTED_END };
        char receive_buffer[RECEIVE_BUFFER_SIZE + 1];
        int ret;

        connection_set_baudrate(&urg->connection, try_baudrate[i]);

        // Clear URG read buffer to avoid having garbage data resulting from the incorrect baudrate communication
        clear_urg_communication_buffer(urg, MAX_TIMEOUT);

	// Sends the QT command and if the response is received then baudrate is correctly set
        ret = scip_response(urg, "QT\n", qt_expected, MAX_TIMEOUT,
                            receive_buffer, RECEIVE_BUFFER_SIZE);

        if (ret > 0) {
            if (!strcmp(receive_buffer, "E")) {
                int scip20_expected[] = { 0, EXPECTED_END };

		// Skips the end-of-line after the QT response
                ignore_receive_data(urg, MAX_TIMEOUT);

		// If "E" is received it is SCIP1.1, so change to SCIP2.0
                ret = scip_response(urg, "SCIP2.0\n", scip20_expected,
                                    MAX_TIMEOUT, NULL, 0);

		// Skips the end-of-line after SCIP2.0
                ignore_receive_data(urg, MAX_TIMEOUT);

		// Changes the baudrate and returns
                return change_sensor_baudrate(urg, try_baudrate[i], baudrate);

            } else if (!strcmp(receive_buffer, "0Ee")) {
                int tm2_expected[] = { 0, EXPECTED_END };

		// If "0Ee" is received it is in TM mode, send "TM2" to end it
                scip_response(urg, "TM2\n", tm2_expected,
                              MAX_TIMEOUT, NULL, 0);

		// Changes the baudrate and returns
                return change_sensor_baudrate(urg, try_baudrate[i], baudrate);
            }
        }

        if (ret <= 0) {
            if (ret == URG_INVALID_RESPONSE) {
	        // If an invalid echoback is received, it is currently in measurement data transmission
	        // so skip the data
                ignore_receive_data_with_qt(urg, MAX_TIMEOUT);

		// Changes the baudrate and returns
                return change_sensor_baudrate(urg, try_baudrate[i], baudrate);

            } else {
	        // If there is no response, changes the baudrate and re-connects
                ignore_receive_data_with_qt(urg, MAX_TIMEOUT);
                continue;
            }
        } else if (!strcmp("00P", receive_buffer)) {

	    // Changes the baudrate and returns
            return change_sensor_baudrate(urg, try_baudrate[i], baudrate);
        }
    }

    return set_errno_and_return(urg, URG_NOT_DETECT_BAUDRATE_ERROR);
}


// Stores the PP command response into urg_t
static int receive_parameter(urg_t *urg)
{
    enum { RECEIVE_BUFFER_SIZE = BUFFER_SIZE * 9, };
    char receive_buffer[RECEIVE_BUFFER_SIZE];
    int pp_expected[] = { 0, EXPECTED_END };
    unsigned short received_bits = 0x0000;
    char *p;
    int i;

    int ret = scip_response(urg, "PP\n", pp_expected, MAX_TIMEOUT,
                            receive_buffer, RECEIVE_BUFFER_SIZE);
    if (ret < 0) {
        return ret;
    } else if (ret < PP_RESPONSE_LINES) {
        ignore_receive_data_with_qt(urg, MAX_TIMEOUT);
        return set_errno_and_return(urg, URG_INVALID_RESPONSE);
    }

    p = receive_buffer;
    for (i = 0; i < (ret - 1); ++i) {

        if (!strncmp(p, "DMIN:", 5)) {
            urg->min_distance = strtol(p + 5, NULL, 10);
            received_bits |= 0x0001;

        } else if (!strncmp(p, "DMAX:", 5)) {
            urg->max_distance = strtol(p + 5, NULL, 10);
            received_bits |= 0x0002;

        } else if (!strncmp(p, "ARES:", 5)) {
            urg->area_resolution = strtol(p + 5, NULL, 10);
            received_bits |= 0x0004;

        } else if (!strncmp(p, "AMIN:", 5)) {
            urg->first_data_index = strtol(p + 5, NULL, 10);
            received_bits |= 0x0008;

        } else if (!strncmp(p, "AMAX:", 5)) {
            urg->last_data_index = strtol(p + 5, NULL, 10);
            received_bits |= 0x0010;

        } else if (!strncmp(p, "AFRT:", 5)) {
            urg->front_data_index = strtol(p + 5, NULL, 10);
            received_bits |= 0x0020;

        } else if (!strncmp(p, "SCAN:", 5)) {
            int rpm = strtol(p + 5, NULL, 10);
	    // Timeout is set about 16 times the sensor measurement period
            urg->scan_usec = 1000 * 1000 * 60 / rpm;
            urg->timeout = urg->scan_usec >> (10 - 4);
            received_bits |= 0x0040;
        }
        p += strlen(p) + 1;
    }

    // Check if all the parameters were received
    if (received_bits != 0x007f) {
        return set_errno_and_return(urg, URG_RECEIVE_ERROR);
    }

    urg_set_scanning_parameter(urg,
                               urg->first_data_index - urg->front_data_index,
                               urg->last_data_index - urg->front_data_index,
                               1);

    return set_errno_and_return(urg, URG_NO_ERROR);
}


//!   Decodes the SCIP message
long urg_scip_decode(const char data[], int size)
{
    const char* p = data;
    const char* last_p = p + size;
    int value = 0;

    while (p < last_p) {
        value <<= 6;
        value &= ~0x3f;
        value |= *p++ - 0x30;
    }
    return value;
}


static int parse_parameter(const char *parameter, int size)
{
    char buffer[5];

    memcpy(buffer, parameter, size);
    buffer[size] = '\0';

    return strtol(buffer, NULL, 10);
}


static urg_measurement_type_t parse_distance_parameter(urg_t *urg,
                                                       const char echoback[])
{
    urg_measurement_type_t ret_type = URG_UNKNOWN;

    urg->received_range_data_byte = URG_COMMUNICATION_3_BYTE;
    if (echoback[1] == 'S') {
        urg->received_range_data_byte = URG_COMMUNICATION_2_BYTE;
        ret_type = URG_DISTANCE;

    } else if (echoback[1] == 'D') {
        if ((echoback[0] == 'G') || (echoback[0] == 'M')) {
            ret_type = URG_DISTANCE;
        } else if ((echoback[0] == 'H') || (echoback[0] == 'N')) {
            ret_type = URG_MULTIECHO;
        }
    } else if (echoback[1] == 'E') {
        if ((echoback[0] == 'G') || (echoback[0] == 'M')) {
            ret_type = URG_DISTANCE_INTENSITY;
        } else if ((echoback[0] == 'H') || (echoback[0] == 'N')) {
            ret_type = URG_MULTIECHO_INTENSITY;
        }
    } else {
        return URG_UNKNOWN;
    }

    // Stores the parameters
    urg->received_first_index = parse_parameter(&echoback[2], 4);
    urg->received_last_index = parse_parameter(&echoback[6], 4);
    urg->received_skip_step = parse_parameter(&echoback[10], 2);

    return ret_type;
}


static urg_measurement_type_t parse_distance_echoback(urg_t *urg,
                                                      const char echoback[])
{
    size_t line_length;
    urg_measurement_type_t ret_type = URG_UNKNOWN;

    if (!strcmp("QT", echoback)) {
        return URG_STOP;
    }

    line_length = strlen(echoback);
    if ((line_length == 12) &&
        ((echoback[0] == 'G') || (echoback[0] == 'H'))) {
        ret_type = parse_distance_parameter(urg, echoback);

    } else if ((line_length == 15) &&
               ((echoback[0] == 'M') || (echoback[0] == 'N'))) {
        ret_type = parse_distance_parameter(urg, echoback);
    }
    return ret_type;
}


static int receive_length_data(urg_t *urg, long length[],
                               unsigned short intensity[],
                               urg_measurement_type_t type, char buffer[])
{
    int n;
    int step_filled = 0;
    int line_filled = 0;
    int multiecho_index = 0;

    int each_size =
        (urg->received_range_data_byte == URG_COMMUNICATION_2_BYTE) ? 2 : 3;
    int data_size = each_size;
    int is_intensity = URG_FALSE;
    int is_multiecho = URG_FALSE;
    int multiecho_max_size = 1;

    if ((type == URG_DISTANCE_INTENSITY) || (type == URG_MULTIECHO_INTENSITY)) {
        data_size *= 2;
        is_intensity = URG_TRUE;
    }
    if ((type == URG_MULTIECHO) || (type == URG_MULTIECHO_INTENSITY)) {
        is_multiecho = URG_TRUE;
        multiecho_max_size = URG_MAX_ECHO;
    }

    do {
        char *p = buffer;
        char *last_p;

        n = connection_readline(&urg->connection,
                                &buffer[line_filled], BUFFER_SIZE - line_filled,
                                urg->timeout);

        if (n > 0) {
	    // Validates the checksum
            if (buffer[line_filled + n - 1] !=
                scip_checksum(&buffer[line_filled], n - 1)) {
                ignore_receive_data_with_qt(urg, urg->timeout);
                return set_errno_and_return(urg, URG_CHECKSUM_ERROR);
            }
        }

        if (n > 0) {
            line_filled += n - 1;
        }
        last_p = p + line_filled;

        while ((last_p - p) >= data_size) {
            int index;

            if (*p == '&') {
                // If the start character is a '&' then assume data is multiecho
                if ((last_p - (p + 1)) < data_size) {
		    // Skips the '&' and if the string size is less than data_size ignore it
                    break;
                }

                --step_filled;
                ++multiecho_index;
                ++p;
                --line_filled;

            } else {
	        // Next data
                multiecho_index = 0;
            }

            index = (step_filled * multiecho_max_size) + multiecho_index;

            if (step_filled >
                (urg->received_last_index - urg->received_first_index)) {
	        // If there is extra data, ignore it
                ignore_receive_data_with_qt(urg, urg->timeout);
                return set_errno_and_return(urg, URG_RECEIVE_ERROR);
            }


            if (is_multiecho && (multiecho_index == 0)) {
	        // Stores dummy values in the multiecho data location
                int i;
                if (length) {
                    for (i = 1; i < multiecho_max_size; ++i) {
                        length[index + i] = 0;
                    }
                }
                if (intensity) {
                    for (i = 1; i < multiecho_max_size; ++i) {
                        intensity[index + i] = 0;
                    }
                }
            }

            // Stores the distance data
            if (length) {
                length[index] = urg_scip_decode(p, 3);
            }
            p += 3;

	    // Stores the intensity data
            if (is_intensity) {
                if (intensity) {
                    intensity[index] = (unsigned short)urg_scip_decode(p, 3);
                }
                p += 3;
            }

            ++step_filled;
            line_filled -= data_size;
        }

        // Prepares the next line to process
        memmove(buffer, p, line_filled);
    } while (n > 0);

    return step_filled;
}


//!  Gets measurement data
static int receive_data(urg_t *urg, long data[], unsigned short intensity[],
                        long *time_stamp)
{
    urg_measurement_type_t type;
    char buffer[BUFFER_SIZE];
    int ret = 0;
    int n;
    int extended_timeout = urg->timeout
        + 2 * (urg->scan_usec * (urg->scanning_skip_scan) / 1000);

    // Gets the echoback
    n = connection_readline(&urg->connection,
                            buffer, BUFFER_SIZE, extended_timeout);
    if (n <= 0) {
        return set_errno_and_return(urg, URG_NO_RESPONSE);
    }
    // Checks the echoback
    type = parse_distance_echoback(urg, buffer);

    // Gets the response message
    n = connection_readline(&urg->connection,
                            buffer, BUFFER_SIZE, urg->timeout);
    if (n != 3) {
        ignore_receive_data_with_qt(urg, urg->timeout);
        return set_errno_and_return(urg, URG_INVALID_RESPONSE);
    }

    if (buffer[n - 1] != scip_checksum(buffer, n - 1)) {
        // Validates the checksum
        ignore_receive_data_with_qt(urg, urg->timeout);
        return set_errno_and_return(urg, URG_CHECKSUM_ERROR);
    }

    if (type == URG_STOP) {
        // If received QT response, ignore the last end-of-line and return as successful
        n = connection_readline(&urg->connection,
                                buffer, BUFFER_SIZE, urg->timeout);
        if (n == 0) {
            return 0;
        } else {
            return set_errno_and_return(urg, URG_INVALID_RESPONSE);
        }
    }

    if (urg->specified_scan_times != 1) {
        if (!strncmp(buffer, "00", 2)) {
	    // If received "00" response, assumes it is the echoback
	    // Skips up to the next empty line and returns the next data
            n = connection_readline(&urg->connection,
                                    buffer, BUFFER_SIZE, urg->timeout);

            if (n != 0) {
                ignore_receive_data_with_qt(urg, urg->timeout);
                return set_errno_and_return(urg, URG_INVALID_RESPONSE);
            } else {
                return receive_data(urg, data, intensity, time_stamp);
            }
        }
    }

    if (((urg->specified_scan_times == 1) && (strncmp(buffer, "00", 2))) ||
        ((urg->specified_scan_times != 1) && (strncmp(buffer, "99", 2)))) {
        if (urg->error_handler) {
            type = urg->error_handler(buffer, urg);
        }

        //if (type == URG_UNKNOWN) {
            // If received "00P" response for Gx/Hx
            // If received "99b" response for Mx/Nx
            ignore_receive_data_with_qt(urg, urg->timeout);
            return set_errno_and_return(urg, URG_INVALID_RESPONSE);
        //}
    }

    // Gets the timestamp
    n = connection_readline(&urg->connection,
                            buffer, BUFFER_SIZE, urg->timeout);
    if (n > 0) {
        if (time_stamp) {
            *time_stamp = urg_scip_decode(buffer, 4);
        }
    }

    // Gets the measurement data
    switch (type) {
    case URG_DISTANCE:
    case URG_MULTIECHO:
        ret = receive_length_data(urg, data, NULL, type, buffer);
        break;

    case URG_DISTANCE_INTENSITY:
    case URG_MULTIECHO_INTENSITY:
        ret = receive_length_data(urg, data, intensity, type, buffer);
        break;

    case URG_STOP:
    case URG_UNKNOWN:
        ret = 0;
        break;
    }

    // If specified_scan_times == 1 then we are using a Gx type command
    // it is not necessary to explicity stop measurement
    if ((urg->specified_scan_times > 1) && (urg->scanning_remain_times > 0)) {
        if (--urg->scanning_remain_times <= 0) {
	    // Stops measurement
            urg_stop_measurement(urg);
        }
    }
    return ret;
}


int urg_open(urg_t *urg, urg_connection_type_t connection_type,
             const char *device_or_address, long baudrate_or_port)
{
    int ret;
    long baudrate = baudrate_or_port;

    urg->is_active = URG_FALSE;
    urg->is_sending = URG_TRUE;
    urg->last_errno = URG_NOT_CONNECTED;
    urg->timeout = MAX_TIMEOUT;
    urg->scanning_skip_scan = 0;
    urg->error_handler = NULL;

    // Connects to the device
    ret = connection_open(&urg->connection, connection_type,
                          device_or_address, baudrate_or_port);

    if (ret < 0) {
        switch (connection_type) {
        case URG_SERIAL:
            urg->last_errno = URG_SERIAL_OPEN_ERROR;
            break;

        case URG_ETHERNET:
            urg->last_errno = URG_ETHERNET_OPEN_ERROR;
            break;

        default:
            urg->last_errno = URG_INVALID_RESPONSE;
            break;
        }
        return urg->last_errno;
    }

    // Make adjustments so to connect with URG using the specified baudrate
    if (connection_type == URG_ETHERNET) {
        // In case of Ethernet, sets a fake baudrate
        baudrate = 115200;
    }

    ret = connect_urg_device(urg, baudrate);
    if (ret != URG_NO_ERROR) {
        return set_errno_and_return(urg, ret);
    }
    urg->is_sending = URG_FALSE;

    // Initializes variables
    urg->last_errno = URG_NO_ERROR;
    urg->range_data_byte = URG_COMMUNICATION_3_BYTE;
    urg->specified_scan_times = 0;
    urg->scanning_remain_times = 0;
    urg->is_laser_on = URG_FALSE;

    // Gets the sensor parameters
    ret = receive_parameter(urg);
    if (ret == URG_NO_ERROR) {
        urg->is_active = URG_TRUE;
    }
    return ret;
}


void urg_close(urg_t *urg)
{
    if (urg->is_active) {
        ignore_receive_data_with_qt(urg, urg->timeout);
    }
    connection_close(&urg->connection);
    urg->is_active = URG_FALSE;
}


void urg_set_timeout_msec(urg_t *urg, int msec)
{
    urg->timeout = msec;
}


int urg_start_time_stamp_mode(urg_t *urg)
{
    const int expected[] = { 0, EXPECTED_END };
    int n;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    // Sends TM0 command
    n = scip_response(urg, "TM0\n", expected, urg->timeout, NULL, 0);
    if (n <= 0) {
        return set_errno_and_return(urg, URG_INVALID_RESPONSE);
    } else {
        return 0;
    }
}


long urg_time_stamp(urg_t *urg)
{
    const int expected[] = { 0, EXPECTED_END };
    char buffer[BUFFER_SIZE];
    char *p;
    int ret;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    ret = scip_response(urg, "TM1\n", expected,
                        urg->timeout, buffer, BUFFER_SIZE);
    if (ret < 0) {
        return ret;
    }

    // Gets and decodes the timestamp stored in buffer
    if (strcmp(buffer, "00P")) {
        // If first status code is not "00P" return
        return set_errno_and_return(urg, URG_RECEIVE_ERROR);
    }
    p = buffer + 4;
    if (strlen(p) != 5) {
        return set_errno_and_return(urg, URG_RECEIVE_ERROR);
    }
    if (p[5] == scip_checksum(p, 4)) {
        return set_errno_and_return(urg, URG_CHECKSUM_ERROR);
    }
    return urg_scip_decode(p, 4);
}


int urg_stop_time_stamp_mode(urg_t *urg)
{
    int expected[] = { 0, EXPECTED_END };
    int n;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    // Sends the TM2 command
    n = scip_response(urg, "TM2\n", expected, urg->timeout, NULL, 0);
    if (n <= 0) {
        return set_errno_and_return(urg, URG_INVALID_RESPONSE);
    } else {
        return 0;
    }
}


static int send_distance_command(urg_t *urg, int scan_times, int skip_scan,
                                 char single_scan_ch, char continuous_scan_ch,
                                 char scan_type_ch)
{
    char buffer[BUFFER_SIZE];
    int write_size = 0;
    int front_index = urg->front_data_index;
    int n;

    urg->specified_scan_times = (scan_times < 0) ? 0 : scan_times;
    urg->scanning_remain_times = urg->specified_scan_times;
    urg->scanning_skip_scan = (skip_scan < 0) ? 0 : skip_scan;
    if (scan_times >= 100) {
        // If the number of scans is over 99, work in infinite scanning mode
        urg->specified_scan_times = 0;
    }

    if (urg->scanning_remain_times == 1) {
        // Prepares the measurement command
        urg_laser_on(urg);

        write_size = snprintf(buffer, BUFFER_SIZE, "%c%c%04d%04d%02d\n",
                              single_scan_ch, scan_type_ch,
                              urg->scanning_first_step + front_index,
                              urg->scanning_last_step + front_index,
                              urg->scanning_skip_step);
    } else {
        write_size = snprintf(buffer, BUFFER_SIZE, "%c%c%04d%04d%02d%01d%02d\n",
                              continuous_scan_ch, scan_type_ch,
                              urg->scanning_first_step + front_index,
                              urg->scanning_last_step + front_index,
                              urg->scanning_skip_step,
                              skip_scan, urg->specified_scan_times);
        urg->is_sending = URG_TRUE;
    }

    n = connection_write(&urg->connection, buffer, write_size);
    if (n != write_size) {
        return set_errno_and_return(urg, URG_SEND_ERROR);
    }

    return 0;
}


int urg_start_measurement(urg_t *urg, urg_measurement_type_t type,
                          int scan_times, int skip_scan)
{
    char range_byte_ch;
    int ret = 0;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    if ((skip_scan < 0) || (skip_scan > 9)) {
        ignore_receive_data_with_qt(urg, urg->timeout);
        return set_errno_and_return(urg, URG_INVALID_PARAMETER);
    }

    //   !!! If a Mx/Nx measurement command is already in operation,
    //   !!! measurement can be re-started after having send QT command.
    //   !!! However, if Mx/Nx is already running and the same command is issued again
    //   !!! (ex., send MD command while another MD is running), the command is overwritten

    // Prepares and sends the measurement command according to the given type
    switch (type) {
    case URG_DISTANCE:
        range_byte_ch =
            (urg->range_data_byte == URG_COMMUNICATION_2_BYTE) ? 'S' : 'D';
        ret = send_distance_command(urg, scan_times, skip_scan,
                                    'G', 'M', range_byte_ch);
        break;

    case URG_DISTANCE_INTENSITY:
        ret = send_distance_command(urg, scan_times, skip_scan,
                                    'G', 'M', 'E');
        break;

    case URG_MULTIECHO:
        ret = send_distance_command(urg, scan_times, skip_scan,
                                    'H', 'N', 'D');
        break;

    case URG_MULTIECHO_INTENSITY:
        ret = send_distance_command(urg, scan_times, skip_scan,
                                    'H', 'N', 'E');
        break;

    case URG_STOP:
    case URG_UNKNOWN:
    default:
        ignore_receive_data_with_qt(urg, urg->timeout);
        urg->last_errno = URG_INVALID_PARAMETER;
        ret = urg->last_errno;
        break;
    }

    return ret;
}


int urg_get_distance(urg_t *urg, long data[], long *time_stamp)
{
    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }
    return receive_data(urg, data, NULL, time_stamp);
}


int urg_get_distance_intensity(urg_t *urg,
                               long data[], unsigned short intensity[],
                               long *time_stamp)
{
    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    return receive_data(urg, data, intensity, time_stamp);
}


int urg_get_multiecho(urg_t *urg, long data_multi[], long *time_stamp)
{
    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    return receive_data(urg, data_multi, NULL, time_stamp);
}


int urg_get_multiecho_intensity(urg_t *urg,
                                long data_multi[],
                                unsigned short intensity_multi[],
                                long *time_stamp)
{
    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    return receive_data(urg, data_multi, intensity_multi, time_stamp);
}


int urg_stop_measurement(urg_t *urg)
{
    enum { MAX_READ_TIMES = 3 };
    int ret = URG_INVALID_RESPONSE;
    int n;
    int i;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    // Sends the QT command
    n = connection_write(&urg->connection, "QT\n", 3);
    if (n != 3) {
        return set_errno_and_return(urg, URG_SEND_ERROR);
    }

    for (i = 0; i < MAX_READ_TIMES; ++i) {
        // Skips measuement data until QT response is received
        ret = receive_data(urg, NULL, NULL, NULL);
        if (ret == URG_NO_ERROR) {
	    // Correct response
            urg->is_laser_on = URG_FALSE;
            urg->is_sending = URG_FALSE;
            return set_errno_and_return(urg, URG_NO_ERROR);
        }
    }
    return ret;
}


int urg_set_scanning_parameter(urg_t *urg, int first_step, int last_step,
                               int skip_step)
{
    // If parameters are out of range, return an error
    if (((skip_step < 0) || (skip_step >= 100)) ||
        (first_step > last_step) ||
        (first_step < -urg->front_data_index) ||
        (last_step > (urg->last_data_index - urg->front_data_index))) {
        return set_errno_and_return(urg, URG_SCANNING_PARAMETER_ERROR);
    }

    urg->scanning_first_step = first_step;
    urg->scanning_last_step = last_step;
    urg->scanning_skip_step = skip_step;

    return set_errno_and_return(urg, URG_NO_ERROR);
}


int urg_set_connection_data_size(urg_t *urg,
                                 urg_range_data_byte_t data_byte)
{
    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    if ((data_byte != URG_COMMUNICATION_3_BYTE) ||
        (data_byte != URG_COMMUNICATION_2_BYTE)) {
        return set_errno_and_return(urg, URG_DATA_SIZE_PARAMETER_ERROR);
    }

    urg->range_data_byte = data_byte;

    return set_errno_and_return(urg, URG_NO_ERROR);
}


int urg_laser_on(urg_t *urg)
{
    int expected[] = { 0, 2, EXPECTED_END };
    int ret;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    if (urg->is_laser_on != URG_FALSE) {
        // If laser is already on don't send the command
        urg->last_errno = 0;
        return urg->last_errno;
    }

    ret = scip_response(urg, "BM\n", expected, urg->timeout, NULL, 0);
    if (ret >= 0) {
        urg->is_laser_on = URG_TRUE;
        ret = 0;
    }
    return ret;
}


int urg_laser_off(urg_t *urg)
{
    return urg_stop_measurement(urg);
}


int urg_reboot(urg_t *urg)
{
    int expected[] = { 0, 1, EXPECTED_END };
    int ret;
    int i;

    if (!urg->is_active) {
        return set_errno_and_return(urg, URG_NOT_CONNECTED);
    }

    // After sending the 2nd RB then close the connection
    for (i = 0; i < 2; ++i) {
        ret = scip_response(urg, "RB\n", expected, urg->timeout, NULL, 0);
        if (ret < 0) {
            return set_errno_and_return(urg, URG_INVALID_RESPONSE);
        }
    }
    urg->is_active = URG_FALSE;
    urg_close(urg);

    urg->last_errno = 0;
    return urg->last_errno;
}


void urg_sleep(urg_t *urg)
{
    enum { RECEIVE_BUFFER_SIZE = 4 };
    int sl_expected[] = { 0, EXPECTED_END };
    char receive_buffer[RECEIVE_BUFFER_SIZE];

    if (urg_stop_measurement(urg) != URG_NO_ERROR) {
        return;
    }

    scip_response(urg, "%SL\n", sl_expected, MAX_TIMEOUT,
                  receive_buffer, RECEIVE_BUFFER_SIZE);
}


void urg_wakeup(urg_t *urg)
{
    urg_stop_measurement(urg);
}


int urg_is_stable(urg_t *urg)
{
    const char *stat = urg_sensor_status(urg);
    return strncmp("Stable", stat, 6) ? 0 : 1;
}


static char *copy_token(char *dest, char *receive_buffer,
                        const char *start_str, const char *end_ch, int lines)
{
    size_t start_str_len = strlen(start_str);
    size_t end_ch_len = strlen(end_ch);
    int i;
    size_t j;

    for (j = 0; j < end_ch_len; ++j) {
        const char *p = receive_buffer;

        for (i = 0; i < lines; ++i) {
            if (!strncmp(p, start_str, start_str_len)) {

                char *last_p = strchr(p + start_str_len, end_ch[j]);
                if (last_p) {
                    *last_p = '\0';
                    memcpy(dest, p + start_str_len,
                           last_p - (p + start_str_len) + 1);
                    return dest;
                }
            }
            p += strlen(p) + 1;
        }
    }
    return NULL;
}


static const char *receive_command_response(urg_t *urg,
                                            char *buffer, int buffer_size,
                                            const char* command,
                                            int response_lines)
{
    const int vv_expected[] = { 0, EXPECTED_END };
    int ret;

    if (!urg->is_active) {
        return NOT_CONNECTED_MESSAGE;
    }

    ret = scip_response(urg, command, vv_expected, urg->timeout,
                        buffer, buffer_size);
    if (ret < response_lines) {
        return RECEIVE_ERROR_MESSAGE;
    }

    return NULL;
}


const char *urg_sensor_product_type(urg_t *urg)
{
    enum {
        RECEIVE_BUFFER_SIZE = BUFFER_SIZE * VV_RESPONSE_LINES,
    };
    char receive_buffer[RECEIVE_BUFFER_SIZE];
    const char *ret;
    char *p;

    ret = receive_command_response(urg, receive_buffer, RECEIVE_BUFFER_SIZE,
                                   "VV\n", VV_RESPONSE_LINES);
    if (ret) {
        return ret;
    }

    p = copy_token(urg->return_buffer,
                   receive_buffer, "PROD:", ";", VV_RESPONSE_LINES);
    return (p) ? p : RECEIVE_ERROR_MESSAGE;
}


const char *urg_sensor_serial_id(urg_t *urg)
{
    enum {
        RECEIVE_BUFFER_SIZE = BUFFER_SIZE * VV_RESPONSE_LINES,
    };
    char receive_buffer[RECEIVE_BUFFER_SIZE];
    const char *ret;
    char *p;

    ret = receive_command_response(urg, receive_buffer, RECEIVE_BUFFER_SIZE,
                                   "VV\n", VV_RESPONSE_LINES);
    if (ret) {
        return ret;
    }

    p = copy_token(urg->return_buffer,
                   receive_buffer, "SERI:", ";", VV_RESPONSE_LINES);
    return (p) ? p : RECEIVE_ERROR_MESSAGE;
}


const char *urg_sensor_firmware_version(urg_t *urg)
{
    enum {
        RECEIVE_BUFFER_SIZE = BUFFER_SIZE * VV_RESPONSE_LINES,
    };
    char receive_buffer[RECEIVE_BUFFER_SIZE];
    const char *ret;
    char *p;

    if (!urg->is_active) {
        return NOT_CONNECTED_MESSAGE;
    }

    ret = receive_command_response(urg, receive_buffer, RECEIVE_BUFFER_SIZE,
                                   "VV\n", VV_RESPONSE_LINES);
    if (ret) {
        return ret;
    }

    p = copy_token(urg->return_buffer,
                   receive_buffer, "FIRM:", " (", VV_RESPONSE_LINES);
    return (p) ? p : RECEIVE_ERROR_MESSAGE;
}


const char *urg_sensor_status(urg_t *urg)
{
    enum {
        RECEIVE_BUFFER_SIZE = BUFFER_SIZE * II_RESPONSE_LINES,
    };
    char receive_buffer[RECEIVE_BUFFER_SIZE];
    const char *ret;
    char *p;

    if (!urg->is_active) {
        return NOT_CONNECTED_MESSAGE;
    }

    ret = receive_command_response(urg, receive_buffer, RECEIVE_BUFFER_SIZE,
                                   "II\n", II_RESPONSE_LINES);
    if (ret) {
        return ret;
    }

    p = copy_token(urg->return_buffer,
                   receive_buffer, "STAT:", ";", II_RESPONSE_LINES);
    return (p) ? p : RECEIVE_ERROR_MESSAGE;
}


const char *urg_sensor_state(urg_t *urg)
{
    enum {
        RECEIVE_BUFFER_SIZE = BUFFER_SIZE * II_RESPONSE_LINES,
    };
    char receive_buffer[RECEIVE_BUFFER_SIZE];
    const char *ret;
    char *p;

    if (!urg->is_active) {
        return NOT_CONNECTED_MESSAGE;
    }

    ret = receive_command_response(urg, receive_buffer, RECEIVE_BUFFER_SIZE,
                                   "II\n", II_RESPONSE_LINES);
    if (ret) {
        return ret;
    }

    p = copy_token(urg->return_buffer,
                   receive_buffer, "MESM:", " (", II_RESPONSE_LINES);
    return (p) ? p : RECEIVE_ERROR_MESSAGE;
}


void urg_set_error_handler(urg_t *urg, urg_error_handler handler)
{
    urg->error_handler = handler;
}
