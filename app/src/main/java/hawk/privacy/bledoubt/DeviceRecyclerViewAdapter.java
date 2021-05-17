package hawk.privacy.bledoubt;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter implements Observer<List<DeviceMetadata>> {
    List<DeviceMetadata> models = new ArrayList<DeviceMetadata>();
    private Context context;

    public DeviceRecyclerViewAdapter(List<DeviceMetadata> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new DeviceMainMenuViewHolder(view, context);
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

    @Override
    public void onChanged(List<DeviceMetadata> deviceMetadata) {
        Log.d("On CHANGED", deviceMetadata.toString());
        this.models = deviceMetadata;
        notifyDataSetChanged();
    }
}
