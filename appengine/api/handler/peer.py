# -*- coding: utf-8 -*-
from api.base import ApiHandler
from entity.peer import PeerData


# /api/v1/peer/register
class PeerRegisterHandler(ApiHandler):
    def get(self):
        return

    def post(self):
        param = self.get_param_as_json()
        pid = param.get("peer_id")
        pdata = PeerData.create_entity(pid)
        pdata.put()
        id = pdata.key.id()
        value = {
            "room_id": id,
            "room_page": u"{}/room/{}".format(self.get_host_url(), id)
        }
        self.write_success(value)
        return


# /api/v1/peer/get/<value>
class PeerGetHandler(ApiHandler):

    def post(self):
        id = self.api_value()
        pdata = PeerData.get_entity_by_id(int(id))
        value = {
            "peer_id": pdata.get_peer_id(),
        }
        self.write_success(value)
        return
