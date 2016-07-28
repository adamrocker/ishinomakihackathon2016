/// <reference path="../../typings/index.d.ts" />
import $ = require("jquery");
import bootstrap = require("bootstrap");

import {View} from "./view";
import {Sdk} from "./sdk";

class App {
    static run() {
        let view = View.getInstance();
        Sdk.healthcheck();
    }
 }

$(function(){
    App.run();
});