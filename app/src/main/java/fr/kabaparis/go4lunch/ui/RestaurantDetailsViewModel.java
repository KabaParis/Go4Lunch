package fr.kabaparis.go4lunch.ui;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.libraries.places.api.model.Place;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import fr.kabaparis.go4lunch.RestaurantDetailsActivity;

public class RestaurantDetailsViewModel extends ViewModel {

    private MutableLiveData<Place> placeLiveData = new MutableLiveData<>();

    private Context context;
    private MutableLiveData<FetchPhotoResponse> photoLiveData = new MutableLiveData<>();


    public RestaurantDetailsViewModel(Context context) {
        this.context = context;
   }

    public MutableLiveData<Place> getPlaceLiveData() {
        return placeLiveData;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void fetchPlaceDetails(String placeId) {
        PlacesClient placesClient = Places.createClient(context);
        if (placeId != null) {
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, Arrays.asList(Place.Field.ID, Place.Field.NAME,
                            Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.TYPES,
                            Place.Field.RATING,
                            Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER))
                    .build();

            placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                @Override
                public void onSuccess(FetchPlaceResponse response) {
                    Place place = response.getPlace();
                    placeLiveData.postValue(place);
                    if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                        FetchPhotoRequest builder = FetchPhotoRequest.builder(place.getPhotoMetadatas().get(0)).build();
                        placesClient.fetchPhoto(builder).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                            @Override
                            public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                                photoLiveData.postValue(fetchPhotoResponse);

                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        int statusCode = apiException.getStatusCode();
                        Log.e("RestaurantDetailsViewModel", "Error getting restaurant details: " + statusCode);
                    }
                }
            });
        }
    }

    public void onLikeButtonClicked() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the restaurant ID
            String restaurantId = getPlace().getValue().getId();

            // Get a reference to the user's favorites collection in Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUser.getUid())
                    .collection("favorites")
                    .document(restaurantId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Toast.makeText(context.getApplicationContext(), "added as favorite", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Document written to: users/" + currentUser.getUid() + "/favorites/" + restaurantId);

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context.getApplicationContext(), "fail adding as favorite", Toast.LENGTH_SHORT).show();
                            Log.w("Error adding favorite", e);
                        }
                    });
        }
    }

    public LiveData<Place> getPlace() {
        return placeLiveData;
    }

    public LiveData<FetchPhotoResponse> getPhoto() {
        return photoLiveData;
    }

}

