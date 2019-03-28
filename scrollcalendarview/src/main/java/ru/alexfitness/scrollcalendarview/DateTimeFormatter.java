package ru.alexfitness.scrollcalendarview;

import java.util.Date;

public interface DateTimeFormatter {

    String dateToString(Date date);
    String timeToString(Date date);
    String timeToString(int hour, int minute);

}
