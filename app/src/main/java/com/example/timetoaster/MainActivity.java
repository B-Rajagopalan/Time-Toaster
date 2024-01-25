package com.example.timetoaster;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static Button track;
    private static Button stop;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        track = findViewById(R.id.button);
        stop = findViewById(R.id.stopButton);

        Intent intent =new Intent(MainActivity.this, MyService.class);

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasUsageStatsPermission(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "Usage Access Required", Toast.LENGTH_SHORT).show();
                    requestUsageStatsPermission(MainActivity.this);
                } else if (MyService.isActive()) {
                    Toast.makeText(MainActivity.this, "Already Running", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Started Service", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startService(intent);
                    }
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!MyService.isActive()) {
                    Toast.makeText(MainActivity.this, "Service Not Yet Started", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
                else {
                    Toast.makeText(MainActivity.this, "Stopped Service", Toast.LENGTH_SHORT).show();
                    stopService(intent);
                    finishAffinity();
                }
            }
        });
    }

    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }
}
