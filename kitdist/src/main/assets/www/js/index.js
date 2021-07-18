var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();

document.getElementById('show_nfc_tag_view').addEventListener('click', function() {
    var success = function(v) {
        console.log(JSON.stringify(v));
        console.log("hasNext? " + JSON.parse(v).data.tagData.hasNext)
        console.log(JSON.parse(v).data.tagData.currentPageIndex + " / " + JSON.parse(v).data.tagData.totalPageCount)
    }, error = function(v) {
        alert(v);
    };
    cordova.plugins.NFC.showNFCTagView(success, error, "read_logs_data", 1);
//    cordova.plugins.NFC.showNFCTagView(success, error, "read_logs_data", 2, 2);
});

document.getElementById('open_camera').addEventListener('click', function() {
    var success = function(v) {
        console.log(v);
        document.getElementById('photo').setAttribute("src", JSON.parse(v).data.data);
    },
    error = function(v) { alert(v); };
    cordova.plugins.Camera.openCamera(success, error);
});

document.getElementById('open_signpad').addEventListener('click', function() {
    var success = function(v) {
        console.log(v);
        document.getElementById('signature').setAttribute("src", JSON.parse(v).data.signData);
     },
     error = function(v) { alert(v); };
    cordova.plugins.SignPad.startSignPad(success, error);
});

document.getElementById('start_location_service').addEventListener('click', function() {
    var success = function(v) { console.log(v); }, error = function(v) { alert(v); };
    cordova.plugins.Location.startLocationService(success, error);
});

document.getElementById('stop_location_service').addEventListener('click', function() {
    var success = function(v) { console.log(v); }, error = function(v) { alert(v); };
    cordova.plugins.Location.stopLocationService(success, error);
});

document.getElementById('open_barcode_scanner').addEventListener('click', function() {
    var success = function(v) { alert(JSON.parse(v).data.barcode); },
    error = function(v) { alert(v); };
    cordova.plugins.Barcode.openBarcodeScanner(success, error);
});

document.getElementById('get_login_info').addEventListener('click', function() {
    var success = function(v) {
        console.log(JSON.stringify(JSON.parse(v).data.userData));
    },error = function(v) { alert(v); };
    cordova.plugins.Auth.getLoginInfo(success, error);
});

document.addEventListener("backbutton", function() {
    console.log("BackButton has been clicked!");
    cordova.plugins.Navigation.logout();
//    cordova.plugins.Navigation.finishApp();
}, false);