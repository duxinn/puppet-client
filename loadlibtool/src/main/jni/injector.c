#include <dlfcn.h>
#include <sys/mman.h>
#include <unistd.h>
#include <malloc.h>
#include <string.h>
#include <stdlib.h>
#include "config.h"
#include "injector.h"
#include "ptrace.h"
#include "utils.h"

long CallMmap(pid_t pid, size_t length) {
    long function_addr = GetRemoteFuctionAddr(pid, LIBC_PATH, ((long) (void *) mmap));
    long params[6];
    params[0] = 0;
    params[1] = length;
    params[2] = PROT_READ | PROT_WRITE | PROT_EXEC;
    params[3] = MAP_PRIVATE | MAP_ANONYMOUS;
    params[4] = 0;
    params[5] = 0;
    if (DEBUG) {
        printf("mmap called, function address %lx process %d size %zu\n", function_addr, pid,
               length);
    }
    return CallRemoteFunction(pid, function_addr, params, 6);
}

long CallMunmap(pid_t pid, long addr, size_t length) {
    long function_addr = GetRemoteFuctionAddr(pid, LIBC_PATH, ((long) (void *) munmap));
    long params[2];
    params[0] = addr;
    params[1] = length;
    if (DEBUG) {
        printf("munmap called, function address %lx process %d address %lx size %zu\n",
               function_addr, pid, addr, length);
    }
    return CallRemoteFunction(pid, function_addr, params, 2);
}

long CallDlopen(pid_t pid, const char *library_path) {
    long function_addr = GetRemoteFuctionAddr(pid, LINKER_PATH(), ((long) (void *) dlopen));
    if (DEBUG) {
        printf("dlopen_addr:%lx\n", function_addr);
    }
    long mmap_ret = CallMmap(pid, 0x400);
    PtraceWrite(pid, (uint8_t *) mmap_ret, (uint8_t *) library_path, strlen(library_path) + 1);
    long params[2];
    params[0] = mmap_ret;
    params[1] = RTLD_NOW | RTLD_LOCAL;
    if (DEBUG) {
        printf("dlopen called, function address %lx process %d library path %s\n", function_addr,
               pid, library_path);
    }
    long vndk_return_addr = GetModuleBaseAddr(pid, VNDK_LIB_PATH);
    long ret = CallRemoteFunctionFromNamespace(pid, function_addr, vndk_return_addr, params, 2);
    CallMunmap(pid, mmap_ret, 0x400);
    return ret;
}

long CallDlsym(pid_t pid, long so_handle, const char *symbol) {
    long function_addr = GetRemoteFuctionAddr(pid, LINKER_PATH(), ((long) (void *) dlsym));
    long mmap_ret = CallMmap(pid, 0x400);
    PtraceWrite(pid, (uint8_t *) mmap_ret, (uint8_t *) symbol, strlen(symbol) + 1);
    long params[2];
    params[0] = so_handle;
    params[1] = mmap_ret;
    if (DEBUG) {
        printf("dlsym called, function address %lx process %d so handle %lx symbol name %s\n",
               function_addr, pid, so_handle, symbol);
    }
    long ret = CallRemoteFunction(pid, function_addr, params, 2);
    CallMunmap(pid, mmap_ret, 0x400);
    return ret;
}

long CallDlclose(pid_t pid, long so_handle) {
    long function_addr = GetRemoteFuctionAddr(pid, LINKER_PATH(), ((long) (void *) dlclose));
    long params[1];
    params[0] = so_handle;
    if (DEBUG) {
        printf("dlclose called, function address %lx process %d so handle %lx\n", function_addr,
               pid, so_handle);
    }
    return CallRemoteFunction(pid, function_addr, params, 1);
}

long InjectLibrary(pid_t pid, const char *library_path) {
    if (DEBUG) {
        printf("Injection started...\n");
    }
    PtraceAttach(pid);
    long so_handle = CallDlopen(pid, library_path);
    if (DEBUG) {
        printf("so_handle:%lx\n", so_handle);
    }
    if (DEBUG) {
        if (!so_handle) {
            printf("Injection failed...\n");
        } else {
            printf("Injection ended succesfully...\n");
        }
    }

    PtraceDetach(pid);
    return so_handle;
}

const char *LINKER_PATH() {
    long local_base_addr = ((long) (void *) dlopen);
    if (DEBUG) {
        printf("local_base_addr :%lx\n", local_base_addr);
    }
    char *file_name = (char *) calloc(50, sizeof(char));
    snprintf(file_name, 50, "/proc/%d/maps", getpid());
    FILE *fp = fopen(file_name, "r");
    free(file_name);
    char line[512];
    if (fp != NULL) {
        while (fgets(line, 512, fp) != NULL) {
            char cpLine[512];
            strcpy(cpLine, line);
            char *last_line;
            char *addr = strtok_r(line, " ", &last_line);
            char *star_char = strtok(addr, "-");
            char *end_char = strtok(NULL, "-");
            long start_addr = strtoul(star_char, NULL, 16);
            long end_addr = strtoul(end_char, NULL, 16);
            if (local_base_addr >= start_addr && local_base_addr <= end_addr) {
                if (DEBUG) {
                    printf("real line :%s\n", cpLine);
                }
                if (strstr(cpLine, "/system/bin/linker") != NULL) {
                    return "/system/bin/linker";
                } else {
                    return "/system/lib/libdl.so";
                }
            }
        }
        fclose(fp);
    }
    return "";
}