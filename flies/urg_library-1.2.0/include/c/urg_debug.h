#ifndef URG_DEBUG_H
#define URG_DEBUG_H

/*!
  \file
  \brief URG debugging functions

  \author Satofumi KAMIMURA

  \attention It is not necessary to use these functions.

  $Id: urg_debug.h,v c5747add6615 2015/05/07 03:18:34 alexandr $
*/

#ifdef __cplusplus
extern "C" {
#endif

#include "urg_sensor.h"


    /*!
     \brief Directly send raw data to the sensor
    */
    extern int urg_raw_write(urg_t *urg, const char *data, int data_size);


    /*!
     \brief Directly get raw data from the sensor
    */
    extern int urg_raw_read(urg_t *urg, char *data, int max_data_size,
                            int timeout);

    /*!
     \brief Directly get raw data from the sensor until end-of-line
     */
    extern int urg_raw_readline(urg_t *urg,char *data, int max_data_size,
                                int timeout);

#ifdef __cplusplus
}
#endif

#endif /* !URG_DEBUG_H */
