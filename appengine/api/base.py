# -*- coding: utf-8 -*-
import webapp2
import json


class ApiHandler(webapp2.RequestHandler):
    def _write_json(self, value, status_code=200):
        value.update({"code": status_code})
        json_value = json.dumps(value)
        self.response.status = status_code
        self.response.headers['Content-Type'] = 'application/json;charset=utf-8'
        self.response.out.write(json_value)
        return

    def write_success(self, value):
        self._write_json(value, status_code=200)
        return
