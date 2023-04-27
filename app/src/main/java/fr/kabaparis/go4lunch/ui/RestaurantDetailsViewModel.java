package fr.kabaparis.go4lunch.ui;

import static android.content.ContentValues.TAG;

import android.content.Context;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RestaurantDetailsViewModel extends ViewModel {

    private MutableLiveData<Place> placeLiveData = new MutableLiveData<>();

    private Context context;
    private MutableLiveData<FetchPhotoResponse> photoLiveData = new MutableLiveData<>();

    public MutableLiveData<Boolean> mIsInFavorites = new MutableLiveData<>(false);

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
        onLikeButtonClicked(placeId);
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

    public void onLikeButtonClicked(String placeId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            DocumentReference favoritesRef = FirebaseFirestore.getInstance()
                    .collection("users").document(currentUser.getUid()).collection("favorites").document(placeId);

            favoritesRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.exists()) {
                                // Document doesn't exist, add it to the collection
                                mIsInFavorites.setValue(true);

                            } else {
                                // Document exists, remove it from the collection
                                mIsInFavorites.setValue(false);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mIsInFavorites.setValue(false);
                            Log.e(TAG, "Error querying favorites collection", e);
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

    public void setRestaurantToFirestore() {
        // Create a new document with the place ID as the document ID
        Map<String, Object> data = new HashMap<>();
        data.put("placeId", placeLiveData.getValue().getId());
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("favorites")
                .document(Objects.requireNonNull(placeLiveData.getValue().getId())).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mIsInFavorites.setValue(true);
                        Log.d(TAG, "Restaurant added to Firestore");
                        Toast.makeText(context, "Restaurant added to favorites", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding restaurant to Firestore", e);
                        Toast.makeText(context, "Error adding restaurant to favorites", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void deleteRestaurantFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            DocumentReference favoritesRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid())
                    .collection("favorites")
                    .document(placeLiveData.getValue().getId());
            favoritesRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            mIsInFavorites.setValue(false);
                            Log.d(TAG, "Restaurant deleted from Firestore");
                            Toast.makeText(context, "Restaurant removed from favorites", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error deleting restaurant from Firestore", e);
                            Toast.makeText(context, "Error removing restaurant from favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void updateFavorites() {
        if (Boolean.TRUE.equals(mIsInFavorites.getValue())) {
            deleteRestaurantFromFirestore();
        } else {
            setRestaurantToFirestore();
        }
    }
}

