package com.example.personaltrackrecord.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.personaltrackrecord.activity.UploadActivity;
import com.example.personaltrackrecord.service.MyLocationService;

import java.time.LocalDateTime;
import java.util.Date;

public class MyLocationListener extends BDAbstractLocationListener {

    Context context;
    SQLiteDatabase db;
    MyLocationService service;

    public MyLocationListener(Context context) {
        super();
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(context, MySQLiteOpenHelper.DATABASE_NAME, null, MySQLiteOpenHelper.VERSION);
        this.context = context;
        this.db = dbHelper.getWritableDatabase();
    }

    public void setService(MyLocationService service) {
        this.service = service;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceiveLocation(BDLocation location) {

        int locType = location.getLocType(); // 定位结果返回码： 61 GPS定位 161网络定位 66离线定位
        if (locType == BDLocation.TypeGpsLocation || locType == BDLocation.TypeNetWorkLocation || locType == BDLocation.TypeOffLineLocation) {
            ContentValues cv = new ContentValues();

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float radius = location.getRadius();
            String coorType = location.getCoorType();
            String time = location.getTime();

          /*地址信息格式：country + province + city + district + town + street
            地址信息格式：中国 宁夏回族自治区 银川市 兴庆区 胜利街街道 永安巷
          */
            // String addr = location.getAddrStr();    //获取详细地址信息
            // String country = location.getCountry();    //获取国家
            // String province = location.getProvince();    //获取省份
            // String city = location.getCity();    //获取城市
            // String district = location.getDistrict();    //获取区县
            String street = location.getStreet(); // 获取街道信息
            String adCode = location.getAdCode(); // 获取adcode
            String town = location.getTown(); // 获取乡镇信息
            String locationDescription = location.getLocationDescribe(); // 获取地址描述信息
            LocalDateTime archiveDate = LocalDateTime.now();

            cv.put("locType", locType);
            cv.put("archiveDate", archiveDate.toString());
            cv.put("longitude", longitude);
            cv.put("latitude", latitude);
            cv.put("radius", radius);
            cv.put("adCode", adCode);
            cv.put("coorType", coorType);
            cv.put("town", town);
            cv.put("street", street);
            cv.put("locDesc", locationDescription);
            service.updataContentValues(cv);
            long id = this.db.insert(MySQLiteOpenHelper.TABLE_NAME, null, cv);
            service.updateErrorMessages("定位成功   :  " + archiveDate.toString() + "\n定位返回码:  " + locType);
        } else {
            service.updateErrorMessages("定位失败   :  " + LocalDateTime.now().toString() + "\n定位返回码:  " + locType);
        }
        CheckUpdateLocalFile();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    // 每隔3小时定时备份当天数据
    private void CheckUpdateLocalFile() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("personaltrackrecord", Context.MODE_PRIVATE);
        long timestamp = new Date().getTime();
        long time = timestamp - sharedPreferences.getLong("lastUpdateTime", 0);
        if (time >= 10800000) {
            Thread uploadThread = UploadActivity.createUploadThread(context, new TimeObject("最近1天", 0), null, true);
            uploadThread.start();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("lastUpdateTime", timestamp);
            editor.apply();
        }
    }
}
