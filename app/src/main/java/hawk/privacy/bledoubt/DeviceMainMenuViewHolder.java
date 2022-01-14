package hawk.privacy.bledoubt;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import hawk.privacy.bledoubt.ui.main.InspectDeviceFragment;
import hawk.privacy.bledoubt.ui.main.RadarFragment;

public class DeviceMainMenuViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {
    private TextView view;
    @NonNull
    private Context context;
    private DeviceMetadata viewModel;
    private static final String TAG = "DeviceMainMenuViewHolder";

    public DeviceMainMenuViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        view = itemView.findViewById(R.id.deviceIdentifier1);
        view.setOnClickListener(this);
        this.context = context;
    }

    public void bindData(final DeviceMetadata viewModel) {
        this.viewModel = viewModel;
        view.setText(viewModel.bluetoothAddress);
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putString(InspectDeviceFragment.BLUETOOTH_ADDRESS_MESSAGE, viewModel.bluetoothAddress);
        Log.d(TAG, viewModel.bluetoothAddress);

        ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, InspectDeviceFragment.class, bundle)
                .addToBackStack("inspect_device")
                .commit();
        }
}
