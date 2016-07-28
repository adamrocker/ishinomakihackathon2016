/// <reference path="../../typings/globals/jquery/index.d.ts" />
import $ = require("jquery");
export class Sdk {
    public static healthcheck(): void {
        let callback = (data) => {
            debugger;
            console.log(data);
        };
        let path: string = "/api/v1/healthcheck";
        Sdk._post_json(path, {}, callback, null);
    }
    
    private static _post_json(path, values, callback_done, callback_fail) {
        let self = this;
        let defaults = {
        };
        let encodedparams = {};
        for (let key in values) {
            let value = values[key];
            if (typeof value == "string") {
                encodedparams[key] = encodeURI(value);
            } else {
                encodedparams[key] = value
            }
        }
        
        $.ajax({
            async: true,
            type: "post",
            url: path,
            data: JSON.stringify(encodedparams),
            mimeType: "application/json;charset=utf-8",
            contentType: "application/JSON;charset=utf-8",
            dataType : "JSON",
            scriptCharset: "utf-8",
        }).done(function(data, text, jqXHR) {
            if (callback_done) {
                callback_done(data, text, jqXHR);
            }
        }).fail(function(data) {
            if (callback_fail) {
                callback_fail(data);
            }
        });
    }
}