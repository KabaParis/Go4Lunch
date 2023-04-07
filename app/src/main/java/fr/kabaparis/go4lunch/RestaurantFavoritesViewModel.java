package fr.kabaparis.go4lunch;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class RestaurantFavoritesViewModel extends ViewModel {
    private final MutableLiveData<List<Restaurant>> favoriteRestaurantsLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void retrieveFavoriteRestaurants() {
        // Get a reference to the Firestore collection
        CollectionReference restaurantsRef = FirebaseFirestore.getInstance().collection("restaurants");

        // Create a query to retrieve the favorite restaurants
        Query favoriteRestaurantsQuery = restaurantsRef.whereEqualTo("isFavorite", true);

        // Execute the query and listen for the results
        favoriteRestaurantsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Convert the result to a list of Restaurant objects
                List<Restaurant> favoriteRestaurants = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Restaurant restaurant = document.toObject(Restaurant.class);
                    favoriteRestaurants.add(restaurant);
                }

                // Update the LiveData object with the list of favorite restaurants
                favoriteRestaurantsLiveData.setValue(favoriteRestaurants);
            }
        });
    }

    public LiveData<List<Restaurant>> getFavoriteRestaurantsLiveData() {
        return favoriteRestaurantsLiveData;
    }

    public void addRestaurant(String name, String type, String address) {
        // Create a new restaurant with the given name, type, and address
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("name", name);
        restaurant.put("type", type);
        restaurant.put("address", address);
        restaurant.put("isFavorite", false);

        // Add the new restaurant to the Firestore collection
        FirebaseFirestore.getInstance().collection("restaurants")
                .add(restaurant)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void updateFavoriteStatus(String restaurantId, boolean isFavorite) {
        // Update the "isFavorite" field of the specified restaurant document
        FirebaseFirestore.getInstance().collection("restaurants")
                .document(restaurantId)
                .update("isFavorite", isFavorite)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Restaurant updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating restaurant", e);
                    }
                });
    }

    public void deleteRestaurant(String restaurantId) {
// Delete the specified restaurant document from the Firestore collection
        FirebaseFirestore.getInstance().collection("restaurants")
                .document(restaurantId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Restaurant deleted successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting restaurant", e);
                    }
                });
    }
}

