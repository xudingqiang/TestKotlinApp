#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "NativeLog"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bella_testapp_JniTestActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ JNI!";
    LOGD("%s", hello.c_str());
    return env->NewStringUTF(hello.c_str());
}