package hawk.privacy.bledoubt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RadarActivity extends Activity implements BeaconConsumer {
    public static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    public static final String EDDYSTONE_TLM_LAYOUT = "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
    public static final String EDDYSTONE_UID_LAYOUT =  "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    public static final String EDDYSTONE_URL_LAYOUT =  "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v";
    public static final String IBEACON_LAYOUT =  "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final int SAVE_TO_JSON_REQUEST_CODE = 1;

    protected static final String TAG = "[RadarActivity]";
    private BeaconManager beaconManager;
    private BeaconHistory beaconHistory;
    private LocationTracker locationTracker;
    private DeviceMainMenuViewAdapter recyclerViewAdapter;
    private Notifications notifications;
    private Context context;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SAVE_TO_JSON_REQUEST_CODE:
                saveToJsonActivityResult(data.getData());
                break;
        }
    }

    /**
     * Save the BeaconHistory as a json file at the given URI
     * @param output_json_uri
     */
    private void saveToJsonActivityResult(Uri output_json_uri) {
        try(OutputStream out = getContentResolver().openOutputStream(output_json_uri)) {
            out.write(beaconHistory.toJSONObject().toString().getBytes());
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(context, getString(R.string.save_to_json_toast, output_json_uri),
                Toast.LENGTH_LONG);
    }

    private void requestUriForSaveToJson() {
        Log.i(TAG, beaconHistory.toString());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/json");
        String suggestedName = "log.json";
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);
        startActivityForResult(intent,1);
    }

    private void initUI() {
        setContentView(R.layout.activity_radar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        final Button radarButton = findViewById(R.id.radar_button);
        radarButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                requestUriForSaveToJson();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                notifications.CreateSuspiciousDeviceNotification(context, null);
            }
        });

        initDeviceList();
    }

    private void initDeviceList() {
        List<DeviceMetadata> models = new ArrayList<>();
        recyclerViewAdapter = new DeviceMainMenuViewAdapter(models);
        RecyclerView recyclerView = findViewById(R.id.main_menu_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void updateRecyclerView() {
        this.recyclerViewAdapter.setModels(this.beaconHistory.getMainMenuViewModels());
        recyclerViewAdapter.notifyDataSetChanged();
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
                updateRecyclerView();
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
        beaconHistory = new BeaconHistory();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser(BeaconType.IBEACON.toString()).setBeaconLayout(IBEACON_LAYOUT));
        beaconManager.bind(this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationTracker = new LocationTracker(locationManager);
        context = getApplicationContext();
        notifications = new Notifications();
        Notifications.createNotificationChannel(context);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_radar, menu);
        return true;
    }

    protected void storeBeacon(Beacon beacon) {
        double distance = beacon.getDistance();
        Log.i(TAG, "Parser " + beacon.getParserIdentifier());
        Location loc = locationTracker.getLastLocation();
        if (loc != null) {
            beaconHistory.add(beacon, BeaconType.IBEACON, new BeaconDetection(new Date(), loc, distance));
        }
    }

    private void RequestLocationManager() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
