package hawk.privacy.bledoubt;

import androidx.annotation.NonNull;

public class DeviceMainMenuViewModel {
    private String identifier;

    public DeviceMainMenuViewModel(@NonNull final String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@NonNull final String identifier) {
        this.identifier = identifier;
    }
}
