cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "kr.co.kit.cordova.plugin.navigation.Navigation",
      "file": "plugins/kr.co.kit.plugin.navigation/www/Navigation.js",
      "pluginId": "kr.co.kit.cordova.plugin.navigation",
      "clobbers": [
        "cordova.plugins.Navigation"
      ]
    },
    {
      "id": "kr.co.kit.cordova.plugin.nfc.NFC",
      "file": "plugins/kr.co.kit.plugin.nfc/www/NFC.js",
      "pluginId": "kr.co.kit.cordova.plugin.nfc",
      "clobbers": [
        "cordova.plugins.NFC"
      ]
    },
    {
      "id": "kr.co.kit.cordova.plugin.signpad.SignPad",
      "file": "plugins/kr.co.kit.plugin.signpad/www/SignPad.js",
      "pluginId": "kr.co.kit.cordova.plugin.signpad",
      "clobbers": [
        "cordova.plugins.SignPad"
      ]
    },
    {
      "id": "kr.co.kit.cordova.plugin.camera.Camera",
      "file": "plugins/kr.co.kit.plugin.camera/www/Camera.js",
      "pluginId": "kr.co.kit.cordova.plugin.camera",
      "clobbers": [
        "cordova.plugins.Camera"
      ]
    },
    {
      "id": "kr.co.kit.cordova.plugin.location.Location",
      "file": "plugins/kr.co.kit.plugin.location/www/Location.js",
      "pluginId": "kr.co.kit.cordova.plugin.location",
      "clobbers": [
        "cordova.plugins.Location"
      ]
    },
    {
      "id": "kr.co.kit.cordova.plugin.barcode.Barcode",
      "file": "plugins/kr.co.kit.plugin.barcode/www/Barcode.js",
      "pluginId": "kr.co.kit.cordova.plugin.barcode",
      "clobbers": [
        "cordova.plugins.Barcode"
      ]
    },
   {
     "id": "kr.co.kit.cordova.plugin.auth.Auth",
     "file": "plugins/kr.co.kit.plugin.auth/www/Auth.js",
     "pluginId": "kr.co.kit.cordova.plugin.auth",
     "clobbers": [
       "cordova.plugins.Auth"
     ]
   }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "kr.co.kit.cordova.plugin.navigation": "1.0.0",
    "kr.co.kit.cordova.plugin.nfc": "1.0.0",
    "kr.co.kit.cordova.plugin.camera": "1.0.0",
    "kr.co.kit.cordova.plugin.location": "1.0.0",
    "kr.co.kit.cordova.plugin.barcode": "1.0.0",
    "kr.co.kit.cordova.plugin.auth": "1.0.0"
  };
});