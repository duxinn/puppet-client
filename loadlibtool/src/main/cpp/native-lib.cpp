#include <jni.h>
#include <string>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include "native-lib.h"
#include <dirent.h>
#include <dlfcn.h>
#include "fake_dlfcn.h"

JavaVM *gJavaVM = NULL;
JNIEnv *gJNIEnv = NULL;
jobject sClassLoader = NULL;


int find_pid_of(const char *process_name) {
    int id;
    pid_t pid = -1;
    DIR *dir;
    FILE *fp;
    char filename[32];
    char cmdline[256];

    struct dirent *entry;

    if (process_name == NULL)
        return -1;

    dir = opendir("/proc/");
    if (dir == NULL)
        return -1;

    while ((entry = readdir(dir)) != NULL) {
        id = atoi(entry->d_name);
        if (id != 0) {
            sprintf(filename, "/proc/%d/cmdline", id);
            fp = fopen(filename, "r");
            if (fp) {
                fgets(cmdline, sizeof(cmdline), fp);
                fclose(fp);

                if (strcmp(process_name, cmdline) == 0) {
                    /* process found */
                    pid = id;
                    break;
                }
            }
        }
    }

    closedir(dir);

    LOGD("target-pid %d", pid);

    return pid;
}


jint (*GetCreatedJavaVMs)(JavaVM **, jsize, jsize *)
=
NULL;

static void init_gvar() {
#ifdef __aarch64__
    #define ART_PATH "/system/lib64/libart.so"
#define DVM_PATH "/system/lib64/libdvm.so"
#else
#define ART_PATH "/system/lib/libart.so"
#define DVM_PATH "/system/lib/libdvm.so"
#endif

    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init");
    jsize size = 0;
    void *handle = NULL;
    if (access(DVM_PATH, F_OK) == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init_dvm");
        handle = fake_dlopen(DVM_PATH, RTLD_NOW | RTLD_GLOBAL);
    } else if (access(ART_PATH, F_OK) == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init_art");
        handle = fake_dlopen(ART_PATH, RTLD_NOW | RTLD_GLOBAL);
    }
    if (!handle) {
        return;
    }
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init_dlopen VM => %p", handle);
    GetCreatedJavaVMs = (jint(*)(JavaVM **, jsize, jsize *)) fake_dlsym(handle, "JNI_GetCreatedJavaVMs");
    if (!GetCreatedJavaVMs) {
        return;
    }
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init_dlsym GetCreatedJavaVMs => %p", GetCreatedJavaVMs);
    GetCreatedJavaVMs(&gJavaVM, 1, &size);
    if (size >= 1) {
        __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init_GetCreatedJavaVMs => %p", &gJavaVM);
        gJNIEnv = NULL;
        gJavaVM->GetEnv((void **) &gJNIEnv, JNI_VERSION_1_6);
        if (gJNIEnv) {
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "init_GetEnv => %p", &gJNIEnv);
            jclass threadClass = gJNIEnv->FindClass("java/lang/Thread");
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "threadClass => %p", &threadClass);
            jmethodID currentThreadMethod = gJNIEnv->GetStaticMethodID(threadClass, "currentThread", "()Ljava/lang/Thread;");
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "currentThreadMethod => %p", currentThreadMethod);
            jobject currentThread = gJNIEnv->CallStaticObjectMethod(threadClass, currentThreadMethod);
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "currentThread => %p", currentThread);
            jmethodID getContextClassLoaderMethod = gJNIEnv->GetMethodID(threadClass, "getContextClassLoader", "()Ljava/lang/ClassLoader;");
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "getContextClassLoaderMethod => %p", getContextClassLoaderMethod);
            sClassLoader = gJNIEnv->CallObjectMethod(currentThread, getContextClassLoaderMethod);
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "sClassLoader => %p", sClassLoader);
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "gJNIEnv null");
        }
    }
}



jobject getGlobalContext(JNIEnv *env) {
    //获取Activity Thread的实例对象
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);
    //获取Application，也就是全局的Context
    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "context %p", context);
    return context;
}



jstring getOptimizedDirFolderPath() {
    jmethodID getDir = gJNIEnv->GetMethodID(gJNIEnv->GetObjectClass(getGlobalContext(gJNIEnv)),"getDir","(Ljava/lang/String;I)Ljava/io/File;");
    jstring optimizedDirName = gJNIEnv->NewStringUTF("optimizedDirectory");
    jobject optimizedDirFolder = gJNIEnv->CallObjectMethod(getGlobalContext(gJNIEnv),getDir,optimizedDirName,0);
    jmethodID getAbsolutePath = gJNIEnv->GetMethodID(gJNIEnv->GetObjectClass(optimizedDirFolder),"getAbsolutePath","()Ljava/lang/String;");
    jstring optimizedDirFolderPath = (jstring) gJNIEnv->CallObjectMethod(optimizedDirFolder, getAbsolutePath);
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "getOptimizedDirFolderPath %p", optimizedDirFolderPath);
    return optimizedDirFolderPath;
}

jobject load_module(char *filepath) {
    if (sClassLoader) {
        jclass path_class_loader = gJNIEnv->FindClass("dalvik/system/DexClassLoader");
        jstring optimizedDirFolderPath = getOptimizedDirFolderPath();
        __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "filepath %s", filepath);
        if (path_class_loader) {
            jmethodID cort = gJNIEnv->GetMethodID(path_class_loader, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
            if (cort) {
                return gJNIEnv->NewObject(path_class_loader, cort, gJNIEnv->NewStringUTF(filepath),
                                          optimizedDirFolderPath,
                                          NULL,
                                          sClassLoader);
            }
        }
    }
    return NULL;
}

int entry(char *apkPath, char *className, char *methodName) {
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "entry_31");
    init_gvar();
    jobject loader = load_module(apkPath);
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "loader = %p", loader);
    jclass clazz = gJNIEnv->FindClass("java/lang/ClassLoader");
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "clazz = %p", clazz);
    jmethodID forclass = gJNIEnv->GetMethodID(clazz, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "loadClass = %p", forclass);
    if (!forclass) {
        return 0x100;
    }
    jstring classNameString = gJNIEnv->NewStringUTF(className);
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "hookStub");
    jclass cls = static_cast<jclass>(gJNIEnv->CallObjectMethod(loader, forclass, classNameString));
    jmethodID callback = gJNIEnv->GetStaticMethodID(cls, methodName, "()V");
    __android_log_print(ANDROID_LOG_DEBUG, "gbinjectc", "callback = %p", callback);
    gJNIEnv->CallStaticVoidMethod(cls, callback);
    return 0x100;
}

__attribute__((constructor)) static void _init() {
    LOGD("[_init] Bridge so has been loaded!!!!");
}