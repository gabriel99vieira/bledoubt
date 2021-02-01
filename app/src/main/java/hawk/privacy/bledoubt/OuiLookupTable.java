package hawk.privacy.bledoubt;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OuiLookupTable {

    private Context context;
    private Map<String, String> macPrefixToString = null;
    private List<Integer> macPrefixLengthsDescending = null;
    private static final String TAG = "[OUI Lookup]";

    public OuiLookupTable(Context context) {
        this.context = context;
    }

    /**
     * Determine the corporation that manufactured the device with a given MAC address,
     * by comparing the MAC address prefix to a database of Organizationally Unique Identifiers
     * (OUI). In the case that multiple MAC prefixes match, the OUI corresponding to the  longest
     * such prefix will be returned.
     *
     * Note: lazy loading of OUI database means this may be slow the first time it runs.
     *
     * @param mac the MAC address of the device. (Or at least the first 3 bytes thereof.)
     * @return the OUI of the MAC address OR the empty string if no OUI could be found.
     */
    public synchronized String lookupOui(String mac) {
        if (this.macPrefixToString == null && context != null ) {
            lazyLoadDictionary();
        }
        return findLongestPrefixInDictionary(mac);
    }

    /**
     * Find the longest MAC prefix that matches the given string. For example, if the
     * dictionary contains both the keys "00:50:C2" and "00:50:C2:00:9", a the mac address
     * "00:50:C2:00:91:CF" would return the OUI corresponding to "00:50:C2:00:9"
     *
     * @param mac the mac address to match against known MAC prefixes
     * @return the Organizationally Unique Identifier (OUI) for the given mac address
     */
    private String findLongestPrefixInDictionary(String mac) {
        if (macPrefixToString != null && macPrefixLengthsDescending != null) {
            for (Integer len : macPrefixLengthsDescending) {
                if (len > mac.length())
                    continue;
                String oui = macPrefixToString.get(mac.substring(0, len));
                Log.i(TAG,  mac.substring(0, len) + " " + oui);
                if (oui != null) {
                    return oui;
                }
            }
        }
        return  "";
    }

    /**
     * Populate the lookup table from MAC prefixes to Organizationally Unique Identifiers (OUI)
     * from the vendorMac resource file. As part of the setup, this also populates a list of lengths
     * of OUI prefixes, which allows us to search for the longest matching prefix for a given mac.
     */
    private void lazyLoadDictionary() {
        macPrefixToString = new HashMap<>();
        macPrefixLengthsDescending = new ArrayList<>();
        InputStream ouiXmlSteam = context.getResources().openRawResource(R.raw.oui_lookup);
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(ouiXmlSteam, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // Pull out the data from each VendorMapping tag and add it to the dictionary
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("VendorMapping")) {
                            parser.require(XmlPullParser.START_TAG, null, "VendorMapping");
                            String prefix = parser.getAttributeValue(null, "mac_prefix");
                            String vendor = parser.getAttributeValue(null, "vendor_name");
                            int len = prefix.length();
                            macPrefixToString.put(prefix, vendor);
                            // Keep the length so we can search for the longest prefix later.
                            if (!macPrefixLengthsDescending.contains(len)) {
                                macPrefixLengthsDescending.add(len);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            macPrefixToString = null;
            macPrefixLengthsDescending = null;
        }
        // Sort macPrefixLengths in descending order

        if (macPrefixLengthsDescending != null) {
            Collections.sort(macPrefixLengthsDescending, Collections.<Integer>reverseOrder());
        }
    }


}
