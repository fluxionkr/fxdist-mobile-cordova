cordova.define("kr.fluxion.cordova.plugin.auth.Auth", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.getLoginInfo = function (success, error) {
        exec(success, error, 'Auth', 'getLoginInfo', []);
    };
});
