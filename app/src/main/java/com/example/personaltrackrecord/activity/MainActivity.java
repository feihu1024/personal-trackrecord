package com.example.personaltrackrecord.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.personaltrackrecord.R;
import com.example.personaltrackrecord.service.MyLocationService;
import com.example.personaltrackrecord.utils.TrackObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ServiceConnection mServiceConn;
    UpdateDataReceiver updateDataReceiver;
    ArrayList<TrackObject> dataList = new ArrayList<TrackObject>();
    ArrayAdapter<TrackObject> adapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // 开启服务
        Intent startIntent = new Intent(this, MyLocationService.class);
        startService(startIntent);

        // 创建service连接
        mServiceConn = new ServiceConnection() {
            MyLocationService.LocationBinder locationBinder;

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                locationBinder = (MyLocationService.LocationBinder) service;
                locationBinder.toForeground(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        // 点击按钮 进入上传界面
        TextView buttonUpload = findViewById(R.id.icon_button_upload);
        buttonUpload.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        // 点击按钮 进入地图
        TextView buttonMap = findViewById(R.id.icon_button_map);
        buttonMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BDMapActivity.class);
            TrackObject to = dataList.get(dataList.size() - 1);
            if (to != null) {
                ContentValues cv = new ContentValues();
                cv.put("longitude", to.getLongitude());
                cv.put("latitude", to.getLatitude());
                cv.put("radius", to.getRadius());
                intent.putExtra("data", cv);
            }
            startActivity(intent);
        });

        // 设置数据列表的展示
        ListView lv = (ListView) findViewById(R.id.lv);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList); // 新建并配置ArrayAapeter
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            TrackObject to = (TrackObject) parent.getItemAtPosition(position);
            // 启动足迹详情Activity
            Intent intent = new Intent(MainActivity.this, LocationDetailActivity.class);
            intent.putExtra("data", to);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 绑定定位服务
        Intent bindIntent = new Intent(this, MyLocationService.class);
        bindService(bindIntent, mServiceConn, BIND_AUTO_CREATE);

        // 注册广播接收器 只接收定位服务发送的广播
        IntentFilter intentDataFilter = new IntentFilter("ACTION_UPDATE_DATA");
        updateDataReceiver = new UpdateDataReceiver();
        registerReceiver(updateDataReceiver, intentDataFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 解绑定位服务
        unbindService(mServiceConn);
        // 注销广播接收器
        unregisterReceiver(updateDataReceiver);
    }

    // 广播接收器 接收并处理位置服务发来的最新定位信息
    private class UpdateDataReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            ContentValues cv = intent.getParcelableExtra("data");
            TrackObject to = new TrackObject();

            to.setArchiveDate(LocalDateTime.parse(cv.getAsString("archiveDate")));
            to.setLocType(cv.getAsInteger("locType"));
            to.setLongitude(cv.getAsDouble("longitude"));
            to.setLatitude(cv.getAsDouble("latitude"));
            to.setRadius(cv.getAsFloat("radius"));
            to.setCoorType(cv.getAsString("coorType"));
            to.setAdCode(cv.getAsString("adCode"));
            to.setTown(cv.getAsString("town"));
            to.setStreet(cv.getAsString("street"));
            to.setLocDesc(cv.getAsString("locDesc"));
            dataList.add(to);
            adapter.notifyDataSetChanged();
        }
    }
}
