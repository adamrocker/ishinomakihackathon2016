# -*- coding: utf-8 -*-
import webapp2

from api import handler

app = webapp2.WSGIApplication([
    ('/api/v1/healthcheck', handler.HealthcheckHandler),
    ('/api/v1/peer/register', handler.peer.PeerRegisterHandler),
    ('/api/v1/peer/get/[0-9].*', handler.peer.PeerGetHandler)
], debug=True)
