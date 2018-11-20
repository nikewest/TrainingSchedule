package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Card {

    private String hexCode;
    private Client client;

    public static Card buildFromJSON(JSONObject jsonObject) throws JSONException {
        Card card = new Card();
        JSONObject clientJsonObject = (JSONObject) jsonObject.getJSONObject("client");
        card.setClient(Client.buildFromJSON(clientJsonObject));
        card.setHexCode(jsonObject.getString("hexCode"));
        return card;
    }

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
