# About BLE-Doubt

BLE-Doubt is an Android app that helps you find Bluetooth-low-energy (BLE) trackers that may be following you around. These devices tend to be small and inconspicuous. They could easily be slipped into your pocket, purse, shoe, or vehicle by someone who wants to know your location data. BLE-Doubt runs in the background looking for suspicious devices when you're on the go, and sends you a notification  

# Tasks

- [ ] Add real-time permissions for BLE .
- [ ] Add packet parser for Tile devices.
- [ ] Create new constructor to load BeaconHistory from JSON.
- [X] Create copy constructor for BeaconHistory.
- [ ] Create a notification for suspicious devices.
- [ ] Implement OUI lookup.
- [ ] Mark devices as safe.
- [ ] Protect from public transit false alarms.
- [ ] Create trajectory fusion algorithm for privacy mode devices.


# How does BLE-Doubt work?

Understanding how BLE-Doubt works requires some basic knowledge about how BLE trackers work. BLE trackers are radio-frequency beacons that broadcast a signal out periodically. This signal contains some basic information about the device: a Bluetooth address, a broadcast strength (a measure of how loud the device is "speaking"), and some other data that depends on the type of beacon being used. A Bluetooth address is a unique number which serves as a name for the beacon. BLE-beacons have a limited range, so if your phone picks up many signals with a single Bluetooth address during a long walk or a car trip, it is likely that the device is following you. BLE-Doubt identifies such devices.

## Helping to locate suspicious devices

Your smart phone keeps track of how loudly nearby Bluetooth devices are "speaking" over Bluetooth. In general, louder devices are closer. Once BLE-Doubt alerts you about a suspicious device, you will have the option
of searching for the device by using your phone as a proximity sensor. BLE-Doubt will also tell you the name and manufacturer of the device if that information is available.

## Dealing with false alarms

If you knowingly carry a Bluetooth connected device around with you, like a Tile tracker or a pair of BLE headphones, BLE-Doubt will probably flag them as suspicious the first time you travel with them. You can mark these devices as "safe" if you recognize them and believe they are harmless. BLE-doubt will ignore safe devices until you specify otherwise from the Options menu.

Additionally, taking public transit may put you in contact with many Bluetooth devices which are not trackers -- namely the devices owned by other people on your train or bus. BLE-Doubt minimizes the risk of notifying you of these devices by [[[still unknown]]].

## Detecting devices in "privacy mode"

Some Bluetooth devices rotate through multiple Bluetooth addresses in an attempt to protect the privacy of the device's owner. BLE-doubt uses a Bayesian (statistical) decision algorithm to corellate the device across multiple Bluetooth addresses as it rotates through them, allowing it to identify devices in privacy mode as long as they stay a fixed difference from your phone. 

# Limitations

BLE-Doubt cannot guarantee your protection from malicious trackers. Some tracking devices are based on Global Positioning System (GPS) or ultra-wideband (UWB) technologies, which BLE-Doubt cannot intercept. Even some BLE trackers may deviate from the assumptions made by BLE-Doubt. For example, devices in privacy mode which randomly change their broadcasting power may not be successfully screened by BLE-Doubt. BLE-doubt also will not detect stationaty Bluetooth devices that do not follow you when you move. To locate all nearby BLE devices, you can use a Bluetooth scanning app like  [nRF Connect](https://www.nordicsemi.com/Software-and-tools/Development-Tools/nRF-Connect-for-mobile), although this may be difficult for non-expert users.

# License 

BLE-Doubt is under the MIT license included in the `LICENSE` file.