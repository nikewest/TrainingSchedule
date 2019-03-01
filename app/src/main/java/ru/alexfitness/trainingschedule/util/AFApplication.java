package ru.alexfitness.trainingschedule.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.alexfitness.trainingschedule.model.Trainer;
import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;

public class AFApplication extends Application  {

    private Trainer trainer;
    private long pauseTimeStamp = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ApiUrlBuilder.setHostUrl(sharedPreferences.getString(getString(R.string.pref_service_address_key), ""));
        ApiUrlBuilder.setLogin(sharedPreferences.getString(getString(R.string.pref_service_login_key),""));
        ApiUrlBuilder.setPwd(sharedPreferences.getString(getString(R.string.pref_service_pwd_key), ""));
    }

    public long getPauseTimeStamp() {
        return pauseTimeStamp;
    }

    public void setPauseTimeStamp(long pauseTimeStamp) {
        this.pauseTimeStamp = pauseTimeStamp;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

}
