package ru.alexfitness.trainingschedule.scrollCalendarView;

import java.util.ArrayList;

public interface EventsLoaderListener {

    void onLoad(ArrayList<ScheduleEvent> scheduleEvents) throws ScheduleEvent.EventsIntersectionException;

}
