package ru.alexfitness.trainingschedule.scrollCalendarView;

import java.util.Calendar;

public interface EventsLoader {

    void loadEvents(Calendar startDate, Calendar endDate);
    void setOnLoadListener(EventsLoaderListener eventsLoaderListener);
    void onLoad();

}
