/*!
  \file
  \brief Serial communications auxiliary functions
  \author Satofumi KAMIMURA

  $Id: urg_serial_utils.c,v c5747add6615 2015/05/07 03:18:34 alexandr $
*/

#include "urg_serial_utils.h"
#include "urg_detect_os.h"


#if defined(URG_WINDOWS_OS)
#include "urg_serial_utils_windows.c"
#else
#include "urg_serial_utils_linux.c"
#endif
