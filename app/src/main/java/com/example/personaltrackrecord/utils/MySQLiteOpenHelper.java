package com.example.personaltrackrecord.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

  public static final int VERSION = 1;
  public static final String DATABASE_NAME = "db_location";
  public static final String TABLE_NAME = "tb_track";

  public MySQLiteOpenHelper(
      Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String sql =
        "create table "
            + TABLE_NAME
            + "(id integer primary key autoincrement,locType integer, archiveDate datetime, longitude real, latitude real, radius real,adCode vchar(32),coorType vchar(32),town vchar(255),street vchar(255), locDesc vchar(255))";
    db.execSQL(sql);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
