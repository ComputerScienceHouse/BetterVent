package edu.rit.csh.bettervent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class SettingsFragment extends Fragment {

    //Preferences file.
    SharedPreferences appSettings;

    EditText filterKeywords;
    String filterKeywordsString = "edu.rit.csh.bettervent.filterkeywords";
    EditText freeColor;
    String freeColorString = "edu.rit.csh.bettervent.freecolor";
    EditText reservedColor;
    String reservedColorString = "edu.rit.csh.bettervent.reservedcolor";
    EditText password;
    String passwordString = "edu.rit.csh.bettervent.password";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded Settings Fragment.");
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        appSettings = getContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        filterKeywords = view.findViewById(R.id.filtering_keywords_prompt);
        freeColor = view.findViewById(R.id.free_color);
        reservedColor = view.findViewById(R.id.reserved_color);
        password = view.findViewById(R.id.password_prompt);

        // Log the current settings and load them into the settings fields.
        infoPrint("Keywords: " + appSettings.getString(filterKeywordsString, "") + "\n" +
                  "ColorFree: " + appSettings.getString(freeColorString, "") + "\n" +
                  "ColorReserved: " + appSettings.getString(reservedColorString, "") + "\n" +
                  "Password: [REDACTED]"
        );

        filterKeywords.setText(appSettings.getString(filterKeywordsString, ""));
        freeColor.setText(appSettings.getString(freeColorString, ""));
        reservedColor.setText(appSettings.getString(reservedColorString, ""));
        password.setText(appSettings.getString(passwordString, ""));


        // Update settings when they lose focus
        (view.findViewById(R.id.filtering_keywords_prompt)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost run code.
                if (!hasFocus) {
                    appSettings.edit().putString(filterKeywordsString, filterKeywords.getText().toString()).apply();
                }
            }
        });

        (view.findViewById(R.id.free_color)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost run code.
                if (!hasFocus) {
                    appSettings.edit().putString(freeColorString, freeColor.getText().toString()).apply();
                }
            }
        });

        (view.findViewById(R.id.reserved_color)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost run code.
                if (!hasFocus) {
                    appSettings.edit().putString(reservedColorString, reservedColor.getText().toString()).apply();
                }
            }
        });

        (view.findViewById(R.id.password_prompt)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost run code.
                if (!hasFocus) {
                    appSettings.edit().putString(passwordString, password.getText().toString()).apply();
                }
            }
        });

        return view;
    }

    private void infoPrint(String info) {
        System.out.println("SETT_: " + info);
    }
}