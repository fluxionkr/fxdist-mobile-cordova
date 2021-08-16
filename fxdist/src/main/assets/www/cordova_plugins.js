cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "kr.fluxion.cordova.plugin.navigation.Navigation",
      "file": "plugins/kr.fluxion.plugin.navigation/www/Navigation.js",
      "pluginId": "kr.fluxion.cordova.plugin.navigation",
      "clobbers": [
        "cordova.plugins.Navigation"
      ]
    },
    {
      "id": "kr.fluxion.cordova.plugin.nfc.NFC",
      "file": "plugins/kr.fluxion.plugin.nfc/www/NFC.js",
      "pluginId": "kr.fluxion.cordova.plugin.nfc",
      "clobbers": [
        "cordova.plugins.NFC"
      ]
    },
    {
      "id": "kr.fluxion.cordova.plugin.signpad.SignPad",
      "file": "plugins/kr.fluxion.plugin.signpad/www/SignPad.js",
      "pluginId": "kr.fluxion.cordova.plugin.signpad",
      "clobbers": [
        "cordova.plugins.SignPad"
      ]
    },
    {
      "id": "kr.fluxion.cordova.plugin.camera.Camera",
      "file": "plugins/kr.fluxion.plugin.camera/www/Camera.js",
      "pluginId": "kr.fluxion.cordova.plugin.camera",
      "clobbers": [
        "cordova.plugins.Camera"
      ]
    },
    {
      "id": "kr.fluxion.cordova.plugin.location.Location",
      "file": "plugins/kr.fluxion.plugin.location/www/Location.js",
      "pluginId": "kr.fluxion.cordova.plugin.location",
      "clobbers": [
        "cordova.plugins.Location"
      ]
    },
    {
      "id": "kr.fluxion.cordova.plugin.barcode.Barcode",
      "file": "plugins/kr.fluxion.plugin.barcode/www/Barcode.js",
      "pluginId": "kr.fluxion.cordova.plugin.barcode",
      "clobbers": [
        "cordova.plugins.Barcode"
      ]
    },
   {
     "id": "kr.fluxion.cordova.plugin.auth.Auth",
     "file": "plugins/kr.fluxion.plugin.auth/www/Auth.js",
     "pluginId": "kr.fluxion.cordova.plugin.auth",
     "clobbers": [
       "cordova.plugins.Auth"
     ]
   }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "kr.fluxion.cordova.plugin.navigation": "1.0.0",
    "kr.fluxion.cordova.plugin.nfc": "1.0.0",
    "kr.fluxion.cordova.plugin.camera": "1.0.0",
    "kr.fluxion.cordova.plugin.location": "1.0.0",
    "kr.fluxion.cordova.plugin.barcode": "1.0.0",
    "kr.fluxion.cordova.plugin.auth": "1.0.0"
  };
});