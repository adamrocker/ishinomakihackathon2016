# -*- coding: utf-8 -*-
from api.base import ApiHandler


class HealthcheckHandler(ApiHandler):
    def get(self):
        value = {"msg": u"Alive"}
        self.write_success(value)
        return
