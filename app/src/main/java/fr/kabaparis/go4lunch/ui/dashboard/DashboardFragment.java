package fr.kabaparis.go4lunch.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.Place;

import java.util.List;

import fr.kabaparis.go4lunch.RestaurantDetailsActivity;
import fr.kabaparis.go4lunch.databinding.FragmentDashboardBinding;
import fr.kabaparis.go4lunch.ui.RestaurantAdapter;
import fr.kabaparis.go4lunch.ui.home.HomeViewModel;

public class DashboardFragment extends Fragment implements RestaurantAdapter.OnItemClickListener {

    private HomeViewModel homeViewModel;
    private FragmentDashboardBinding binding;

    private RecyclerView nearbyRecyclerView;
    private RestaurantAdapter nearbyAdapter;

    @Override
    public void onItemClick(Place restaurant) {
        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        intent.putExtra("place_id", restaurant.getId());
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the nearby ListView and adapter
        nearbyRecyclerView = binding.nearbyRecyclerView;
  //      nearbyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        nearbyAdapter = new RestaurantAdapter(getContext());
        nearbyRecyclerView.setAdapter(nearbyAdapter);

        // Set the layout manager for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        nearbyRecyclerView.setLayoutManager(layoutManager);

        // Get a reference to the DashboardViewModel
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Set the click listener for the RestaurantAdapter
        nearbyAdapter.setOnItemClickListener(this);

        // Observe the nearbyPlaces LiveData from the DashboardViewModel
        homeViewModel.nearbyPlaces.observe(getViewLifecycleOwner(), new Observer<List<Place>>() {
            @Override
            public void onChanged(List<Place> restaurants) {
                // Update the nearbyRestaurants ArrayList and notify the adapter
                nearbyAdapter.setRestaurants(restaurants, homeViewModel.userLocation);
                nearbyAdapter.notifyDataSetChanged();
            }
        });

        return root;
    }
}







