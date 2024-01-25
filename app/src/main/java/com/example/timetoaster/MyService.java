package com.example.timetoaster;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;

import java.util.List;

public class MyService extends Service {
    private Handler handler;
    private Runnable periodicTask;
    private static final int NOTIFICATION_ID = 1;
    private static int externalTime;
    private static int internalTime;
    private static MyService intent;
    private static int limitCount,limit = 10;
    private static boolean flag;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        intent = this;
        handler = new Handler();
        setupPeriodicTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID,NotificationHelper.createNotification(this));
        return super.onStartCommand(intent, flags, startId);
    }

    public static boolean isActive() {
        return intent!=null;
    }

    private void setupPeriodicTask() {
        periodicTask = new Runnable() {
            @Override
            public void run() {
                UsageStatsManager usageStatsManager = (UsageStatsManager) MyService.this.getSystemService(Context.USAGE_STATS_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    long currentTime = System.currentTimeMillis();
                    List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, currentTime);

                    if (stats != null) {
                        long lastUsedTime = 0;
                        String currentForegroundApp = "";

                        for (UsageStats usageStats : stats) {
                            if (usageStats.getLastTimeUsed() > lastUsedTime) {
                                lastUsedTime = usageStats.getLastTimeUsed();
                                currentForegroundApp = usageStats.getPackageName();
                            }
                        }

                        if (currentForegroundApp.equals("com.instagram.android")) {
                            if(limitCount >0 && limitCount <limit && flag) {
                                Toast.makeText(MyService.this, "Subconscious Touch", Toast.LENGTH_SHORT).show();
                            }
                            flag = true;
                            externalTime+=1;
                            if(externalTime==900) {
                                internalTime+=externalTime;
                                Toast.makeText(MyService.this, "Wasted "+internalTime/60+" minute(s)", Toast.LENGTH_SHORT).show();
                                externalTime = 0;
                            }
                            limitCount = 0;
                        }
                        else {
                            externalTime = 0;
                            internalTime = 0;
                            if(limitCount <limit) limitCount++;
                        }
                    }
                }
                handler.postDelayed(this, 1000); // Repeat every 1000 milliseconds (1 second)
            }
        };
        handler.postDelayed(periodicTask, 1000); // Start the periodic task initially after 1000 milliseconds
    }

    @Override
    public void onDestroy() {
        intent = null;
        flag = false;
        handler.removeCallbacks(periodicTask); // Remove the periodic task when the service is destroyed
        super.onDestroy();
    }
}
