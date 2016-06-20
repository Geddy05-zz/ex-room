#!/usr/bin/python
from flask_restful import Resource, abort
__all__ = ["Scenes"]

SCENES = {
    'speaker_count': {'post_play_song': 'play the song with ID ... '},
    'speaker_orientation': {'': ''},
    'speaker_volume': {'', ''}
}


def abort_if_scene_doesnt_exist(req):
    """ Function to check if the requested url exists or not """
    if req not in SCENES:
        abort(http_status_code=404, message="The request url: {} does not exists".format(req))


class Scenes(Resource):
    """ Speakers Objects """
    def __init__(self):
        self.object_name = "{}".format(self.__class__.__name__)

    def __str__(self):
        return self.__dict__

    def get(self):
        return "What Box do you want to play"

    def put(self, box_id):
        print("[+] Doing Something")
        return "PUT Request Done"

