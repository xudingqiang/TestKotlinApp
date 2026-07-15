#include <jni.h>
#include <string>
#include <android/log.h>


#include <dirent.h>
#include <vector>
#define TAG "NativeLog"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOG_TAG "NativeFind"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bella_testapp_JniTestActivity_stringFromJNICPP(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ JNI!";
    LOGD("%s", hello.c_str());
    return env->NewStringUTF(hello.c_str());
}



extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_bella_testapp_JniTestActivity_scanDirectoryCpp(
        JNIEnv *env,
        jobject thiz,
        jstring path_str) {

    if (path_str == nullptr) {
        return nullptr;
    }

    const char *path =
            env->GetStringUTFChars(path_str, nullptr);

    if (path == nullptr) {
        return nullptr;
    }

    DIR *dir = opendir(path);

    if (dir == nullptr) {
        LOGD("Cannot open directory : %s", path);

        env->ReleaseStringUTFChars(
                path_str,
                path);

        return nullptr;
    }

    //--------------------------------------------------
    // 单次遍历目录
    //--------------------------------------------------

    std::vector<std::string> files;

    files.reserve(4096);

    struct dirent *entry;

    while ((entry = readdir(dir)) != nullptr) {

        const char *name = entry->d_name;

        // 跳过 "." ".."
        if (name[0] == '.') {

            if (name[1] == '\0') {
                continue;
            }

            if (name[1] == '.' &&
                name[2] == '\0') {
                continue;
            }
        }

        files.emplace_back(name);
    }

    closedir(dir);

    //--------------------------------------------------
    // 创建 String[]
    //--------------------------------------------------

    jclass stringClass =
            env->FindClass("java/lang/String");

    if (stringClass == nullptr) {

        env->ReleaseStringUTFChars(
                path_str,
                path);

        return nullptr;
    }

    jobjectArray result =
            env->NewObjectArray(
                    (jsize) files.size(),
                    stringClass,
                    nullptr);

    if (result == nullptr) {

        env->DeleteLocalRef(stringClass);

        env->ReleaseStringUTFChars(
                path_str,
                path);

        return nullptr;
    }

    //--------------------------------------------------
    // 填充数据
    //--------------------------------------------------

    const size_t size = files.size();

    for (size_t i = 0; i < size; ++i) {

        jstring name =
                env->NewStringUTF(
                        files[i].c_str());

        if (name == nullptr) {
            continue;
        }

        env->SetObjectArrayElement(
                result,
                (jsize) i,
                name);

        env->DeleteLocalRef(name);

        if (env->ExceptionCheck()) {
            env->ExceptionClear();
            break;
        }
    }

    //--------------------------------------------------
    // 清理资源
    //--------------------------------------------------

    env->DeleteLocalRef(stringClass);

    env->ReleaseStringUTFChars(
            path_str,
            path);

    return result;
}



// 递归查找文件的核心函数
extern "C"
void Java_com_bella_testapp_JniTestActivity_findFiles(const std::string& basePath, const std::string& targetName, std::vector<std::string>& results) {
    DIR* dir = opendir(basePath.c_str());
    if (dir == nullptr) {
        return;
    }

    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        // 跳过 "." 和 ".."
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }

        // 拼接当前完整路径
        std::string fullPath = basePath;
        if (fullPath.back() != '/') {
            fullPath += "/";
        }
        fullPath += entry->d_name;

        // 💡 【核心修改点】：使用 strstr 检查 entry->d_name 是否包含 targetName
        // 如果返回值不为 nullptr，说明包含该子串
        if (strstr(entry->d_name, targetName.c_str()) != nullptr) {
            results.push_back(fullPath);
            LOGI("找到包含关键词的文件: %s", fullPath.c_str());
        }

        // 如果是目录，则递归进入
        if (entry->d_type == DT_DIR) {
            Java_com_bella_testapp_JniTestActivity_findFiles(fullPath, targetName, results);
        }
    }

    closedir(dir);
}
