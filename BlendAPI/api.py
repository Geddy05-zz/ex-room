# coding=utf-8
__author__ = "nimbus"
__all__ = ["MainApiObject"]
__version__ = "2.2"

from flask import Flask
from flask_restful import Api, Resource

from Controllers.media_controller import MediaStateController, MediaAudioController
from Controllers.speaker_controller import SpeakersResourceController, SpeakersUnitController

app = Flask(__name__)
api = Api(app)

# Api settings
HOST = ""
PORT = 5000
DEBUG = True

# Routes Media Object
api.add_resource(MediaStateController, '/media/<string:path>', endpoint='media')
api.add_resource(MediaAudioController, '/media/<string:path>/<int:song_id>')

# Routes Speakers Object
api.add_resource(SpeakersResourceController, '/speaker/<string:path>', endpoint='speaker')
api.add_resource(SpeakersUnitController, '/speaker/<string:path>/<int:unit_id>')

if __name__ == '__main__':
        app.run(host=HOST, port=PORT, debug=DEBUG)

