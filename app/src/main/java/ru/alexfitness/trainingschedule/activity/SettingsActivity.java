package ru.alexfitness.trainingschedule.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ru.alexfitness.trainingschedule.BuildConfig;
import ru.alexfitness.trainingschedule.R;
import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        findPreference(getString(R.string.pref_schedule_time_step_key)).setOnPreferenceChangeListener(this);

        EditTextPreference preferenceServerAddress = (EditTextPreference) findPreference(getString(R.string.pref_service_address_key));
        preferenceServerAddress.setSummary(preferenceServerAddress.getText());

        Preference preferenceVersion = findPreference("pref_version");
        preferenceVersion.setSummary(BuildConfig.VERSION_NAME);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_service_address_key))) {
            Preference preference = findPreference(key);
            preference.setSummary(sharedPreferences.getString(key, ""));
            ApiUrlBuilder.setHostUrl(sharedPreferences.getString(key, ""));
        }
        if(key.equals(getString(R.string.pref_service_login_key))) {
            ApiUrlBuilder.setLogin(sharedPreferences.getString(key, ""));
        }
        if(key.equals(getString(R.string.pref_service_pwd_key))) {
            ApiUrlBuilder.setPwd(sharedPreferences.getString(key, ""));
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
