#!/usr/bin/python
# coding=utf-8
from flask_restful import (Resource, abort, output_json)
import schema

__all__ = ["Controller", "MediaStateController", "MediaAudioController"]


class Controller(Resource):
    """ This Controller Class is a Abstract implementation for
    the child-controller-classes. E.g: MediaStateController, MediaAudioController """

    path_schema = schema.DOMAIN["media"]
    header_response = {"Content-Type": "application/json"}

    @classmethod
    def abort_if_path_doesnt_exist(cls, func):
        """ Function to check if the requested url exists or not """

        def does_it_exists(*args, **kwargs):
            if kwargs["path"] not in cls.path_schema:
                abort(http_status_code=404, message="The request url: {} does not exists".format(kwargs["path"]))
            return func(args, kwargs)

        return does_it_exists


class MediaStateController(Controller):
    """ This class is inteded to call by paths as endpoints.
    Return a list of values or a single value in JSON format """

    def __init__(self):
        super().__init__()
        self.object_name = "{}".format(self.__class__.__name__)

    @Controller.abort_if_path_doesnt_exist
    def get(self, path):
        """
        This GET method can be called to gather information about the
        components by calling it endpoint. E.g. http://localhost:5000/media/path
        :param path: string
        :return: json
        """
        from Resources.media_resource import MediaRecourse

        response = {
            path["path"]: getattr(MediaRecourse, path["path"])(),
        }

        return output_json(data=response, code=200, headers=Controller.header_response)

    @Controller.abort_if_path_doesnt_exist
    def post(self, path):
        """
        This post endpoint is used to insert or modify data. The Resource Object is called
        from here on out to perform the connection with third-party components
        :param path: string
        :return: json
        """
        from Resources.media_resource import MediaRecourse

        response = {
            path["path"]: getattr(MediaRecourse, path["path"])(),
        }

        return output_json(data=response, code=200, headers=Controller.header_response)


class MediaAudioController(Controller):
    """ This class is intended to control the audio that
    will be called by a GET or POST method. Also it is required to
    receive a value at endpoint of the call. This must be a integer"""

    def __init__(self):
        super().__init__()
        self.object_name = "{}".format(self.__class__.__name__)

    @Controller.abort_if_path_doesnt_exist
    def get(self, song_id, *args, **kwargs):
        """ This method is to get the state of the audio by calling the media_resource object
        :param song_id: int
        :return:
        """
        from Resources.media_resource import MediaRecourse

        response = {
            kwargs["path"]: getattr(MediaRecourse, kwargs["path"])(song_id),
        }

        return output_json(data=response, code=200, headers=Controller.header_response)

    @Controller.abort_if_path_doesnt_exist
    def post(self, song_id, *args, **kwargs):
        """ Only post call with id. E.g. http://localhost:5000/media/path/song_id
        This method is work in progress as my colleges are building the user application """
        from Resources.media_resource import MediaRecourse

        response = {
            kwargs["path"]: getattr(MediaRecourse, kwargs["path"])(song_id),
        }

        return output_json(data=response, code=200, headers=Controller.header_response)
