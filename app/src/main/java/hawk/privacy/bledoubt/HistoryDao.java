package hawk.privacy.bledoubt;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMetadata(DeviceMetadata... metadata);

    @Update
    void updateMetadata(DeviceMetadata... metadata);

    @Delete
    void deleteMetadata(DeviceMetadata... metadata);



    @Query("SELECT * FROM DeviceMetadata")
    DeviceMetadata[] loadAllDeviceMetadata();

    @Query("SELECT * FROM DeviceMetadata WHERE bluetoothAddress IN (:bluetoothAddresses)")
    DeviceMetadata[] loadMetadataForDevice(List<String> bluetoothAddresses);


    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertDetections(BeaconDetection... detections);

    @Delete
    void deleteDetections(BeaconDetection... detections);

    @Query("SELECT * FROM BeaconDetection where bluetoothAddress = (:bluetoothAddress) ORDER BY timestamp")
    BeaconDetection[] getDetectionsForDevice(String bluetoothAddress);

    /**
     * Warning: Deletes all data from DeviceMetadata table. Do not execute without
     * also nuking detections.
     */
    @Query("DELETE FROM DeviceMetadata")
    public void nukeDeviceMetadata();

    @Query("DELETE FROM BeaconDetection")
    public void nukeBeaconDetections();

}
