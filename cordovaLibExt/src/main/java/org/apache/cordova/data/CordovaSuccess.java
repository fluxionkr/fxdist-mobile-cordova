package org.apache.cordova.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

public class CordovaSuccess {

    @SuppressWarnings("unused")
    @SerializedName("code")
    private int code;

    @SuppressWarnings("unused")
    @SerializedName("message")
    private String message;

    @SuppressWarnings("unused")
    @SerializedName("data")
    private JsonElement data;

    private CordovaSuccess(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private CordovaSuccess(int code, String message, String data) {
        this.code = code;
        this.message = message;
        JsonElement parser = JsonParser.parseString(data);
        if (parser.isJsonObject() || parser.isJsonArray()) {
            this.data = parser;
        } else {
            throw new IllegalArgumentException("data is not JsonObject.");
        }
    }

    @SuppressWarnings("unused")
    public static String getCordovaSuccess(int code) {
        return getCordovaSuccess(code, "");
    }

    public static String getCordovaSuccess(int code, String message) {
        return new Gson().toJson(new CordovaSuccess(code, message), CordovaSuccess.class);
    }

    public static String getCordovaSuccess(int code, String message, String data) {
        return new Gson().toJson(new CordovaSuccess(code, message, data), CordovaSuccess.class);
    }
}
