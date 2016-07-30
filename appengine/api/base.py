# -*- coding: utf-8 -*-
import webapp2
import json


class ApiHandler(webapp2.RequestHandler):

    def api_version(self):
        return self.request.path.split('/')[2]

    def api_kind(self):
        return self.request.path.split('/')[3]

    def api_action(self):
        return self.request.path.split('/')[4]

    def api_value(self):
        return self.request.path.split('/')[5]

    def get_host_url(self):
        # host_url-> http://test.sample.com
        return self.request.host_url

    def get_base_api_url(self):
        return "{host}/api/v1".format(host=self.get_host_url())

    def get_param_as_json(self):
        return json.loads(self.request.body)

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
