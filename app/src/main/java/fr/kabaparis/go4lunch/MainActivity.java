package fr.kabaparis.go4lunch;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fr.kabaparis.go4lunch.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    //FOR DESIGN
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 6 - Configure all views
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.nav_home, R.id.nav_gallery)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        binding.activityMainNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.sign_out_button) {
                    AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    signOut();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "logout failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                public void onClick(View v) {
                                    if (v.getId() == R.id.sign_out_button) {
                                        AuthUI.getInstance()
                                                .signOut(MainActivity.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        // user is now signed out
                                                        startActivity(new Intent(String.valueOf(MainActivity.this)));
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
                return true;
            }
        });
        // Authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.hot_bowl_white)
                .setTheme(R.style.login_theme)
                .setAlwaysShowSignInMethodScreen(true)
                .setIsSmartLockEnabled(false)
                .build();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            signInLauncher.launch(signInIntent);
        }
        else {
            configureUserInfo();
        }
    }

    private void configureUserInfo() {
        final View headerView = binding.activityMainNavView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.header_user_name);
        TextView userEmail = headerView.findViewById(R.id.header_user_email);
        ImageView userPicture = headerView.findViewById(R.id.header_user_picture);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (Objects.requireNonNull(user).getPhotoUrl() != null) {
                Glide.with(MainActivity.this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(userPicture);
            }
            if (user.getDisplayName() != null) {
                userName.setText(user.getDisplayName());
            }

            if (user.getEmail() != null) {
                userEmail.setText((user.getEmail()));
            }
        }
    }

    public void signOut() {
        startActivity(new Intent(String.valueOf(MainActivity.this)));
        finish();
    }

    @Override
    public void onBackPressed() {
        // 5 - Handle back click to close menu
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // 4 - Handle Navigation Item Click
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_gallery:
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            default:
                break;
        }
        binding.drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }
    // ---------------------
    // CONFIGURATION
    // ---------------------

    // 1 - Configure Toolbar
    private void configureToolBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //     getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_view_list);
    }

    // 2 - Configure Drawer Layout
    private void configureDrawerLayout() {
        toggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        binding.drawer.addDrawerListener(toggle);
    }

    // 3 - Configure NavigationView
    private void configureNavigationView() {
//        binding.activityMainNavView.setNavigationItemSelectedListener(this);
    }

    ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }

                private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
                    Glide.with(MainActivity.this).load(R.drawable.blurred_people_eating_gaussien).circleCrop().into((ImageView)
                            binding.activityMainNavView.getHeaderView(0).findViewById(R.id.header_user_picture));
                    IdpResponse response = result.getIdpResponse();
                    // Successfully signed in
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        // Name, email address, and profile photo Url
                       configureUserInfo();
                    } else {
                        if (response == null) {
                            // No user signed in
                            return;
                        }
                    }
                }
    });
}

/*

 private void signOut() {
     signOut(MainActivity.this);
 }

    public void signOut(MainActivity mainActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout ? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        builder.show();

    }
*/



