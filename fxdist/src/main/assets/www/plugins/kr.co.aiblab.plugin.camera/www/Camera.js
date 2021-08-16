cordova.define("kr.fluxion.cordova.plugin.camera.Camera", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.openCamera = function (success, error) {
        exec(success, error, 'Camera', 'openCamera', []);
    };
});
