package hawk.privacy.bledoubt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class InspectDeviceActivity extends Activity {
    public static final String BLUETOOTH_ADDRESS_MESSAGE = "hawk.privacy.bledoubt.bluetooth_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspect_device);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        Intent intent = getIntent();
        String bluetoothAddress = intent.getStringExtra(BLUETOOTH_ADDRESS_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.inspect_layout_title);
        textView.setText(bluetoothAddress);

        Trajectory traj = BeaconHistory.getAppBeaconHistory(this).getTrajectory(bluetoothAddress);
        textView.setText(traj.toString());
        //FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

}
