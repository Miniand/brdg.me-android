package com.miniand.brdgme;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by beefsack on 17/01/15.
 */
public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsFragment fragment = new SettingsFragment();
        fragment.setLogOutHandler(new SettingsFragment.LogOutHandler() {
            @Override
            public void HandleLogOut() {
                Brdgme.logOut();
            }
        });
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        public static interface LogOutHandler {
            void HandleLogOut();
        }

        private LogOutHandler logOutHandler;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            findPreference("log_out_button").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (logOutHandler != null) {
                        logOutHandler.HandleLogOut();
                    }
                    return false;
                }
            });
        }

        public void setLogOutHandler(LogOutHandler logOutHandler) {
            this.logOutHandler = logOutHandler;
        }
    }
}
