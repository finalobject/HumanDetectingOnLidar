#ifndef QRK_MATH_UTILITIES_H
#define QRK_MATH_UTILITIES_H

/*!
  \file
  \brief Auxiliary math functions
  \author Satofumi KAMIMURA

  $Id: math_utilities.h,v e5d1719877a2 2015/05/07 04:12:14 jun $
*/

#include "detect_os.h"
#if defined(WINDOWS_OS)
#define _USE_MATH_DEFINES
#endif
#include <math.h>


#ifndef M_PI
//! PI approximation (for Visual C++ 6.0)
#define M_PI 3.14159265358979323846
#endif

#if defined(MSC)
extern long lrint(double x);
#endif

#endif /* !QRK_MATH_UTILITIES_H */
