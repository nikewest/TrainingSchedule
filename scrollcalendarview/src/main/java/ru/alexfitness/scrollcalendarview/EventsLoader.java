package ru.alexfitness.scrollcalendarview;

import java.util.Calendar;

public interface EventsLoader {

    void loadEvents(Calendar startDate, Calendar endDate);
    void setOnLoadListener(EventsLoaderListener eventsLoaderListener);
    void onLoad();

}
