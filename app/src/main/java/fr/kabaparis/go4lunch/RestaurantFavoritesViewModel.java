/*
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

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addRestaurant(String name, String type, String address, Boolean isFavorite) {
        // Create a new restaurant with the given name, type, and address
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("name", name);
        restaurant.put("type", type);
        restaurant.put("address", address);
        restaurant.put("isFavorite", false);

        // Add the new restaurant to the Firestore collection
        FirebaseFirestore.getInstance().collection("favorites")
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
        FirebaseFirestore.getInstance().collection("favorites")
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
        FirebaseFirestore.getInstance().collection("favorites")
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
*/
