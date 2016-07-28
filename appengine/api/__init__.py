# -*- coding: utf-8 -*-
import webapp2

from api import handler

app = webapp2.WSGIApplication([
    ('/api/v1/healthcheck', handler.HealthcheckHandler),
], debug=True)
