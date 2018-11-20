package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Subscription implements Serializable {

    private String uid;
    private String name;

    public static Subscription buildFromJSON(JSONObject jsonObject) throws JSONException {
        Subscription sub = new Subscription();
        sub.setUid(jsonObject.getString("subscriptionUid"));
        sub.setName(jsonObject.getString("subscriptionName"));
        return sub;
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

    @Override
    public String toString() {
        return getName();
    }
}
