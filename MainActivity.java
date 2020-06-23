package app.phedev.eramasjid.view;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.iid.FirebaseInstanceId;

import app.phedev.eramasjid.R;
import app.phedev.eramasjid.databinding.ActivityMainBinding;
import app.phedev.eramasjid.helper.AppPreference;
import app.phedev.eramasjid.helper.StaticValue;
import app.phedev.eramasjid.ui.side.ActionBottomFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }
        setContentView(binding.getRoot());
        binding.navView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        BadgeDrawable badgeDrawable = binding.navView.getOrCreateBadge(R.id.navigation_notifications);

        if (!isGooglePlayServicesAvailable(this)){
            return;
        }

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getNotifCount();

        mainViewModel.getNotifData().observe(this, integer -> {
            if (integer==null){
                badgeDrawable.setVisible(false);
                return;
            }
            if (integer == 0){
                badgeDrawable.setVisible(false);
            } else {
                badgeDrawable.setNumber(integer);
                badgeDrawable.setVisible(true);
            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);

        AppPreference appPreference = new AppPreference(getApplicationContext());

        if (getIntent().hasExtra(StaticValue.KEY_MASJID)){
            navController.navigate(R.id.navigation_eramasjid);
        }


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (appPreference.getMasjidId()==0){
                BottomSheetDialogFragment actionFrag = new ActionBottomFragment();
                actionFrag.show(getSupportFragmentManager(), actionFrag.getTag());
            }
            if (destination.getId()==R.id.navigation_notifications){
                mainViewModel.setNotifView();
                badgeDrawable.setVisible(false);
            }
        });



    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if (!isGooglePlayServicesAvailable(this)){
                if(googleApiAvailability.isUserResolvableError(status)) {
                    googleApiAvailability.getErrorDialog(activity, status, 2404).show();
                }
            }
            return false;
        }
        return true;
    }


}
