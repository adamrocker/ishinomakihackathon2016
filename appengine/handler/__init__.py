# -*- coding: utf-8 -*-
import webapp2
from handler import room


app = webapp2.WSGIApplication([
    ('/room', room.IndexHandler),
    ('/room/[0-9].*', room.RoomHandler)
], debug=True)
