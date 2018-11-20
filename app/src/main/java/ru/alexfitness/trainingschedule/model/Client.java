package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {

    private String uid;
    private String name;

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

    public static Client buildFromJSON(JSONObject jsonObject) throws JSONException {
        Client client = new Client();
        client.setUid(jsonObject.getString("clientId"));
        client.setName(jsonObject.getString("clientName"));
        return client;
    }

}
