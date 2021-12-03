package com.example.personaltrackrecord.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.personaltrackrecord.R;
import com.example.personaltrackrecord.service.MyLocationService;

public class BDMapActivity extends AppCompatActivity {
  private MapView mMapView = null;
  private BaiduMap mBaiduMap = null;
  private boolean needZoomto = true;
  UpdateDataReceiver updateDataReceiver;

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bdmap);
    // 获取地图控件引用
    mMapView = (MapView) findViewById(R.id.bmapView);
    mBaiduMap = mMapView.getMap();

    // 设置返回
    TextView buttonBack = findViewById(R.id.icon_button_back);
    buttonBack.setOnClickListener(
        v -> {
          this.finish();
        });

    // 设置定位样式
    BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
    MyLocationConfiguration myLocationConfiguration =
        new MyLocationConfiguration(
            MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
    mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);
    mBaiduMap.setMyLocationEnabled(true);

    // 启动时进行一次定位
    ContentValues cv = this.getIntent().getParcelableExtra("data");
    if (cv != null && needZoomto) {
      zoomTo(cv);
      needZoomto = false;
    }

    // 开启服务
    Intent startIntent = new Intent(this, MyLocationService.class);
    startService(startIntent);

    // 注册广播接收器 只接收定位服务发送的广播
    IntentFilter intentFilter = new IntentFilter("ACTION_UPDATE_DATA");
    updateDataReceiver = new UpdateDataReceiver();
    registerReceiver(updateDataReceiver, intentFilter);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.onDestroy();
    // 注销广播接收器
    unregisterReceiver(updateDataReceiver);
  }

  // 广播接收器 接收并处理位置服务发来的最新定位信息
  private class UpdateDataReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
      ContentValues cv = intent.getParcelableExtra("data");
      double longitude = cv.getAsDouble("longitude");
      double latitude = cv.getAsDouble("latitude");
      float radius = cv.getAsFloat("radius");
      //            float direction = cv.getAsFloat("direction");
      MyLocationData md =
          new MyLocationData.Builder()
              .accuracy(radius)
              .longitude(longitude)
              .latitude(latitude)
              .build();
      mBaiduMap.setMyLocationData(md);
      if (needZoomto) {
        zoomTo(cv);
        needZoomto = false;
      }
    }
  }

  private void zoomTo(ContentValues cv) {
    double longitude = cv.getAsDouble("longitude");
    double latitude = cv.getAsDouble("latitude");
    float radius = cv.getAsFloat("radius");
    MyLocationData md =
        new MyLocationData.Builder()
            .accuracy(radius)
            .longitude(longitude)
            .latitude(latitude)
            .build();
    mBaiduMap.setMyLocationData(md);
    MapStatus mapStatus =
        new MapStatus.Builder().target(new LatLng(latitude, longitude)).zoom(18).build();
    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
  }
}
