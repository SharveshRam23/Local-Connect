package com.example.localconnect.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "local_connect_prefs";
    private static final String KEY_IS_PROVIDER_LOGIN = "is_provider_login";
    private static final String KEY_PROVIDER_NAME = "provider_name";
    private static final String KEY_PROVIDER_PINCODE = "provider_pincode";
    private static final String KEY_PROVIDER_ID = "provider_id";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createProviderSession(String id, String name, String pincode) {
        editor.putBoolean(KEY_IS_PROVIDER_LOGIN, true);
        editor.putString(KEY_PROVIDER_ID, id);
        editor.putString(KEY_PROVIDER_NAME, name);
        editor.putString(KEY_PROVIDER_PINCODE, pincode);
        editor.apply();
    }

    public boolean isProviderLoggedIn() {
        return prefs.getBoolean(KEY_IS_PROVIDER_LOGIN, false);
    }

    public String getProviderId() {
        return prefs.getString(KEY_PROVIDER_ID, null);
    }

    public String getProviderName() {
        return prefs.getString(KEY_PROVIDER_NAME, "Provider");
    }

    public String getProviderPincode() {
        return prefs.getString(KEY_PROVIDER_PINCODE, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
