package ru.alexfitness.trainingschedule.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract  class Converter {

    private static final String HEXES = "0123456789ABCDEF";

    public static String tagIdToHexString(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * byteArray.length);
        for (final byte b : byteArray) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static String dateToString1C(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        return simpleDateFormat.format(date);
    }

    public static Calendar calendarFromString(String dateString) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormat.parse(dateString));
        return (Calendar) calendar.clone();
    }
}
