package com.example.personaltrackrecord.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.personaltrackrecord.R;
import com.example.personaltrackrecord.utils.MySQLiteOpenHelper;
import com.example.personaltrackrecord.utils.TimeObject;
import com.example.personaltrackrecord.utils.TrackObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class UploadActivity extends AppCompatActivity {

    UpdateErrorReceiver updateErrorReceiver;

    private SQLiteDatabase db;
    private Button btnUpload = null;
    private Spinner spnTime = null;
    private TextView tvTime;
    private TextView tvErrorMsg;
    private TextView tvUploadMsg;
    private static String TV_TIME_TEXT = "请选择时间范围：";
    private String TAG_UPDATE = "upload";
    private String TAG_ERROR = "error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(UploadActivity.this, MySQLiteOpenHelper.DATABASE_NAME, null, MySQLiteOpenHelper.VERSION);
        this.db = dbHelper.getReadableDatabase();

        btnUpload = (Button) findViewById(R.id.btn_upload);
        spnTime = (Spinner) findViewById(R.id.spn_time);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvTime.setText(TV_TIME_TEXT);
        tvErrorMsg = (TextView) findViewById(R.id.tv_error_msg);
        tvUploadMsg = (TextView) findViewById(R.id.tv_upload_msg);

        initSpinner(spnTime);

        btnUpload.setOnClickListener(
                v -> {
                    TimeObject timeObject = (TimeObject) spnTime.getSelectedItem();
                    Thread uploadThread = createUploadThread(UploadActivity.this, timeObject, tvUploadMsg, false);
                    uploadThread.start();
                    // insertTest(UploadActivity.this);
                });

        // 设置返回
        TextView buttonBack = findViewById(R.id.icon_button_back);
        buttonBack.setOnClickListener(
                v -> {
                    this.finish();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 监听定位失败的通知
        IntentFilter intentErrorFilter = new IntentFilter("ACTION_UPDATE_ERROR");
        updateErrorReceiver = new UpdateErrorReceiver();
        registerReceiver(updateErrorReceiver, intentErrorFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(updateErrorReceiver);
    }

    public void initSpinner(Spinner spn) {
        List<TimeObject> spnList = new ArrayList<>();
        spnList.add(new TimeObject("当天", 0));
        spnList.add(new TimeObject("最近2天", 1));
        spnList.add(new TimeObject("最近3天", 2));
        spnList.add(new TimeObject("最近5天", 4));
        spnList.add(new TimeObject("最近7天", 6));
        spnList.add(new TimeObject("全部", -1));
        ArrayAdapter<TimeObject> spnAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spnList);
        spn.setAdapter(spnAdapter);
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeObject timeObject = (TimeObject) parent.getAdapter().getItem(position);
                int trackCount= queryTrackCountByTime(timeObject.toTimeString());
                tvTime.setText(TV_TIME_TEXT + "\t当前共: " + trackCount + "条数据");
                Log.d(TAG_UPDATE, "count： " + trackCount + "   time: " + timeObject.toTimeString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public int queryTrackCountByTime(String time) {
        // 查询最近24h以内的数据
        String sql_queryTrackCount = "select count(id) as tcount from " + MySQLiteOpenHelper.TABLE_NAME + " where archiveDate >= '" + time + "';";
        Cursor cursor = db.rawQuery(sql_queryTrackCount, null);
        cursor.moveToNext();
        int trackCount = cursor.getInt(cursor.getColumnIndex("tcount"));
        return trackCount;
    }

    public static Thread createUploadThread(Context context, TimeObject timeObject, TextView messageView, boolean isAuto) {
        Thread queryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                postMessage(messageView, "开始导出.....");

                String timeString = timeObject.toTimeString();

                SQLiteDatabase db = new MySQLiteOpenHelper(context, MySQLiteOpenHelper.DATABASE_NAME, null, MySQLiteOpenHelper.VERSION).getReadableDatabase();
                String sql_queryTrackCount = "select count(id) as tcount from " + MySQLiteOpenHelper.TABLE_NAME + " where archiveDate >= '" + timeString + "';";

                // 查询记录总数
                Cursor cursor = db.rawQuery(sql_queryTrackCount, null);
                cursor.moveToNext();
                int trackCount = cursor.getInt(cursor.getColumnIndex("tcount"));


                List<TrackObject> tracklist = null;
                int writeSzie = 4000;
                int writeCount = (int) Math.ceil(trackCount /(double) writeSzie);
                int success=0;



                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT INTO \"public\".\"tb_track\"" + "(\"loc_type\", \"create_time\", \"loc_point\", " + "\"longitude\", \"latitude\", \"radius\", " +
                        "\"coor_type\", " + "\"ad_code\", \"town\", \"street\", \"ad_desc\") VALUES ");

                String fileName = "trackdata_" + timeObject.getText() + "_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".sql";
                File file = new File(context.getExternalFilesDir(isAuto ? "track-data-auto" : "track-data-custom"), fileName);
                FileWriter fileWriter = null;

                try{
                    fileWriter = new FileWriter(file);
                    fileWriter.write(sqlBuilder.toString());
                    for (int i = 0; i < writeCount; i++) {
                        tracklist = queryTrackListByPage(context, timeObject.toTimeString(),i,writeSzie);
                        sqlBuilder.setLength(0);
                        for (int j=0;j<tracklist.size();j++) {
                            sqlBuilder.append(tracklist.get(j).toSqlString(false)+",");
                            // 替换最后的逗号
                            if (i==writeCount-1 && j == tracklist.size() - 1) {
                                sqlBuilder.replace(sqlBuilder.length() - 1, sqlBuilder.length(), " ON conflict(create_time) DO NOTHING;");
                            }
                            // 计数并输出结果
                            success++;
                            postMessage(messageView, "数据导出中...\t" + String.format("%.2f",Double.valueOf(success/(double)trackCount*100))+"%" );
                        }
                        // 写入文件
                        fileWriter.write(sqlBuilder.toString());
                    }
                    fileWriter.close();
                    postMessage(messageView, "导出成功,当前共导出：" + success + "条\n" + "导出文件: " + fileName);
                } catch (IOException e) {
                    postMessage(messageView, "导出文件异常");
                    System.out.println("导出文件异常");
                    e.printStackTrace();
                    try {
                        fileWriter.close();
                    } catch (IOException ioException) {
                        postMessage(messageView, "文件关闭异常");
                        System.out.println("文件关闭异常");
                        ioException.printStackTrace();
                    }
                } finally {
                    try {
                        fileWriter.close();
                    } catch (IOException ioException) {
                        postMessage(messageView, "文件关闭异常");
                        System.out.println("文件关闭异常");
                        ioException.printStackTrace();
                    }
                }
            }
        });

        return queryThread;
    }

    public static void insertTest(Context context) {
        // System.out.println(tracklist.get(0).printString());
        // TrackObject{id=184469, loctype=61, archiveDate=2022-04-30T00:00:01, longitude=108.971217, latitude=34.225874, radius=9.0, coorType='gcj02', adCode='610113', town='大雁塔街道', street='西影路', locDesc='在后村嘉园附近'}
        // id,aDate,locType,longitude,latitude,radius,adCode,coorType,town,street,locDesc
        // locType===10000的都是测试数据
        SQLiteDatabase db = new MySQLiteOpenHelper(context, MySQLiteOpenHelper.DATABASE_NAME, null, MySQLiteOpenHelper.VERSION).getWritableDatabase();
        // db.execSQL("delete from tb_track where locType = 10000");
        int writeSzie = 1000;
        int writeCount = 1000;
        String execSql = "";
        for (int i = 0; i < writeCount; i++) {
            String sql = "insert into "+MySQLiteOpenHelper.TABLE_NAME+"(locType,archiveDate,longitude,latitude,radius,coorType,adCode,town,street,locDesc) values";
            for (int j = i * writeSzie; j < (i + 1) * writeSzie && j < writeSzie*writeCount; j++) {
                sql+="("+"10000"+",'"+TimeObject.toTimeString(LocalDateTime.now())+"',108.971217"+",34.225874"+",9.0"+",'gcj02'"+",610113"+",'大雁塔街道'"+",'西影路'"+",'在后村嘉园附近'),";
            }
            execSql=sql.substring(0,sql.length()-1);
            System.out.println(i+": "+execSql);
            db.execSQL(execSql);
        }
    }

    public static List<TrackObject> queryTrackList(Context context, String time) {
        List<TrackObject> tracklist = new ArrayList<>();
        SQLiteDatabase db = new MySQLiteOpenHelper(context, MySQLiteOpenHelper.DATABASE_NAME, null, MySQLiteOpenHelper.VERSION).getReadableDatabase();
        String sql_queryTracklist = "select *, datetime(archiveDate) as aDate from " + MySQLiteOpenHelper.TABLE_NAME + " where archiveDate >= '" + time + "';";
        Cursor cursor = db.rawQuery(sql_queryTracklist, null);
        while (cursor.moveToNext()) {
            TrackObject to = new TrackObject();
            to.setId(cursor.getInt(0));
            to.setArchiveDate(LocalDateTime.parse(cursor.getString(cursor.getColumnIndex("aDate")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            to.setLocType(cursor.getInt(cursor.getColumnIndex("locType")));
            to.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
            to.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
            to.setRadius(cursor.getFloat(cursor.getColumnIndex("radius")));
            to.setAdCode(cursor.getString(cursor.getColumnIndex("adCode")));
            to.setCoorType(cursor.getString(cursor.getColumnIndex("coorType")));
            to.setTown(cursor.getString(cursor.getColumnIndex("town")));
            to.setStreet(cursor.getString(cursor.getColumnIndex("street")));
            to.setLocDesc(cursor.getString(cursor.getColumnIndex("locDesc")));
            tracklist.add(to);
        }
        return tracklist;
    }

    public static List<TrackObject> queryTrackListByPage(Context context, String time,int page,int size) {
        List<TrackObject> tracklist = new ArrayList<>();
        SQLiteDatabase db = new MySQLiteOpenHelper(context, MySQLiteOpenHelper.DATABASE_NAME, null, MySQLiteOpenHelper.VERSION).getReadableDatabase();
        String sql_queryTracklist = "select *, datetime(archiveDate) as aDate from " + MySQLiteOpenHelper.TABLE_NAME + " where archiveDate >= '" + time + "' limit "+size+" offset "+(size*page)+";";
        Cursor cursor = db.rawQuery(sql_queryTracklist, null);
        while (cursor.moveToNext()) {
            TrackObject to = new TrackObject();
            to.setId(cursor.getInt(0));
            to.setArchiveDate(LocalDateTime.parse(cursor.getString(cursor.getColumnIndex("aDate")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            to.setLocType(cursor.getInt(cursor.getColumnIndex("locType")));
            to.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
            to.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
            to.setRadius(cursor.getFloat(cursor.getColumnIndex("radius")));
            to.setAdCode(cursor.getString(cursor.getColumnIndex("adCode")));
            to.setCoorType(cursor.getString(cursor.getColumnIndex("coorType")));
            to.setTown(cursor.getString(cursor.getColumnIndex("town")));
            to.setStreet(cursor.getString(cursor.getColumnIndex("street")));
            to.setLocDesc(cursor.getString(cursor.getColumnIndex("locDesc")));
            tracklist.add(to);
        }
        return tracklist;
    }

    public static void postMessage(TextView textView, String message) {
        if (textView != null) {
            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(message);
                }
            });
        }
    }

    private class UpdateErrorReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            tvErrorMsg.setText("---------------------------------\n" + message);
        }
    }
}