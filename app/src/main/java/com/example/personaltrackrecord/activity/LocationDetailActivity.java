package com.example.personaltrackrecord.activity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDLocation;
import com.example.personaltrackrecord.R;
import com.example.personaltrackrecord.utils.TimeObject;
import com.example.personaltrackrecord.utils.TrackObject;

public class LocationDetailActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 设置返回
        TextView buttonBack = findViewById(R.id.icon_button_back);
        buttonBack.setOnClickListener(
                v -> {
                    this.finish();
                });

        TrackObject o = (TrackObject) getIntent().getSerializableExtra("data");

        TextView tv_archiveDate = findViewById(R.id.tv_archiveDate);
        TextView tv_locType = findViewById(R.id.tv_locType);
        TextView tv_longitude = findViewById(R.id.tv_longitude);
        TextView tv_latitude = findViewById(R.id.tv_latitude);
        TextView tv_radius = findViewById(R.id.tv_radius);
        TextView tv_coorType = findViewById(R.id.tv_coorType);
        TextView tv_adCode = findViewById(R.id.tv_adCode);
        TextView tv_town = findViewById(R.id.tv_town);
        TextView tv_street = findViewById(R.id.tv_street);
        TextView tv_locDesc = findViewById(R.id.tv_locDesc);

        String locTypeString = "";
        int locType = o.getLocType();
        if (locType == BDLocation.TypeGpsLocation) {
            locTypeString = "gps定位";
        } else if (locType == BDLocation.TypeNetWorkLocation) {
            locTypeString = "网络定位";
        } else if (locType == BDLocation.TypeOffLineLocation) {
            locTypeString = "离线定位";
        } else {
            locTypeString = "未知定位";
        }

        tv_archiveDate.setText("归档时间： " + TimeObject.dateTimeFormatter.format(o.getArchiveDate()));
        tv_locType.setText("定位方式： " + locTypeString + "(" + locType + ")");
        tv_longitude.setText("目标经度： " + o.getLongitude().toString());
        tv_latitude.setText("目标纬度： " + o.getLatitude().toString());
        tv_radius.setText("定位精度： " + o.getRadius().toString() + "m");
        tv_coorType.setText("坐标类型： " + o.getCoorType());
        tv_adCode.setText("行政编码： " + o.getAdCode());
        tv_town.setText("社区名称： " + o.getTown());
        tv_street.setText("街道名称： " + o.getStreet());
        tv_locDesc.setText("地址描述： " + o.getLocDesc());
    }
}
