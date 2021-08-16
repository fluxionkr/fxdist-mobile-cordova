cordova.define("kr.fluxion.cordova.plugin.location.Location", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.startLocationService = function (success, error) {
        exec(success, error, 'Location', 'startLocationService', []);
    };
    exports.stopLocationService = function (success, error) {
        exec(success, error, 'Location', 'stopLocationService', []);
    };
});
