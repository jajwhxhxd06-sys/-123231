package com.swill.injector;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String SCRIPT_PATH = "/data/local/tmp/script.lua";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button injectButton = findViewById(R.id.inject_button);
        injectButton.setOnClickListener(v -> {
            copyScript();
            injectViaShell();
        });
    }

    private void copyScript() {
        try {
            InputStream is = getAssets().open("script.lua");
            FileOutputStream fos = new FileOutputStream(SCRIPT_PATH);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();
            Toast.makeText(this, "Скрипт скопирован", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка копирования", Toast.LENGTH_SHORT).show();
        }
    }

    private void injectViaShell() {
        try {
            // Получаем PID Roblox
            Process pidProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", "pidof com.roblox.client"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(pidProcess.getInputStream()));
            String pidStr = reader.readLine();
            if (pidStr == null || pidStr.isEmpty()) {
                Toast.makeText(this, "Roblox не запущен", Toast.LENGTH_SHORT).show();
                return;
            }
            int pid = Integer.parseInt(pidStr.trim());

            // Записываем Lua-скрипт прямо в память процесса через /proc/pid/mem
            String memPath = "/proc/" + pid + "/mem";
            String mapsPath = "/proc/" + pid + "/maps";

            // Ищем адрес libluajit.so в maps
            Process mapsProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat " + mapsPath + " | grep libluajit.so"});
            BufferedReader mapsReader = new BufferedReader(new InputStreamReader(mapsProcess.getInputStream()));
            String mapLine = mapsReader.readLine();
            if (mapLine == null) {
                Toast.makeText(this, "libluajit.so не найдена", Toast.LENGTH_SHORT).show();
                return;
            }

            // Парсим базовый адрес
            String[] parts = mapLine.split(" ");
            String[] addrRange = parts[0].split("-");
            long baseAddr = Long.parseLong(addrRange[0], 16);

            // Ищем смещение luaL_dofile (это нужно обновлять под версию)
            long offsetLuaLDoFile = 0x12345678; // Пример, нужно актуальное смещение
            long addrLuaLDoFile = baseAddr + offsetLuaLDoFile;

            // Записываем вызов функции (грубо — через shell)
            String injectCommand = "echo 'call luaL_dofile(" + addrLuaLDoFile + ", \"" + SCRIPT_PATH + "\")' > /proc/" + pid + "/mem";
            Process injectProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", injectCommand});
            int exitCode = injectProcess.waitFor();

            if (exitCode == 0) {
                Toast.makeText(this, "Инжект выполнен (без бинарника)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка инжекта", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
