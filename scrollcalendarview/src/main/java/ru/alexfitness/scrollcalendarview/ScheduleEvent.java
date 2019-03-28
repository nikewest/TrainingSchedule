package ru.alexfitness.scrollcalendarview;

import android.graphics.Color;

import java.util.Date;

public class ScheduleEvent {

    private String uid;
    private long id;
    private Date start;
    private Date end;
    private String name;
    private String description;
    private int color = Color.WHITE;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static class EventsIntersectionException extends Exception {
        public EventsIntersectionException() {
            super("Events intersection found!");
        }
    }

    public ScheduleEvent(String uid, Date start, Date end, String name, String description){
        this.uid = uid;
        this.start = start;
        this.end = end;
        this.name = name;
        this.description = description;

        if(!checkDates()){
            throw new IllegalArgumentException();
        }
    }

    public ScheduleEvent(long id, Date start, Date end, String name, String description) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.name = name;
        this.description = description;

        if(!checkDates()){
            throw new IllegalArgumentException();
        }
    }

    public boolean checkDates(){
        if(start!=null && end!=null){
            if(start.after(end) || start.getYear()!=end.getYear() || start.getMonth()!=end.getMonth() || start.getDate()!=end.getDate()){
                return false;
            }
        }
        return true;
    }

    public static boolean eventsIntersect(ScheduleEvent scheduleEvent1, ScheduleEvent scheduleEvent2){
        if(scheduleEvent1 == scheduleEvent2){
            return false;
        }
        return scheduleEvent2.getEnd().after(scheduleEvent1.getStart()) && scheduleEvent1.getEnd().after(scheduleEvent2.getStart());
    }
}
