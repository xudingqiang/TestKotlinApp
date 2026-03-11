//
// Created by xudq on 2026/3/11.
//
#include <jni.h>
#include <string.h>
#include <android/log.h>

#define TAG "NativeLog"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

JNIEXPORT jstring JNICALL
Java_com_bella_testapp_JniTestActivity_stringFromJNIC(JNIEnv* env, jobject thiz) {
    const char* message = "Hello from C JNI!";
    LOGD("%s", message);
    return (*env)->NewStringUTF(env, message);
}