package hawk.privacy.bledoubt;

import android.bluetooth.BluetoothClass;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class DeviceMainMenuViewAdapter extends RecyclerView.Adapter {
    List<DeviceMainMenuViewModel> models;

    public DeviceMainMenuViewAdapter(List<DeviceMainMenuViewModel> models) {
        this.models = models;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new DeviceMainMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((DeviceMainMenuViewHolder) holder).bindData(models.get(position));
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.recycler_view_device;
    }
}
