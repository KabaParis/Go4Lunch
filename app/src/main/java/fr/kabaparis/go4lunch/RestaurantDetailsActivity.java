package fr.kabaparis.go4lunch;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.kabaparis.go4lunch.ui.RestaurantDetailsViewModel;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private RestaurantDetailsViewModel mViewModel;

    private ImageView mDetailsPicture;
    private FloatingActionButton mRestaurantChoice;
    private TextView mRestaurantName;
    private TextView mRestaurantAddress;
    private MaterialButton mButtonCall;
    private MaterialButton mButtonLike;
    private MaterialButton mButtonWebsite;
    private RatingBar mRatingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);


        mViewModel = new RestaurantDetailsViewModel(this); // passing the activity context
        // get the views identified by their attributes

        mDetailsPicture = findViewById(R.id.restaurant_details_picture);
        mRestaurantChoice = findViewById(R.id.button_restaurant_choice);
        mRestaurantName = findViewById(R.id.restaurant_details_name);
        mRestaurantAddress = findViewById(R.id.restaurant_details_address);
        mButtonCall = findViewById(R.id.button_call_detail);
        mButtonLike = findViewById(R.id.button_like_detail);
        mButtonWebsite = findViewById(R.id.button_website_detail);
        mRatingBar = findViewById(R.id.restaurant_details_rating);


        String placeId = getIntent().getStringExtra("place_id");

        mViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                // Pass a valid context to the ViewModel constructor
                return (T) new RestaurantDetailsViewModel(getApplicationContext());
            }
        }).get(RestaurantDetailsViewModel.class);

        mViewModel.setContext(this);
        Log.d("restaurant_id", placeId);
        mViewModel.fetchPlaceDetails(placeId);

        mViewModel.getPlace().observe(this, new Observer<Place>() {
            @Override
            public void onChanged(Place restaurant) {
                if (restaurant != null) {
                    mRestaurantName.setText(restaurant.getName());
                    mRestaurantAddress.setText(restaurant.getAddress());
                    if (restaurant.getRating() != null) {
                        mRatingBar.setRating(restaurant.getRating().floatValue() * 3 / 5f);
                    }
                }
            }
        });

        mViewModel.getPhoto().observe(this, new Observer<FetchPhotoResponse>() {

            @Override
            public void onChanged(FetchPhotoResponse fetchPhotoResponse) {
                Glide.with(RestaurantDetailsActivity.this).load(fetchPhotoResponse.getBitmap())
                        .centerCrop().into(mDetailsPicture);
            }
        });


        // Set MaterialButton Call
        // When click on Call Button, show Restaurant number if existing, if not existing, show an error message
        mButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Place restaurant = mViewModel.getPlace().getValue();
                if (restaurant != null) {
                    String phoneNumber = restaurant.getPhoneNumber();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        // If the phone number exists, show it
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    } else {
                        // If the phone number doesn't exist, show an error message
                        Toast.makeText(RestaurantDetailsActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Set MaterialButton Website
        // When click on Website Button, send to restaurant's Website if available
        mButtonWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri websiteUri = mViewModel.getPlace().getValue().getWebsiteUri();
                if (websiteUri != null) {
                    // If the website URL exists, open it in a web browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, websiteUri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        // If there's no app that can handle the intent, show an error message
                        Toast.makeText(RestaurantDetailsActivity.this, "No app can handle this action", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If the website URL doesn't exist, show an error message
                    Toast.makeText(RestaurantDetailsActivity.this, "Website not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set MaterialButton Like
        // When click on Like Button, add or remove restaurant from favorites
            mButtonLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewModel.onLikeButtonClicked();
                }
            });
        }
}


