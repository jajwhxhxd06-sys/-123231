package com.swill.injector;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String SCRIPT_PATH = "/data/local/tmp/script.lua";
    private static final String INJECTOR_PATH = "/data/local/tmp/injector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button injectButton = findViewById(R.id.inject_button);
        injectButton.setOnClickListener(v -> {
            copyAsset("script.lua", SCRIPT_PATH);
            copyAsset("injector", INJECTOR_PATH);
            runInjector();
        });
    }

    private void copyAsset(String assetName, String destPath) {
        try {
            InputStream is = getAssets().open(assetName);
            FileOutputStream fos = new FileOutputStream(destPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();

            if (assetName.equals("injector")) {
                Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod 755 " + destPath});
            }
            Toast.makeText(this, assetName + " скопирован", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void runInjector() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", INJECTOR_PATH});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Toast.makeText(this, "Инжект выполнен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка инжекта (код " + exitCode + ")", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
