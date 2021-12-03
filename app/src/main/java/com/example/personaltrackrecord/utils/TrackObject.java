package com.example.personaltrackrecord.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TrackObject implements Serializable {
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Integer id;
    private int locType;
    private LocalDateTime archiveDate;
    private Double longitude;
    private Double latitude;
    private Float radius;
    private String coorType;
    private String adCode;
    private String town;
    private String street;
    private String locDesc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getLocType() {
        return locType;
    }

    public void setLocType(int locType) {
        this.locType = locType;
    }

    public LocalDateTime getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(LocalDateTime archiveDate) {
        this.archiveDate = archiveDate;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public String getCoorType() {
        return coorType;
    }

    public void setCoorType(String coorType) {
        this.coorType = coorType;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getLocDesc() {
        return locDesc;
    }

    public void setLocDesc(String locDesc) {
        this.locDesc = locDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackObject that = (TrackObject) o;
        return Objects.equals(id, that.id)
                && Objects.equals(locType, that.locType)
                && Objects.equals(archiveDate, that.archiveDate)
                && Objects.equals(longitude, that.longitude)
                && Objects.equals(latitude, that.latitude)
                && Objects.equals(radius, that.radius)
                && Objects.equals(coorType, that.coorType)
                && Objects.equals(adCode, that.adCode)
                && Objects.equals(town, that.town)
                && Objects.equals(street, that.street)
                && Objects.equals(locDesc, that.locDesc);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String toString() {
        return dateTimeFormatter.format(archiveDate)
                + "\t\t"
                + locType
                + "\t\t"
                + longitude
                + "\t\t"
                + latitude
                + "\t\t"
                + radius;
    }

    public String printString() {
        return "TrackObject{"
                + "id="
                + id
                + ", loctype="
                + locType
                + ", archiveDate="
                + archiveDate
                + ", longitude="
                + longitude
                + ", latitude="
                + latitude
                + ", radius="
                + radius
                + ", coorType='"
                + coorType
                + '\''
                + ", adCode='"
                + adCode
                + '\''
                + ", town='"
                + town
                + '\''
                + ", street='"
                + street
                + '\''
                + ", locDesc='"
                + locDesc
                + '\''
                + '}';
    }
}
