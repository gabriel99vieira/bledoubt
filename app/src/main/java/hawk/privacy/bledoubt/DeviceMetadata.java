package hawk.privacy.bledoubt;

import org.altbeacon.beacon.Beacon;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DeviceMetadata {
    @PrimaryKey @NonNull
    public String bluetoothAddress;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type_code")
    public int typeCode;

    @ColumnInfo(name = "id1")
    public String id1;

    @ColumnInfo(name = "id2")
    public String id2;

    @ColumnInfo(name = "id3")
    public String id3;

    @ColumnInfo(name = "manufacturer")
    public int manufacturer;

    @ColumnInfo(name = "parser_id")
    public String parserId;



    public DeviceMetadata (Beacon beacon, BeaconType type) {
        bluetoothAddress = beacon.getBluetoothAddress();
        name = beacon.getBluetoothName();
        typeCode = beacon.getBeaconTypeCode();
        id1 = beacon.getId1().toHexString();
        id2 = beacon.getId2().toHexString();
        id3 = beacon.getId3().toHexString();
        manufacturer = beacon.getManufacturer();
        parserId = beacon.getParserIdentifier();
    }

    public DeviceMetadata(@NonNull String bluetoothAddress, String name, int typeCode, String id1,
                          String id2, String id3, int manufacturer, String parserId) {
        this.bluetoothAddress = bluetoothAddress;
        this.name = name;
        this.typeCode = typeCode;
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.manufacturer = manufacturer;
        this.parserId = parserId;
    }

    public DeviceMetadata(DeviceMetadata other) {
        bluetoothAddress = other.bluetoothAddress;
        name = other.bluetoothAddress;
        typeCode = other.typeCode;
        id1 = other.id1;
        id2 = other.id2;
        id3 = other.id3;
        manufacturer = other.manufacturer;
        parserId = other.parserId;
    }

    private static String generateIdentifier(Beacon beacon, BeaconType type) {
        String id = "";
        String name = beacon.getBluetoothName();
        if (name != null && name != "") {
            id += name;
        } else {
            id += beacon.getBluetoothAddress();
        }
        return id;
    }
}
