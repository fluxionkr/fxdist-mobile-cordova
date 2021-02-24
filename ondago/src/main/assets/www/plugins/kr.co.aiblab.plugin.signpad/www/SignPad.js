cordova.define("kr.co.aiblab.cordova.plugin.signpad.SignPad", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.startSignPad = function (success, error) {
        exec(success, error, 'SignPad', 'startSignPad');
    };
});
