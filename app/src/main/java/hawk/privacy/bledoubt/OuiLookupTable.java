package hawk.privacy.bledoubt;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.RequiresApi;

public class OuiLookupTable {

    private Context context;
    private Map<String, String> macPrefixToString = null;
    private static final String TAG = "[OUI Lookup]";

    public OuiLookupTable(Context context) {
        this.context = context;
    }

    /**
     * Determine the corporation that manufactured the device with a given MAC address,
     * by comparing the MAC address prefix to a database of Organizationally Unique Identifiers
     * (OUI).
     *
     * Note: lazy loading of OUI database means this may be slow the first time it runs.
     *
     * @param mac the MAC address of the device. (Or at least the first 3 bytes thereof.)
     * @return the OUI of the address OR the empty string if no OUI could be found.
     */
    public synchronized String lookupOui(String mac) {
        if (this.macPrefixToString == null && context != null ) {
            lazyLoadDictionary();
        }
        String oui = macPrefixToString.get(mac.substring(0,8));
        if (oui != null)
            return oui;
        return "";
    }

    private void lazyLoadDictionary() {
        macPrefixToString = new HashMap<>();
        InputStream ouiXmlSteam = context.getResources().openRawResource(R.raw.oui_lookup);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(ouiXmlSteam, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                Log.i(TAG, Integer.toString(eventType));
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("VendorMapping")) {
                            parser.require(XmlPullParser.START_TAG, null, "VendorMapping");
                            String prefix = parser.getAttributeValue(null, "mac_prefix");
                            String vendor = parser.getAttributeValue(null, "vendor_name");
                            Log.i(TAG, prefix + " " + vendor);
                            macPrefixToString.put(prefix, vendor);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }


}
