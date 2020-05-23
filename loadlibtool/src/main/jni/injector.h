#include <stdio.h>

#ifndef INJECTOR_H_
#define INJECTOR_H_

#if defined(__aarch64__)
#define LIBC_PATH      "/system/lib64/libc.so"
#define VNDK_LIB_PATH  "/system/lib64/libRS.so"
#else
#define LIBC_PATH      "/system/lib/libc.so"
#define VNDK_LIB_PATH  "/system/lib/libRS.so"
#endif

long CallMmap(pid_t pid, size_t length);
long CallDlopen(pid_t pid, const char* library_path);
long CallDlsym(pid_t pid, long so_handle, const char* symbol);
long CallMunmap(pid_t pid, long addr, size_t length);
long CallDlclose(pid_t pid, long so_handle);
long InjectLibrary(pid_t pid, const char* library_path);
const char *LINKER_PATH();
#endif
