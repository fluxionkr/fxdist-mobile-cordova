cordova.define("kr.fluxion.cordova.plugin.navigation.Navigation", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.finishApp= function () {
        exec(null, null, 'Navigation', 'finishApp', []);
    };
    exports.logout= function () {
        exec(null, null, 'Navigation', 'logout', []);
    };
});
