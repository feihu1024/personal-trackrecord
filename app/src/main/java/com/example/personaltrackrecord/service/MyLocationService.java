package com.example.personaltrackrecord.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.personaltrackrecord.utils.MyLocationListener;
import com.example.personaltrackrecord.utils.NotificationUtils;

public class MyLocationService extends Service {
    private LocationClient locationClient;
    private MyLocationListener myLocationListener;
    private NotificationUtils mNotificationUtils;
    private LocationBinder locationBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());

        // 配置百度定位客户端参数
        LocationClientOption locationOption = getLocationOption();
        myLocationListener = new MyLocationListener(getApplicationContext());
        myLocationListener.setService(this);
        locationClient.setLocOption(locationOption);
        locationClient.registerLocationListener(myLocationListener);

        // 创建通知信道
        if (Build.VERSION.SDK_INT >= 26)
            mNotificationUtils = new NotificationUtils(getApplicationContext());

        // 创建Binder
        locationBinder = new LocationBinder();

        // 开始定位
        locationClient.start();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    public LocationClientOption getLocationOption() {
        LocationClientOption locationOption = new LocationClientOption();
        locationOption.setIsNeedAltitude(true);
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationOption.setIsNeedAddress(true);
        // locationOption.setLocationNotify(true);
        locationOption.setIsNeedLocationDescribe(true);
        locationOption.SetIgnoreCacheException(false);
        // locationOption.setIgnoreKillProcess(true);  // 默认true
        locationOption.setOpenGps(true);
        locationOption.setScanSpan(3000);
        // locationOption.setOpenAutoNotifyMode(3000, 100,
        // LocationClientOption.LOC_SENSITIVITY_HIGHT);
        return locationOption;
    }

    public void updataContentValues(ContentValues cv) {
        Intent intent = new Intent();
        intent.setAction("ACTION_UPDATE_DATA");
        intent.putExtra("data", cv);
        // 发送广播通知activity更新lv
        sendBroadcast(intent);
        // System.out.println(cv.get("longitude")+"\t"+cv.get("latitude"));
    }

    public void updateErrorMessages(final String message) {
        Intent intent = new Intent();
        intent.setAction("ACTION_UPDATE_ERROR");
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    private Notification createNotification(AppCompatActivity activity) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification("足迹 后台定位", "正在后台定位");
            notification = builder2.build();
        } else {
            // 获取一个Notification构造器
            Notification.Builder builder = new Notification.Builder(activity);
            Intent nfIntent = new Intent(activity, activity.getClass());

            builder
                    .setContentIntent(PendingIntent.getActivity(activity, 0, nfIntent, 0)) // 设置PendingIntent
                    .setContentTitle("足迹 后台定位") // 设置下拉列表里的标题
                    // .setSmallIcon(R.drawable.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("正在后台定位") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

            notification = builder.build(); // 获取构建好的Notification
        }
        notification.defaults = Notification.DEFAULT_SOUND; // 设置为默认的声音
        return notification;
    }

    public class LocationBinder extends Binder {

        public void toForeground(AppCompatActivity activity) {
            Notification notification = createNotification(activity);
            locationClient.enableLocInForeground(1, notification);
        }
    }
}
