package ru.alexfitness.trainingschedule.scrollCalendarView;

import java.util.ArrayList;

import ru.alexfitness.trainingschedule.model.ScheduleEvent;

public interface EventsLoaderListener {

    void onLoad(ArrayList<ScheduleEvent> scheduleEvents);

}
