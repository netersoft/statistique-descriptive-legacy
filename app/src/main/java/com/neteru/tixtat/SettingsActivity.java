package com.neteru.tixtat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialisation de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Chargement du fragment des préférences
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new mPreferenceFragmentCompat()).commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings_str);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class mPreferenceFragmentCompat extends PreferenceFragmentCompat
    {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Ajout du template des préférences
            setPreferencesFromResource(R.xml.app_preferences, rootKey);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity);
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        overridePendingTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity);

        return true;
    }

}
