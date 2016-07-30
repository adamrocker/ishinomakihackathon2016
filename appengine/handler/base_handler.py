# -*- coding: utf-8 -*-

import os
import jinja2
import webapp2
import json


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.join(os.path.dirname(__file__), '../templates')),
    autoescape=False)


class BaseHandler(webapp2.RequestHandler):
    def render(self, template_name, value):
        if not isinstance(value, dict):
            value = {}

        self.response.headers['Content-Type'] = 'text/html'
        template = JINJA_ENVIRONMENT.get_template(template_name)
        html = template.render(value)
        self.response.write(html)
        return

    def get_path_item_by_index(self, index):
        return self.request.path.split('/')[index]

    def get_url(self):
        return self.request.url

    def get_app_url(self):
        room_id = self.request.path.split('/')[2]
        url = "app://ishinomaki-hackathon2016.appspot.com/room/{id}".format(id=room_id)
        return url

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
