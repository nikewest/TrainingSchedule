package com.alamkanak.weekview;

import java.util.Calendar;
import java.util.List;

public class MonthLoader implements WeekViewLoader {

    private MonthChangeListener mOnMonthChangeListener;

    public MonthLoader(MonthChangeListener listener){
        this.mOnMonthChangeListener = listener;
    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance){
        return instance.get(Calendar.YEAR) * 12 + instance.get(Calendar.MONTH) + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0;
    }

    @Override
    public void onLoad(int startPeriodIndex, int endPeriodIndex) {
        mOnMonthChangeListener.onMonthChange(startPeriodIndex / 12, startPeriodIndex % 12 + 1, endPeriodIndex / 12, endPeriodIndex % 12 + 1);
    }

    public MonthChangeListener getOnMonthChangeListener() {
        return mOnMonthChangeListener;
    }

    public void setOnMonthChangeListener(MonthChangeListener onMonthChangeListener) {
        this.mOnMonthChangeListener = onMonthChangeListener;
    }

    public interface MonthChangeListener {

        void onMonthChange(int startNewYear, int startNewMonth, int endNewYear, int endNewMonth);

    }
}
