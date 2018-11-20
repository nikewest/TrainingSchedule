package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Training implements Serializable {

    private String uid;
    private String trainingName;

    public static Training buildFromJSON(JSONObject jsonObject) throws JSONException {
        Training training = new Training();
        training.setUid(jsonObject.getString("trainingUid"));
        training.setTrainingName(jsonObject.getString("trainingName"));
        return training;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    @Override
    public String toString() {
        return getTrainingName();
    }
}
