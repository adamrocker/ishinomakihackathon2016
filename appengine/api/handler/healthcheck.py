# -*- coding: utf-8 -*-
from api.base import ApiHandler


class HealthcheckHandler(ApiHandler):
    def get(self):
        value = {
            "msg": u"Alive",
            "method": "get"
        }
        self.write_success(value)
        return

    def post(self):
        value = {
            "msg": u"Alive",
            "method": "post"
        }
        self.write_success(value)
        return
