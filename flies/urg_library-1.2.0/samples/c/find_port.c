/*!
  \example find_port.c Finds out the port
  \author Satofumi KAMIMURA

  $Id: find_port.c,v c5747add6615 2015/05/07 03:18:34 alexandr $
*/

#include "urg_serial_utils.h"
#include <stdio.h>


int main(void)
{
    int found_port_size = urg_serial_find_port();
    int i;

    if (found_port_size == 0) {
        printf("could not found ports.\n");
        return 1;
    }

    for (i = 0; i < found_port_size; ++i) {
        printf("%s", urg_serial_port_name(i));
        if (urg_serial_is_urg_port(i)) {
          printf(" [URG]");
        }
        printf("\n");
    }

    return 0;
}
