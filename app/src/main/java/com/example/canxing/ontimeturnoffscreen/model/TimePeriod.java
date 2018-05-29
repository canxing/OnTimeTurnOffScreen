package com.example.canxing.ontimeturnoffscreen.model;

import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

/**
 * 时间段对象，包含开始时间，结束时间，是否开启，是否每天
 */
public class TimePeriod {
    public static final String TABLENAME = "timeperiod";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_START_HOUR = "starthour";
    public static final String COLUMN_START_MINUTE = "startminute";
    public static final String COLUMN_END_HOUR = "endhour";
    public static final String COLUMN_END_MINUTE = "endminute";
    public static final String COLUMN_IS_ON = "ison";
    public static final String COLUMN_IS_EVERY_DAY = "everyday";
    public static final String CREATE_TABLE =  "create table " + TABLENAME + "( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_START_HOUR + " int not null, "
            + COLUMN_START_MINUTE + " int not null, "
            + COLUMN_END_HOUR + " int not null, "
            + COLUMN_END_MINUTE + " int not null, "
            + COLUMN_IS_ON + " int not null, "
            + COLUMN_IS_EVERY_DAY + " int not null)" ;
    public static final int ON = 1;
    public static final int OFF = 0;
    private int id;             //时间段id
    private int startHour;      //开始时间的小时
    private int startMinute;    //开始时间的分钟
    private int endHour;        //结束时间的小时
    private int endMinute;      //结束时间的分钟
    private int isOn;           //是否开启，0表示关闭，1表示开启
    private int isEveryDay;     //是否每天都运行，0表示不是，1表示每天运行

    public String getStartTime() {
        return startHour + ":" + startMinute;
    }
    public String getEndTime() {
        return endHour + ":" + endMinute;
    }

    public TimePeriod() {}
    public TimePeriod(int startHour, int startMinute, int endHour, int endMinute) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public TimePeriod(int startHour, int startMinute, int endHour, int endMinute, int isOn, int isEveryDay) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.isOn = isOn;
        this.isEveryDay = isEveryDay;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public void setIsOn(int isOn) {
        this.isOn = isOn;
    }

    public void setIsEveryDay(int isEveryDay) {
        this.isEveryDay = isEveryDay;
    }

    public int getId() {
        return id;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public int getIsOn() {
        return isOn;
    }

    @Override
    public String toString() {
        return "TimePeriod{" +
                "id=" + id +
                ", startHour=" + startHour +
                ", startMinute=" + startMinute +
                ", endHour=" + endHour +
                ", endMinute=" + endMinute +
                ", isOn=" + isOn +
                ", isEveryDay=" + isEveryDay +
                '}';
    }

    public int getIsEveryDay() {
        return isEveryDay;
    }
}
