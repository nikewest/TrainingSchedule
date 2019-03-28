package ru.alexfitness.trainingschedule.model;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;

import ru.alexfitness.trainingschedule.util.Converter;

public class ScheduleEvent {

    private String uid;

    private Calendar start;
    private Calendar end;

    private String name;
    private String description;

    private Client client;
    private Trainer trainer;
    private Training training;
    private boolean writtenOff;

    private ScheduleEvent(){}

    private int color = Color.WHITE;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public Training getTraining() {
        return training;
    }

    public void setTraining(Training training) {
        this.training = training;
    }

    public boolean isWrittenOff() {
        return writtenOff;
    }

    public void setWrittenOff(boolean writtenOff) {
        this.writtenOff = writtenOff;
    }

    public int getPeriodIndex(){
        return getStart().get(Calendar.YEAR) * 1000 + getStart().get(Calendar.DAY_OF_YEAR);
    }

    public static ScheduleEvent buildFromJSON(JSONObject jsonObject) throws JSONException, ParseException {
        ScheduleEvent scheduleEvent = new ScheduleEvent();
        scheduleEvent.setUid(jsonObject.getString("uid"));
        scheduleEvent.setClient(Client.buildFromJSON(jsonObject.getJSONObject("client")));
        scheduleEvent.setTraining(Training.buildFromJSON(jsonObject.getJSONObject("training")));
        scheduleEvent.setStart(Converter.calendarFromString(jsonObject.getString("start")));
        scheduleEvent.setEnd(Converter.calendarFromString(jsonObject.getString("end")));
        scheduleEvent.setWrittenOff(jsonObject.getBoolean("writtenoff"));
        scheduleEvent.setName(scheduleEvent.getClient().getName() + '\n' + scheduleEvent.getTraining().getTrainingName());
        return scheduleEvent;
    }
}
