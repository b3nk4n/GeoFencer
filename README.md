# GeoFencer
An Android test app to compare different Geo-fence implementations and configurations.

### Implementations
- Google Play Geo-fencing
- PathSense

### Remarks
- Autmatic re-registration of the geo-fence when the device is rebooted or the location provider was changed
- Optional location polling in the background to feed Google Play Services with data
- Use of BroadcastReceiver instead of Service because services could be stopped by the OS in case of low memory resources
