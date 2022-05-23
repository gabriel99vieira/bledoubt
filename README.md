# About BLE-Doubt

BLE-Doubt is an Android app that helps you detect Bluetooth-low-energy (BLE) trackers like AirTags, Tiles, and Chipolos that may be following you around. These devices tend to be small and inconspicuous. They could easily be slipped into your pocket, purse, shoe, or vehicle by someone who wants to know your location data. BLE-Doubt runs in the background looking for suspicious devices when you're on the go, and sends you a notification if it finds something suspicious. 

BLE-Doubt is a research application that you're welcome to compile and try out, but for a better UX, we recommend [AirGuard](https://play.google.com/store/apps/details?id=de.seemoo.at_tracking_detection.release&hl=en_US&gl=US) to non-expert users. The analysis used for our 2022 SafeThings paper can be found in the `analysis` directory. 


# How does BLE-Doubt work?

Understanding how BLE-Doubt works requires some basic knowledge about how BLE trackers work. BLE trackers are radio-frequency beacons that broadcast a signal out periodically. This signal contains some basic information about the device: a Bluetooth address, a broadcast signal strength (a measure of how loud the device is "speaking"), and some other data that depends on the type of beacon being used. A Bluetooth address is a unique number which serves as a name for the beacon. BLE-beacons have a limited range, so if your phone picks up many signals with a single Bluetooth address during a long walk or a car trip, it is likely that the device is following you. BLE-Doubt identifies such devices.

## Dealing with false alarms

If you knowingly carry a Bluetooth connected device around with you, like a your own Tile or AIrTag or a pair of BLE headphones, BLE-Doubt will probably flag them as suspicious the first time you travel with them. You can mark these devices as "safe" if you recognize them and believe they are harmless. BLE-doubt will ignore safe devices until you specify otherwise from the Options menu.

Additionally, taking public transit may put you in contact with many Bluetooth devices which are not trackers -- namely the devices owned by other people on your train or bus.

## Detecting devices in "privacy mode"

Some Bluetooth devices rotate through multiple Bluetooth addresses in an attempt to protect the privacy of the device's owner. BLE-Doubt can detect these devices, but may lose track of them after a while.

# Limitations

BLE-Doubt cannot guarantee your protection from malicious trackers. Some tracking devices are based on Global Positioning System (GPS) or ultra-wideband (UWB) technologies, which BLE-Doubt cannot intercept. Even some BLE trackers may deviate from the assumptions made by BLE-Doubt. For example, some devices in privacy mode who change their Bluetooth addresses very frequently may not be successfully screened by BLE-Doubt. BLE-doubt also will not detect stationaty Bluetooth devices that do not follow you when you move. To locate all nearby BLE devices, you can use a Bluetooth scanning app like  [nRF Connect](https://www.nordicsemi.com/Software-and-tools/Development-Tools/nRF-Connect-for-mobile), although this may be difficult for non-expert users. Apple also offers a [scanner app](https://play.google.com/store/apps/details?id=com.apple.trackerdetect&hl=en_US&gl=US) to help you find nearby AirTags.

# License 

All BLE-Doubt code, algorithms, and data are licensed under the MIT license included in the `LICENSE` file. 

# Citation
If you use BLE-Doubt in your academic or professional work, we ask that you cite our [SafeThings 2022 paper](https://safe-things-2022.github.io/).

```bibtex
@inproceedings{BLE-Doubt,
  author    = "Briggs, Jimmy and Geeng, Christine",
  title     = "BLE-Doubt: Smartphone-Based Detection of Malicious Bluetooth Trackers",
  booktitle = "SafeThings 2022",
  publisher = "IEEE",
}
```

