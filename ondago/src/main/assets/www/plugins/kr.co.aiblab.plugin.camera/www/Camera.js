cordova.define("kr.co.aiblab.cordova.plugin.camera.Camera", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.openCamera = function (success, error) {
        exec(success, error, 'Camera', 'openCamera', []);
    };
});
