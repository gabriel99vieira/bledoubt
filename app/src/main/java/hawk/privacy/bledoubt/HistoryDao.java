package hawk.privacy.bledoubt;

import java.util.List;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT * FROM DeviceMetadata")
    LiveData<List<DeviceMetadata>> loadAllDeviceMetadataLive();

    @Query("SELECT * FROM DeviceMetadata WHERE is_suspicious AND NOT is_safe")
    LiveData<List<DeviceMetadata>> loadSuspiciousDeviceMetadataLive();

    @Query("SELECT * FROM DeviceMetadata WHERE is_suspicious AND NOT is_safe")
    List<DeviceMetadata> loadSuspiciousDeviceMetadata();

    @Query("SELECT COUNT(bluetoothAddress)  FROM DeviceMetadata WHERE is_suspicious AND NOT is_safe")
    int countSuspiciousDevices();

    @Query("SELECT * FROM DeviceMetadata WHERE is_safe")
    LiveData<List<DeviceMetadata>> loadSafeDeviceMetadataLive();

    @Query(
       "SELECT meta.* " +
       "FROM " +
       "DeviceMetadata AS meta " +
       "LEFT JOIN " +
       "BeaconDetection AS last_detection " +
       "ON (meta.bluetoothAddress = last_detection.bluetoothAddress) " +
       "WHERE last_detection.timestamp = ( " +
       "  SELECT " +
       "    MAX(detection.timestamp) " +
       "  FROM " +
       "    BeaconDetection AS detection " +
       "  WHERE detection.bluetoothAddress = last_detection.bluetoothAddress " +
       ") " +
       "AND last_detection.timestamp > :start_time")
    LiveData<List<DeviceMetadata>> loadDeviceMetadataSinceTimeLive(String start_time);

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
