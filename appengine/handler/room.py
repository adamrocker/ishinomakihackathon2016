# -*- coding: utf-8 -*-
from handler.base_handler import BaseHandler


class IndexHandler(BaseHandler):
    def get(self):
        value = {
            "url": self.get_url()
        }
        self.render("top/index.html", value)
        return


class RoomHandler(BaseHandler):
    def get(self):
        value = {
            "url": self.get_url()
        }
        self.render("room/index.html", value)
        return
