# **cordova-ext-9.0.0**


## **How to add new Cordova Plugin**
  
<br/>

### 1. Cordova javascript 파일 추가
> Web contents 에 포함

/plugins/kr.fluxion.plguin.pluginName/www/Plugin.js 추가
````javascript
cordova.define("kr.fluxion.cordova.plugin.pluginName.Plugin", function(require, exports, module) {
    var exec = require('cordova/exec');
    exports.showToast = function (message, success, error) {
        exec(success, error, 'Plugin', 'plugin_action', [message]);
    };
});
````

/cordova_plugin.js 업데이트
````javascript
cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    // 1. Plugin 추가
    {
      "id": "kr.fluxion.cordova.plugin.pluginName.Plugin",
      "file": "plugins/kr.fluxion.plugin.pluginName/www/Plugin.js",
      "pluginId": "kr.fluxion.cordova.plugin.pluginName",
      "clobbers": [
        "cordova.plugins.Plugin"
      ]
    }
  ];
  module.exports.metadata = {
    // 2. Plugin 메타정보 추가
    "kr.fluxion.cordova.plugin.pluginName": "1.0.0"
  };
});
````

### 2. Cordova Java(Kotlin) 파일 추가
> - 패키지 명은 플러그인 모듈의 id 가 되므로 정의할 때 주의해야 함
> - action 과 args 정의는 js 에 정의한 내용과 일치해야 함
````java
public class Plugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("plugin_action")) {
            String arg0 = args.getString(0);
            // TODO 
            return true;
        }
        return false;
    }
}
````

### 3. config.xml 파일 업데이트
feature 추가
````xml
<feature name="Plugin">
    <param
        name="android-package"
        value="kr.fluxion.plugin.pluginName.Plugin" />
</feature>
````

### 4. android.json 파일 업데이트

````json
"config_munge": {
    "files": {
      "res/xml/config.xml": {
        "parents": {
          "/*": [
            {
              "xml": "<feature name=\"Plugin\"><param name=\"android-package\" value=\"kr.fluxion.cordova.plugin.pluginName\" /></feature>",
              "count": 1
            }
          ]
        }
      }
    }
}
````
````json
"installed_plugins": {
    "kr.fluxion.cordova.plugin.pluginName": {
      "PACKAGE_NAME": "kr.fluxion.cordova"
    }
  }
````
````json
"modules": [
    {
      "id": "kr.fluxion.cordova.plugin.pluginName.Plugin",
      "file": "plugins/kr.fluxion.cordova.plugin.pluginName/www/Plugin.js",
      "pluginId": "kr.fluxion.cordova.plugin.pluginName",
      "clobbers": [
        "cordova.plugins.Plugin"
      ]
    }
  ],
  "plugin_metadata": {
    "kr.fluxion.cordova.plugin.pluginName": "1.0.0"
  }
````