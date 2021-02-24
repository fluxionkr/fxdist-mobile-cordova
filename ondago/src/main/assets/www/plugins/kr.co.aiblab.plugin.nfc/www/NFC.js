cordova.define("kr.co.aiblab.cordova.plugin.nfc.NFC", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.showNFCTagView = function (success, error, tagType, currentPageIndex, totalPageCount) {
        if (totalPageCount == null) totalPageCount = 0
        exec(success, error, 'NFC', 'showNFCTagView', [tagType, currentPageIndex, totalPageCount]);
    };
});
