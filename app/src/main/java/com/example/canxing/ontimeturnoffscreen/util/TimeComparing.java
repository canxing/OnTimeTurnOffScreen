package com.example.canxing.ontimeturnoffscreen.util;

import android.util.Log;

/**
 * 时间比较类，可以比较一个时间是在另一时间之前还是之后
 * 或者判断一个时间是否在两个时间之间
 */
public class TimeComparing {
    /**
     * 判断t1时间字符串代表的时间是否在startTime和endTIme两个时间字符串代表的时间之间
     * 如果在，返回true，否则返回false
     *
     * 如果 startTime < endTIme,说明这段时间在同一天之内
     * 如果 startTime > endTIme 说明这段时间过了零点
     * 如果 startTime = endTIme 只能判断要判断的时间是否和开始时间相等
     * @param startTime 时间段的开始时间
     * @param endTime 时间段的结束时间
     * @param t1 判断时间
     * @return
     */
    public static boolean inPeriod(String startTime, String endTime, String t1) {
        if(isBefore(startTime, endTime)) {
            return isAfter(t1, startTime) && isBefore(t1, endTime);
        } else if (isAfter(startTime, endTime)) {
            return !isAfter(t1, endTime) && !isBefore(t1, startTime);
        } else {
            return isEquals(t1, startTime);
        }
    }
    /**
     * 判断t1时间字符串代表的时间是否在other时间字符串代表的时间之后，
     * 如果在之后，返回true，否则返回false
     * @param t1
     * @param other
     * @return
     */
    public static boolean isBefore(String t1, String other) {
        if(isAfter(t1, other) || isEquals(t1, other)) { return false; }
        return true;
    }
    /**
     * 比较两个时间字符串代表的时间是否是同一时间，如果是返回true，否则返回false
     * @param t1
     * @param other
     * @return
     */
    public static boolean isEquals(String t1, String other) {
        FourTuple<Integer, Integer, Integer, Integer> timeTuple
                = toIntTuple(t1, other);
        if(timeTuple == null) return false;
        if(timeTuple.first == timeTuple.third && timeTuple.second == timeTuple.fourth)
            return true;
        return false;
    }
    /**
     * 判断t1时间是否在other时间之后
     * @param t1 待比较时间
     * @param other 时间标准
     * @return 如果t1时间在other之后返回true,否则返回false
     */
    public static boolean isAfter(String t1, String other){
        FourTuple<Integer, Integer, Integer, Integer> timeTuple
                = toIntTuple(t1, other);
        if(timeTuple == null) return false;
        int startHour = timeTuple.first;
        int startMinute = timeTuple.second;
        int endHour = timeTuple.third;
        int endMinute = timeTuple.fourth;
        if(startHour > endHour) { return true; }
        else if(startHour < endHour) { return false; }
        else {
            if(startMinute > endMinute) { return true; }
            else { return false; }
        }
    }
    private static FourTuple<Integer, Integer, Integer, Integer> toIntTuple(String t1, String other) {
        String[] startTime = t1.split(":");
        String[] endTime = other.split(":");
        int startHour = 0;
        int startMinute = 0;
        int endHour = 0;
        int endMinute = 0;
        try{
            startHour = Integer.parseInt(startTime[0]);
            startMinute = Integer.parseInt(startTime[1]);
            endHour = Integer.parseInt(endTime[0]);
            endMinute = Integer.parseInt(endTime[1]);
        } catch (Exception e) {
            return null;
        }
        return Tuple.fourTuple(startHour, startMinute, endHour, endMinute);
    }

    /**
     * 计算两个时间点之间的差值
     * @param time
     * @param startTime
     * @return
     */
    public static int sub(String time, String startTime) {
        FourTuple<Integer, Integer, Integer, Integer> timeTuple
                = toIntTuple(time, startTime);
        if(timeTuple == null) return Integer.MAX_VALUE;
        int startHour = timeTuple.first;
        int startMinute = timeTuple.second;
        int endHour = timeTuple.third;
        int endMinute = timeTuple.fourth;
        if (isAfter(time, startTime)) {
            return (23 - startHour) * 60 + 60 - startMinute + endHour * 60 + endMinute;
        } else {
            return (endHour - startHour) * 60 + endMinute - startMinute;
        }
    }
}
