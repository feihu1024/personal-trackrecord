package com.example.personaltrackrecord.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TimeObject {
    private String text;
    private LocalDateTime time;

    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RequiresApi(api = Build.VERSION_CODES.O)
    public TimeObject(String text, int dayCount) {
        this.text = text;
        if (dayCount < 0) {
            // 天数小于0 表示全部
            this.time = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        } else {
            LocalDate localDate = LocalDateTime.now().minusDays(dayCount).toLocalDate();
            this.time = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
        }
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeObject that = (TimeObject) o;
        return time == that.time && text.equals(that.text);
    }

    @Override
    public String toString() {
        return text;
    }

    public String toTimeString() {
        return dateTimeFormatter.format(time);
    }
}
