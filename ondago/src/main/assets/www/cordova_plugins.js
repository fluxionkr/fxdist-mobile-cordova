cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "kr.co.aiblab.cordova.plugin.navigation.Navigation",
      "file": "plugins/kr.co.aiblab.plugin.navigation/www/Navigation.js",
      "pluginId": "kr.co.aiblab.cordova.plugin.navigation",
      "clobbers": [
        "cordova.plugins.Navigation"
      ]
    },
    {
      "id": "kr.co.aiblab.cordova.plugin.nfc.NFC",
      "file": "plugins/kr.co.aiblab.plugin.nfc/www/NFC.js",
      "pluginId": "kr.co.aiblab.cordova.plugin.nfc",
      "clobbers": [
        "cordova.plugins.NFC"
      ]
    },
    {
      "id": "kr.co.aiblab.cordova.plugin.signpad.SignPad",
      "file": "plugins/kr.co.aiblab.plugin.signpad/www/SignPad.js",
      "pluginId": "kr.co.aiblab.cordova.plugin.signpad",
      "clobbers": [
        "cordova.plugins.SignPad"
      ]
    },
    {
      "id": "kr.co.aiblab.cordova.plugin.camera.Camera",
      "file": "plugins/kr.co.aiblab.plugin.camera/www/Camera.js",
      "pluginId": "kr.co.aiblab.cordova.plugin.camera",
      "clobbers": [
        "cordova.plugins.Camera"
      ]
    },
    {
      "id": "kr.co.aiblab.cordova.plugin.location.Location",
      "file": "plugins/kr.co.aiblab.plugin.location/www/Location.js",
      "pluginId": "kr.co.aiblab.cordova.plugin.location",
      "clobbers": [
        "cordova.plugins.Location"
      ]
    },
    {
      "id": "kr.co.aiblab.cordova.plugin.barcode.Barcode",
      "file": "plugins/kr.co.aiblab.plugin.barcode/www/Barcode.js",
      "pluginId": "kr.co.aiblab.cordova.plugin.barcode",
      "clobbers": [
        "cordova.plugins.Barcode"
      ]
    },
   {
     "id": "kr.co.aiblab.cordova.plugin.auth.Auth",
     "file": "plugins/kr.co.aiblab.plugin.auth/www/Auth.js",
     "pluginId": "kr.co.aiblab.cordova.plugin.auth",
     "clobbers": [
       "cordova.plugins.Auth"
     ]
   }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "kr.co.aiblab.cordova.plugin.navigation": "1.0.0",
    "kr.co.aiblab.cordova.plugin.nfc": "1.0.0",
    "kr.co.aiblab.cordova.plugin.camera": "1.0.0",
    "kr.co.aiblab.cordova.plugin.location": "1.0.0",
    "kr.co.aiblab.cordova.plugin.barcode": "1.0.0",
    "kr.co.aiblab.cordova.plugin.auth": "1.0.0"
  };
});