package hawk.privacy.bledoubt;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AirTagBeaconParser extends BeaconParser {
    private static String TAG = "[AirTagBeaconParser]";
    private static final short FLAGS_TYPE = 0x01;
    private static final byte COMPLETE_SERVICE_ID_16_BIT_TYPE = 0x03;
    private static final byte MANUFACTURER_SPECIFIC_DATA_TYPE = (byte) 0xFF;
    private static final int AIR_TAG_MANUFACTURER_ID = 0x004C;


    public AirTagBeaconParser() {
        this.mIdentifier = "AirTag";
    }

    @Override
    public Beacon fromScanData(byte[] bytesToProcess, int rssi, BluetoothDevice device, long timestampMs) {
        if (bytesToProcess == null)
            return null;
        Beacon.Builder builder = new Beacon.Builder()
                .setBluetoothAddress(device.getAddress())
                .setBluetoothName(device.getName())
                .setRssi(rssi)
                .setMultiFrameBeacon(false)
                .setId1("")
                .setId2("")
                .setId3("")
                .setParserIdentifier(mIdentifier)
                ;

        for (AirTagBeaconParser.EirPacket packet : enumeratePackets(bytesToProcess)) {
            switch (packet.type) {
                case MANUFACTURER_SPECIFIC_DATA_TYPE:
                    byte[] manufacturer_id_bytes = {packet.data[0], packet.data[1]};
                    if (bytesToInt(manufacturer_id_bytes) == AIR_TAG_MANUFACTURER_ID) {
                        return builder.build();
                    }
                default:
                    continue;
            }
        }

        return null;
    }

    /**
     * A plain-old-data class which represents a single Bluetooth Extended Inquiry Response
     * packet, including type and data information. Types can be found at
     * https://www.bluetooth.com/specifications/assigned-numbers/generic-access-profile/
     */
    private class EirPacket {
        public short type;
        public byte[] data;
        public EirPacket(byte type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }

    /**
     * Create an iterable of input byte sequence interpreting the data as EIR packets.
     *
     * @param bytesToProcess - The byte array from a single Bluetooth advertisement.
     * @return
     */
    private List<AirTagBeaconParser.EirPacket> enumeratePackets(byte[] bytesToProcess) {
        ArrayList<AirTagBeaconParser.EirPacket> packets = new ArrayList<>();
        int packetStart = 0;
        while (packetStart < bytesToProcess.length && bytesToProcess[packetStart] != 0) {
            int packetLength = bytesToProcess[packetStart];
            byte type = bytesToProcess[packetStart+1];
            byte[] data = new byte[packetLength-1];
            for (int i = 0; i < packetLength-1; i++)
                data[i] = bytesToProcess[packetStart + 2 + i];
            packets.add(new AirTagBeaconParser.EirPacket(type, data));
            packetStart += 1+packetLength;
        }
        return  packets;
    }


    /**
     * Get the integer equivalent to the concatenation of the input bytes.
     *
     * Example bytesToInt({0xB1,0x33)) == 0xB133
     *
     * @param bytes - an array of bytes interpretted in little-endian byte order. Must be
     * 2 bytes in length.
     */
    static private int bytesToInt(byte[] bytes) {
        final int bytesPerInt = 2;
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result += (((int) bytes[i]) << (2*4*i)) & (0xff <<(2*4*i));
        }
        return result;
    }
}
