cordova.define("kr.co.aiblab.cordova.plugin.barcode.Barcode", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.openBarcodeScanner = function (success, error) {
        exec(success, error, 'Barcode', 'openBarcodeScanner', []);
    };
});
