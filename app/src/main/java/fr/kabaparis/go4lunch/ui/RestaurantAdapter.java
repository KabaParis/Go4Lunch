package fr.kabaparis.go4lunch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import fr.kabaparis.go4lunch.R;
import fr.kabaparis.go4lunch.RestaurantDetailsActivity;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final PlacesClient placesClient;
    private Context context;
    private List<Place> restaurants;
    private LatLng userLocation;

    public RestaurantAdapter(Context context) {
        this.context = context;
        placesClient = Places.createClient(context);
        this.restaurants = new ArrayList<>();
    }

    public void setRestaurants(List<Place> restaurants, LatLng userLocation) {
        this.restaurants = restaurants;
        this.userLocation = userLocation;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_list_items, parent, false);
        return new RestaurantViewHolder(view);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Place restaurant = restaurants.get(position);
        holder.nameTextView.setText(restaurant.getName());
        holder.addressTextView.setText(restaurant.getAddress());
        holder.openingHoursTextView.setText((CharSequence) restaurant.getOpeningHours());
        if(restaurant.getRating() == null) {
            holder.ratingBar.setVisibility(View.INVISIBLE);
        }
        else {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(restaurant.getRating().floatValue()*3/5f);
            Log.d("rating", "onBindViewHolder: " + restaurant.getRating());
        }


        if (restaurant.getPhotoMetadatas() != null && !restaurant.getPhotoMetadatas().isEmpty()) {
            FetchPhotoRequest builder = FetchPhotoRequest.builder(restaurant.getPhotoMetadatas().get(0)).build();
            placesClient.fetchPhoto(builder).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                @Override
                public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                    Glide.with(context).load(fetchPhotoResponse.getBitmap()).centerCrop().into(holder.pictureImageView);
                }
            });
        }

        // Calculate distance to the restaurant
        Location restaurantLocation = new Location("");
        restaurantLocation.setLatitude(restaurant.getLatLng().latitude);
        restaurantLocation.setLongitude(restaurant.getLatLng().longitude);
        int distance = 0;
        Location user = new Location("");
        user.setLatitude(userLocation.latitude);
        user.setLongitude(userLocation.longitude);

        distance = (int) user.distanceTo(restaurantLocation);



        holder.distanceTextView.setText(context.getString(R.string.restaurant_distance, distance));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RestaurantDetailsActivity.class);
                intent.putExtra("place", restaurant);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RestaurantDetailsActivity.class);
                intent.putExtra("rating", restaurant.getRating());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        // Declare
        TextView nameTextView;
        TextView addressTextView;
        TextView openingHoursTextView;
        ImageView pictureImageView;
        TextView distanceTextView;
        TextView attendeesNumberTextView;
        ImageView attendeesIconImageView;
        RatingBar ratingBar;

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            //Initialize
            nameTextView = itemView.findViewById(R.id.restaurant_name);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
            openingHoursTextView = itemView.findViewById(R.id.restaurant_opening_hours);
            pictureImageView = itemView.findViewById(R.id.restaurant_picture);
            distanceTextView = itemView.findViewById(R.id.restaurant_distance);
            attendeesNumberTextView = itemView.findViewById(R.id.attendees_number);
            attendeesIconImageView = itemView.findViewById(R.id.attendees_icon);
            ratingBar = itemView.findViewById(R.id.restaurant_rating);
        }
        public void setRating(float rating) {
            ratingBar.setRating(rating);
        }

    }
    public interface OnItemClickListener {
        void onItemClick(Place restaurant);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}


