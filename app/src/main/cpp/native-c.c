//
// Created by xudq on 2026/3/11.
//
#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <dirent.h>


#define TAG "NativeLog"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

JNIEXPORT jstring JNICALL
Java_com_bella_testapp_JniTestActivity_stringFromJNIC(JNIEnv* env, jobject thiz) {
    const char* message = "Hello from C JNI!";
    LOGD("%s", message);
    return (*env)->NewStringUTF(env, message);
}


JNIEXPORT jobjectArray JNICALL
Java_com_bella_testapp_JniTestActivity_scanDirectory(JNIEnv* env, jobject thiz, jstring path_str) {

    // 1. 将 Java 字符串转换为 C 字符串
    const char* path = (*env)->GetStringUTFChars(env, path_str, NULL);
    if (path == NULL) {
        return NULL; // 内存分配失败
    }

    // 2. 打开目录
    DIR* dir = opendir(path);
    if (dir == NULL) {
        LOGD("Cannot open directory: %s", path);
        (*env)->ReleaseStringUTFChars(env, path_str, path);
        return NULL; // 目录不存在或无权限访问
    }

    // 3. 第一次遍历：统计目录下文件/文件夹的数量，用于初始化 Java 数组大小
    int count = 0;
    struct dirent* entry;
    while ((entry = readdir(dir)) != NULL) {
        // 过滤掉 "." 和 ".." 目录
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }
        count++;
    }

    // 重置目录流指针，准备第二次遍历
    rewinddir(dir);

    // 4. 找到 Java 的 String 类，并创建对应大小的 jobjectArray
    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray fileArray = (*env)->NewObjectArray(env, count, stringClass, NULL);

    // 5. 第二次遍历：填充文件名到 Java 数组中
    int index = 0;
    while ((entry = readdir(dir)) != NULL) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }

        // 将 C 字符串转为 Java String
        jstring jfilename = (*env)->NewStringUTF(env, entry->d_name);

        // 设置到数组对应的索引中
        (*env)->SetObjectArrayElement(env, fileArray, index, jfilename);

        // 释放局部引用，防止局部引用表溢出
        (*env)->DeleteLocalRef(env, jfilename);

        index++;
    }

    // 6. 释放资源
    closedir(dir);
    (*env)->ReleaseStringUTFChars(env, path_str, path);

    // 7. 返回结果数组
    return fileArray;
}


