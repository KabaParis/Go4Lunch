package fr.kabaparis.go4lunch.ui.home;

import static java.security.AccessController.getContext;

import static fr.kabaparis.go4lunch.ui.home.HomeFragment.REQUEST_CODE_LOCATION_PERMISSION;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
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

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<List<Place>> nearbyPlaces;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        nearbyPlaces = new MutableLiveData<>();

    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public LiveData<List<Place>> searchForPlacesNearby() {
        // Create a new Places client
        PlacesClient placesClient = Places.createClient(getApplication());

        // Create the Places API request
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest
                .builder(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .build();

        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);
        task.addOnSuccessListener(new OnSuccessListener<FindCurrentPlaceResponse>() {
            @Override
            public void onSuccess(FindCurrentPlaceResponse response) {
                List<Place> places = new ArrayList<>();
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Place place = placeLikelihood.getPlace();
                    LatLng placeLocation = place.getLatLng();
                    if (placeLikelihood.getPlace().getTypes().contains(Place.Type.RESTAURANT)) {
                        // Add the restaurant to the list
                        places.add(place);
                        Log.d("SearchPlaces", "Found nearby restaurant: " + place.getName() + " at " + placeLocation);
                        //TODO si je suis dans ligne 75 c'est que j'ai récupéré la liste des restos
                        // Add a marker for the restaurant
                    }
                }
                // Update the value of the LiveData
                nearbyPlaces.postValue(places);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    Log.e("SearchPlaces", "Error getting nearby places: " + statusCode);
                    // Handle the error status code as required.
                }

            }
        });
        return nearbyPlaces;
    }
        // Return the LiveData
        public LiveData<List<Place>> getRestaurantPlacesLiveData () {
            return nearbyPlaces;
        }

}



