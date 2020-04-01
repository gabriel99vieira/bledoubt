package hawk.privacy.bledoubt;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceMainMenuViewHolder extends RecyclerView.ViewHolder {
    private TextView view;

    public DeviceMainMenuViewHolder(@NonNull View itemView) {
        super(itemView);
        view = (TextView) itemView.findViewById(R.id.deviceIdentifier1);
    }

    public void bindData(final DeviceMetadata viewModel) {
        view.setText(viewModel.getIdentifier());
    }

}
