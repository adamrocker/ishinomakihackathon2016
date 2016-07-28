# -*- coding: utf-8 -*-
# Copyright 2016 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import jinja2
import webapp2


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.join(os.path.dirname(__file__), 'templates')),
    autoescape=False)


class PageHandler(webapp2.RequestHandler):
    def render(self, template_name, value):
        if not isinstance(value, dict):
            value = {}

        self.response.headers['Content-Type'] = 'text/html'
        template = JINJA_ENVIRONMENT.get_template(template_name)
        html = template.render(value)
        self.response.write(html)
        return


class IndexHandler(PageHandler):

    def get(self):
        self.render("index.html", None)
        return


app = webapp2.WSGIApplication([
    ('/', IndexHandler),
], debug=True)
