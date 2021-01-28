package hawk.privacy.bledoubt;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

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

    @ColumnInfo(name = "is_safe")
    public boolean isSafe;



    public DeviceMetadata (Beacon beacon, BeaconType type) {
        bluetoothAddress = beacon.getBluetoothAddress();
        name = beacon.getBluetoothName();
        typeCode = beacon.getBeaconTypeCode();

        id1 = beacon.getId1().toHexString();
        id2 = beacon.getId2().toHexString();
        id3 = beacon.getId3().toHexString();
        manufacturer = beacon.getManufacturer();
        parserId = beacon.getParserIdentifier();
        isSafe=false;
    }

    public DeviceMetadata(@NonNull String bluetoothAddress, String name, int typeCode, String id1,
                          String id2, String id3, int manufacturer, String parserId, boolean isSafe) {
        this.bluetoothAddress = bluetoothAddress;
        this.name = name;
        this.typeCode = typeCode;
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.manufacturer = manufacturer;
        this.parserId = parserId;
        this.isSafe = isSafe;
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
        isSafe = other.isSafe;
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

    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        try {
            result.put("address", bluetoothAddress);
            result.put("name", name);
            result.put("type", typeCode);
            result.put("id1", id1);
            result.put("id2", id2);
            result.put("id3", id3);
            result.put("manufacturer", manufacturer);
            result.put("parserId", parserId);
            result.put("isSafe", isSafe);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
