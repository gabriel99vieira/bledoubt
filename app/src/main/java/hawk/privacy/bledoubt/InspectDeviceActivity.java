package hawk.privacy.bledoubt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class InspectDeviceActivity extends Activity {
    public static final String BLUETOOTH_ADDRESS_MESSAGE = "hawk.privacy.bledoubt.bluetooth_address";
    public static final int MAP_CAMERA_DEFAULT_PADDING = 100;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_api_key));

        setContentView(R.layout.activity_inspect_device);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        Intent intent = getIntent();
        final String bluetoothAddress = intent.getStringExtra(BLUETOOTH_ADDRESS_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.inspect_layout_title);
        textView.setText(bluetoothAddress);

        final Trajectory traj = BeaconHistory.getAppBeaconHistory(this).getTrajectory(bluetoothAddress);
        textView.setText(traj.toString());

        // Mapbox Stuff
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Add data for trajectory to renderer;
                        style.addSource(new GeoJsonSource("line-source",
                            FeatureCollection.fromFeature(
                                    trajectoryToMapFeature(traj)
                        )));

                        // Apply visual style to trajectory data
                        style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                                PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                        ));
                    }
                });

                // Set the camera to something amicable to the user.
                LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
                for (BeaconDetection detection : traj) {
                    latLngBoundsBuilder.include(new LatLng(detection.latitude, detection.longitude));
                }
                LatLngBounds bounds = latLngBoundsBuilder.build();
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_CAMERA_DEFAULT_PADDING));
            }
        });

        // Allow to mark device as safe
        CheckBox safeCheckbox = findViewById(R.id.safeCheckBox);
        safeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BeaconHistory.getAppBeaconHistory(getApplicationContext()).markSafe(bluetoothAddress, isChecked);
            }
        });

    }

    /**
     * Create a displayable Mapsbox feature from the trajectory.
     * @param traj
     * @return feature
     */
    public static Feature trajectoryToMapFeature(Trajectory traj) {
        ArrayList<Point> trajectoryCoordinates = new ArrayList<>();
        for (BeaconDetection det : traj) {
            trajectoryCoordinates.add(Point.fromLngLat(det.longitude, det.latitude));
        }
        return Feature.fromGeometry(
                LineString.fromLngLats(trajectoryCoordinates)
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}