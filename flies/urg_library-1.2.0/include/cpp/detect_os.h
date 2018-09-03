#ifndef QRK_DETECT_OS_H
#define QRK_DETECT_OS_H

/*!
  \file
  \brief Detects the operating system
  \author Satofumi KAMIMURA

  $Id: detect_os.h,v c5747add6615 2015/05/07 03:18:34 alexandr $
*/

#if defined(_WIN32)
#define QRK_WINDOWS_OS

#if defined(_MSC_VER) || defined(__BORLANDC__)
#define QRK_MSC
#elif defined __CYGWIN__
#define QRK_CYGWIN
#elif defined __MINGW32__
#define QRK_MINGW
#endif

#elif defined __linux__
#define QRK_LINUX_OS

#else
// If cannot detect the OS, assumes it is a Mac
#define QRK_MAC_OS
#endif

#endif /* !QRK_DETECT_OS_H */
