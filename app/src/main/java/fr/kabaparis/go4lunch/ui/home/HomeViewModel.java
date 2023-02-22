package fr.kabaparis.go4lunch.ui.home;

import static java.security.AccessController.getContext;

import static fr.kabaparis.go4lunch.ui.home.HomeFragment.REQUEST_CODE_LOCATION_PERMISSION;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fr.kabaparis.go4lunch.R;

public class HomeViewModel extends ViewModel {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Application application;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    public HomeViewModel(@NonNull Application application) {
        this.application = application; // store the application context in the field
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        Places.initialize(application, application.getString(R.string.google_api_key));
    }

    public void requestLocationPermission(Activity activity) {
        if (!isLocationPermissionGranted(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void searchForPlacesNearby(LatLng location) {
        // Create a new Places client
        PlacesClient placesClient = Places.createClient(application);

        // Define the search parameters
        String type = "restaurant";
        int radius = 1000; // in meters
        LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(location.latitude - 0.1, location.longitude - 0.1))
                .include(new LatLng(location.latitude + 0.1, location.longitude + 0.1))
                .build();

        // Check for location permission using the application context
        if (ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission here or show a message to the user
            return;
        }

        // Create the Places API request
        List<String> placesType = new ArrayList<>();
        placesType.add(type);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placesType)
                .setLocationBias(RectangularBounds.newInstance(bounds))
                .setFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .build();

 /*       List<Place.Field> placeFields = Arrays.asList((Place.Field.values()));
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields)
                .setLocationBias(new LocationBias.Builder().setLooseness(0.1f).setLatLngBounds(bounds).build())
                .build();
*/

        // Use the fused location provider client to get the current location and pass it to the Places API request
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    request = request.newBuilder().setLocation(location).build();
                    if (ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Request permission here
                        ActivityCompat.requestPermissions(new Activity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                        return;

                    }
                    Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);
                    task.addOnSuccessListener(new OnSuccessListener<FindCurrentPlaceResponse>() {
                        @Override
                        public void onSuccess(FindCurrentPlaceResponse response) {
                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place place = placeLikelihood.getPlace();
                                LatLng placeLocation = place.getLatLng();
                                googleMap.addMarker(new MarkerOptions().position(placeLocation).title(place.getName()));
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the error.
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                int statusCode = apiException.getStatusCode();
                                // Handle the error status code as required.
                            }

                        }
                    });
                }
            }
        });
    }
}



