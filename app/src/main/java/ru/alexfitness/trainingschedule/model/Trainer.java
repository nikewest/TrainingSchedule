package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Trainer implements Serializable{

    private String uid;
    private String name;
    private String surname;
    private String fathername;

    public static Trainer buildFromJSON(JSONObject jsonObject) throws JSONException {
        Trainer trainer = new Trainer();
        trainer.uid = jsonObject.getString("id");
        JSONObject personInfo = jsonObject.getJSONObject("person");
        trainer.name = personInfo.getString("name");
        trainer.surname = personInfo.getString("surname");
        trainer.fathername = personInfo.getString("fathername");
        return trainer;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFathername() {
        return fathername;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    @Override
    public String toString() {
        return getSurname() + " " + getName() + " " + getFathername();
    }
}
