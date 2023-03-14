package fr.kabaparis.go4lunch;

import android.app.Application;

import com.google.android.libraries.places.api.Places;

public class Go4LunchApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Places.initialize(this, getString(R.string.google_places_api_key));
    }
}
