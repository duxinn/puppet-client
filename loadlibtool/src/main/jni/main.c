#include <stdio.h>
#include <dlfcn.h>
#include <malloc.h>
#include <string.h>
#include <zconf.h>
#include "config.h"
#include "injector.h"
#include "ptrace.h"
#include "utils.h"

// 判断APP是否运行
int isAppRunning(char const *packageName) {
    pid_t pid = GetPid(packageName);
    if (pid > 0) return 1;
    return 0;
}

// 判断是否注入
int isInjected(char const *targetPackageName, char const *soPackageName) {
    pid_t pid = GetPid(targetPackageName);
    if (pid <= 0) return -1;
    char *file_name = (char *) calloc(50, sizeof(char));
    snprintf(file_name, 50, "/proc/%d/maps", pid);
    FILE *fp = fopen(file_name, "r");
    free(file_name);
    char line[512];
    int ret = 0;
    if (fp != NULL) {
        while (fgets(line, 512, fp) != NULL) {
            if (strstr(line, soPackageName) != NULL) {
                ret = 1;
                break;
            }
        }
        fclose(fp);
    }
    return ret;
}

// 注入
int inject(char const *targetPackageName,
        char const *libraryPath,
        char const *apkPath,
        char const *className,
        char const *methodName) {

    pid_t pid = GetPid(targetPackageName);
    long soHandle = InjectLibrary(pid, libraryPath);

    PtraceAttach(pid);

    long apkPathSpace = CallMmap(pid, strlen(apkPath) + 1);
    PtraceWrite(pid, (uint8_t *) apkPathSpace, (uint8_t *) apkPath, strlen(apkPath) + 1);

    long classNameSpace = CallMmap(pid, strlen(className) + 1);
    PtraceWrite(pid, (uint8_t *) classNameSpace, (uint8_t *) className, strlen(className) + 1);

    long methodNameSpace = CallMmap(pid, strlen(methodName) + 1);
    PtraceWrite(pid, (uint8_t *) methodNameSpace, (uint8_t *) methodName, strlen(methodName) + 1);

    long entry_addr = CallDlsym(pid, soHandle, "_Z5entryPcS_S_");

    if (DEBUG) {
        printf("entry_addr:%lx\n", entry_addr);
    }

    long params[3];
    params[0] = apkPathSpace;
    params[1] = classNameSpace;
    params[2] = methodNameSpace;
    long ret = CallRemoteFunction(pid, entry_addr, params, 3);
    if (DEBUG) {
        printf("ret:%lx\n", ret);
    }
    CallMunmap(pid, apkPathSpace, strlen(apkPath) + 1);
    CallMunmap(pid, classNameSpace, strlen(className) + 1);
    CallMunmap(pid, methodNameSpace, strlen(methodName) + 1);

    CallDlclose(pid, soHandle);
    PtraceDetach(pid);

    return 0;
}

int main(int argc, char const *argv[]) {
    if (argc < 2) {
        printf("lack parameters: %s\n", argv[0]);
        return -1;
    }
    if (IsSelinuxEnabled()) {
        DisableSelinux();
    }
    const char *method = argv[1];
    if (strcmp("isAppRunning", method) == 0) {
        if (argc < 3) {
            printf("lack parameters: %s\n", argv[0]);
            return -1;
        } else {
            return isAppRunning(argv[2]);
        }
    } else if (strcmp("isInjected", method) == 0) {
        if (argc < 4) {
            printf("lack parameters: %s\n", argv[0]);
            return -1;
        } else {
            return isInjected(argv[2], argv[3]);
        }
    } else if (strcmp("inject", method) == 0) {
        if (argc < 7) {
            printf("lack parameters: %s\n", argv[0]);
            return -1;
        } else {
            return inject(argv[2], argv[3], argv[4], argv[5], argv[6]);
        }
    }

    printf("unknown method: %s %s\n", argv[0], argv[1]);
    return -1;
}

