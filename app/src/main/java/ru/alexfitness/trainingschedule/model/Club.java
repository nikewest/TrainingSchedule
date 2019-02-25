package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Club {
    private String apiUrl;
    private String name;
    private String GUID;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public static Club buildFromJSON(JSONObject jsonObject) throws JSONException {
        Club club = new Club();
        club.setGUID(jsonObject.getString("GUID"));
        club.setApiUrl(jsonObject.getString("apiUrl"));
        club.setName(jsonObject.getString("name"));
        return club;
    }

    @Override
    public String toString() {
        return name;
    }
}
