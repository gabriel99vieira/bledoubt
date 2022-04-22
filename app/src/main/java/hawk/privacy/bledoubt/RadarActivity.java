package hawk.privacy.bledoubt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.mapbox.mapboxsdk.Mapbox;

import hawk.privacy.bledoubt.ui.main.DeviceListFragment;
import hawk.privacy.bledoubt.ui.main.RadarFragment;
import hawk.privacy.bledoubt.ui.main.RadarViewModel;

public class RadarActivity extends AppCompatActivity implements BeaconConsumer, DeviceListFragment.OnListFragmentInteractionListener {

    // Beacon Layouts
    public static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    public static final String EDDYSTONE_TLM_LAYOUT = "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
    public static final String EDDYSTONE_UID_LAYOUT =  "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    public static final String EDDYSTONE_URL_LAYOUT =  "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v";
    public static final String IBEACON_LAYOUT =  "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    // Request Codes
    private static final int SAVE_TO_JSON_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 2;
    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 3;

    // Identifier for background work request.
    private static final String BG_WORK_NAME = "TrajectoryAnalysisWork";

    protected static final String TAG = "[RadarActivity]";

    // Data management for finding beacons
    private BeaconManager beaconManager;
    private BeaconHistory beaconHistory;
    private LocationTracker locationTracker;
    private OuiLookupTable ouiLookup;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    public RadarViewModel radarViewModel;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SAVE_TO_JSON_REQUEST_CODE:
                saveToJsonActivityResult(data.getData());
                break;
            case ENABLE_BLUETOOTH_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK)
                    activateRadar();
                else {
                    deactivateRadar();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activateRadar();
                } else {
                    deactivateRadar();
                }
        }
    }


    /**
     * Enable the BLE scanner and location tracker, collecting data to detect devices.
     * Also enable a background task that attempts to identify malicious trackers.
     */
    private void activateRadar() {
        Log.i(TAG, "Activating Radar");

        if (requestLocationPermissions() && requestBluetoothEnabled()) {
            activateBeaconManager();
            activateLocationTracker();

            PeriodicWorkRequest analyzeTrajectoryRequest = new PeriodicWorkRequest.Builder(
                    HistoryAnalyzer.class, 15, TimeUnit.MINUTES)
                    .addTag(HistoryAnalyzer.TAG)
                    .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    BG_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    analyzeTrajectoryRequest
            );
        }
    }

    private boolean requestBluetoothEnabled() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void activateBeaconManager() {
        if (beaconManager == null) {
            beaconManager = BeaconManager.getInstanceForApplication(this);
            beaconManager.getBeaconParsers().add(new BeaconParser(BeaconType.IBEACON.toString()).setBeaconLayout(IBEACON_LAYOUT));

            BeaconParser tileParser = new ServiceUuidBeaconParser(0xFEED, "Tile");
            beaconManager.getBeaconParsers().add(tileParser);
            BeaconParser chipoloParser = new ServiceUuidBeaconParser(0xFE65, "Chipolo");
            beaconManager.getBeaconParsers().add(chipoloParser);
            BeaconParser spotParser = new ServiceUuidBeaconParser(0xFF00, "Spot");
            beaconManager.getBeaconParsers().add(spotParser);
            BeaconParser airTagParser = new AirTagBeaconParser();
            beaconManager.getBeaconParsers().add(airTagParser);

            Notification persistentNotification = Notifications.getForegroundScanningNotification(this);
            beaconManager.enableForegroundServiceScanning(persistentNotification, Notifications.FOREGROUND_NOTIFICATION_ID);
            beaconManager.setEnableScheduledScanJobs(false);
            beaconManager.setBackgroundBetweenScanPeriod(0);
            beaconManager.setBackgroundScanPeriod(1100);
            beaconManager.bind(this);
        }
    }

    private void activateLocationTracker() {
        if (locationTracker == null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationTracker = new LocationTracker(this);
            Log.d(TAG, "Turning on location");
        }
    }

    private boolean requestLocationPermissions() {
        boolean missingAnyPermission = false;
        boolean needsAnyRationale = false;
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        for (String permission_name : permissions) {
            if (checkSelfPermission(permission_name) != PackageManager.PERMISSION_GRANTED) {
                missingAnyPermission = true;
                if (shouldShowRequestPermissionRationale(permission_name)) {
                    needsAnyRationale = true;
                }
            }
        }
        if (missingAnyPermission) {
            if (needsAnyRationale) {
                showPermissionRationale();
            } else {
                requestPermissions(permissions, LOCATION_PERMISSIONS_REQUEST_CODE);
            }
            return false;
        }
        return true;
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_text)
                .setPositiveButton(R.string.permission_rationale_accept, (dialog, which) ->
                        requestPermissions(new String[]{
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSIONS_REQUEST_CODE
                        )
                )
                .setNegativeButton(R.string.permission_rationale_dismiss, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Disable the BLE scanner and location tracker, stopping the collection of data.
     */
    private void deactivateRadar() {
        if (beaconManager != null) {
            beaconManager.unbind(this);
            beaconManager = null;
        }
        locationTracker = null;

        WorkManager.getInstance(context).cancelUniqueWork(BG_WORK_NAME);
    }

    /**
     * Save the BeaconHistory as a json file at the given URI
     * @param output_json_uri
     */
    private void saveToJsonActivityResult(Uri output_json_uri) {
        boolean success = true;
        try (OutputStream out = getContentResolver().openOutputStream(output_json_uri)) {
            out.write(beaconHistory.toJSONObject().toString().getBytes());
        } catch (IOException | JSONException e) {
            success = false;
        }
        if (success) {
            Toast.makeText(context, getString(R.string.failed_json_save_toast, output_json_uri), Toast.LENGTH_LONG);
        } else {
            Toast.makeText(context, getString(R.string.save_to_json_toast, output_json_uri), Toast.LENGTH_LONG);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    storeBeacon(beacon);
                }
                //updateRecyclerView();
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_api_key));
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        beaconHistory = BeaconHistory.getAppBeaconHistory(this);
        context = getApplicationContext();
        ouiLookup = new OuiLookupTable(this);

        radarViewModel = new ViewModelProvider(this).get(RadarViewModel.class);
        radarViewModel.getIsRadarEnabled().observe(this, isEnabled -> {
            if (isEnabled) {
                activateRadar();
            } else {
                deactivateRadar();
            }
        });

        requestLocationPermissions();
        Notifications.createNotificationChannels(context);

        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, RadarFragment.class, null)
                    .commit();
        }
        initToolbar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deactivateRadar();
    }

    protected void storeBeacon(Beacon beacon) {
        if (locationTracker != null && beaconManager != null) {
            Log.i(TAG, "Parser " + beacon.getParserIdentifier() + ". Mac " + beacon.getBluetoothAddress());
            Location loc = locationTracker.getLastLocation();
            String oui = ouiLookup.lookupOui(beacon.getBluetoothAddress());
            if (loc != null) {
                beaconHistory.add(beacon, BeaconType.IBEACON, new BeaconDetection(beacon, new Date(), loc));
                Log.d(TAG, " " + beaconHistory.getTrajectory(beacon.getBluetoothAddress()).size());
            } else {
                Log.i(TAG, "No location data. Parser cannot store beacon.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_history:
                confirmAndDeleteHistory();
                return true;
            case R.id.export_history:
                requestUriForSaveToJson();
                return true;
            case R.id.force_analyze:
                HistoryAnalyzer.analyze(this, new HistoryAnalyzer.TopologicalClassifier(60,300,300));
                requestUriForSaveToJson();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmAndDeleteHistory() {
        final RadarActivity activity = this;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_text)
                .setPositiveButton(R.string.confirm_delete_accept, (dialog, which) -> activity.beaconHistory.clearAll())
                .setNegativeButton(R.string.confirm_delete_dismiss, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void requestUriForSaveToJson() {
        Log.i(TAG, beaconHistory.toString());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/json");
        String suggestedName = "log.json";
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);
        startActivityForResult(intent, SAVE_TO_JSON_REQUEST_CODE);
    }



    @Override
    public void onListFragmentInteraction(@Nullable DeviceMetadata item) {
        Toast.makeText(context, "Clicked item " + item.name, Toast.LENGTH_LONG);
    }
}
