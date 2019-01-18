package ru.alexfitness.trainingschedule.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;
import ru.alexfitness.trainingschedule.util.AFPreferenceActivity;

public class SettingsActivity extends AFPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnableAuthEndTimeOut(false);

        addPreferencesFromResource(R.xml.prefs);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        findPreference(getString(R.string.pref_schedule_time_step_key)).setOnPreferenceChangeListener(this);

        EditTextPreference preferenceServerAddress = (EditTextPreference) findPreference(getString(R.string.pref_service_address_key));
        preferenceServerAddress.setSummary(preferenceServerAddress.getText());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(key.equals(getString(R.string.pref_service_address_key))) {
            preference.setSummary(((EditTextPreference) preference).getText());
            ApiUrlBuilder.setHostUrl(((EditTextPreference) preference).getText());
        }
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(getString(R.string.pref_schedule_time_step_key))){
            int checkValue = -1;
            try {
                checkValue = Integer.parseInt((String) newValue);
            } catch (NumberFormatException ex){
            }
            if((checkValue <= 0)||(checkValue > 60)||(60 % checkValue != 0 )){
                Toast.makeText(this, R.string.toast_msg_edit_pref_schedule_time_step, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }
}
