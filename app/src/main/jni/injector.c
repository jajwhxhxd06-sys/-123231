#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "Injector"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

int get_roblox_pid() {
    FILE *fp = popen("pidof com.roblox.client", "r");
    int pid = 0;
    fscanf(fp, "%d", &pid);
    pclose(fp);
    return pid;
}

void inject_script(int pid) {
    char script_path[256];
    sprintf(script_path, "/data/local/tmp/script.lua");

    if (ptrace(PTRACE_ATTACH, pid, NULL, NULL) == -1) {
        LOGI("Не удалось прикрепиться к процессу");
        return;
    }
    waitpid(pid, NULL, 0);

    void *handle = dlopen("libluajit.so", RTLD_NOW);
    if (!handle) {
        LOGI("Не найдена libluajit.so");
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return;
    }

    int (*luaL_dofile)(void*, const char*) = (int (*)(void*, const char*))dlsym(handle, "luaL_dofile");
    if (!luaL_dofile) {
        LOGI("Функция luaL_dofile не найдена");
        dlclose(handle);
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return;
    }

    void *L = (void*)0x12345678; // Требуется актуальный оффсет!
    int result = luaL_dofile(L, script_path);
    LOGI("Скрипт выполнен, результат: %d", result);

    dlclose(handle);
    ptrace(PTRACE_DETACH, pid, NULL, NULL);
}

int main() {
    int pid = get_roblox_pid();
    if (pid == 0) {
        LOGI("Roblox не запущен");
        return 1;
    }
    inject_script(pid);
    return 0;
}
