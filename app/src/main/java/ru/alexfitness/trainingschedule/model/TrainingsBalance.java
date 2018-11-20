package ru.alexfitness.trainingschedule.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class TrainingsBalance implements Serializable {

    private String trainingUid;
    private String trainingName;
    private String clientUid;
    private String clientName;
    private int balance;

    public static TrainingsBalance buildFromJSON(JSONObject jsonObject) throws JSONException {
        TrainingsBalance trainingsBalance = new TrainingsBalance();
        trainingsBalance.setClientUid(jsonObject.getString("clientUid"));
        trainingsBalance.setClientName(jsonObject.getString("clientName"));
        trainingsBalance.setTrainingUid(jsonObject.getString("trainingUid"));
        trainingsBalance.setTrainingName(jsonObject.getString("trainingName"));
        trainingsBalance.setBalance(jsonObject.getInt("balance"));
        return trainingsBalance;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public String getTrainingUid() {
        return trainingUid;
    }

    public void setTrainingUid(String trainingUid) {
        this.trainingUid = trainingUid;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }
}
