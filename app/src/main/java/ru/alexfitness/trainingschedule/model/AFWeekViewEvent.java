package ru.alexfitness.trainingschedule.model;

import android.graphics.Color;

import com.alamkanak.weekview.WeekViewEvent;

import ru.alexfitness.trainingschedule.util.SUID;

public class AFWeekViewEvent extends WeekViewEvent {

    private String uid;
    private String client;
    private String training;
    //private AFWeekViewEventType type;
    private boolean writtenOff = false;
    private Trainer trainer;

    public AFWeekViewEvent(){
        this.setId(SUID.id());
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isWrittenOff() {
        return writtenOff;
    }

    public void setWrittenOff(boolean writtenOff) {
        this.writtenOff = writtenOff;
    }

    public void setAppearence(){
        if(isWrittenOff()){
            setColor(Color.LTGRAY);
        } else {
            setColor(Color.BLUE);
        }
    }

    public String getTraining() {
        return training;
    }

    public void setTraining(String training) {
        this.training = training;
    }
}
