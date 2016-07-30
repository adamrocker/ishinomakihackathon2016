from google.appengine.ext import ndb


class PeerData(ndb.Model):
    STATUS_DISABLE = -1
    STATUS_ENABLE = 0

    user = ndb.UserProperty()
    peer_id = ndb.StringProperty()
    status = ndb.IntegerProperty()
    created = ndb.DateTimeProperty(auto_now_add=True)

    @staticmethod
    def create_entity(pid, user=None):
        """
        :param UserProperty user:
        :param str pid:
        :rtype: PeerData
        """
        pdata = PeerData()
        pdata.user = user
        pdata.peer_id = pid
        pdata.status = PeerData.STATUS_ENABLE
        return pdata

    @staticmethod
    def get_entity_by_id(id):
        """
        :param int id:
        :rtype: PeerData or None
        """
        return PeerData.get_by_id(id)

    def get_peer_id(self):
        """
        :rtype: str
        """
        return self.peer_id
