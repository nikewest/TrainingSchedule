package ru.alexfitness.scrollcalendarview;

import java.util.ArrayList;

public interface EventsLoaderListener {

    void onLoad(ArrayList<Event> events) throws Event.EventsIntersectionException;

}
