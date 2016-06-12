#!/usr/bin/python
# coding=utf-8
from flask_restful import (Resource, abort, output_json)
import schema

__all__ = ["Controller", "SpeakersResourceController", "SpeakersUnitController"]


class Controller(Resource):
    """ This Controller Class is a Abstract implementation for
    the child-controller-classes. """

    path_schema = schema.DOMAIN["speakers"]
    header_response = {"Content-Type": "application/json"}

    @classmethod
    def abort_if_path_doesnt_exist(cls, func):
        """ Function to check if the requested url exists or not """

        def does_it_exists(*args, **kwargs):
            if kwargs["path"] not in cls.path_schema:
                abort(http_status_code=404, message="The request url: {} does not exists".format(kwargs["path"]))
            return func(args, kwargs)

        return does_it_exists


class SpeakersResourceController(Controller):
    """ Speakers Controllers Objects """

    def __init__(self):
        self.object_name = "{}".format(self.__class__.__name__)

    @Controller.abort_if_path_doesnt_exist
    def get(self, path):
        from Resources.speaker_resource import SpeakerResource

        response = {
            path["path"]: getattr(SpeakerResource, Controller.path_schema.get(path['path']))()
        }

        return output_json(data=response, code=200, headers=Controller.header_response)

    @Controller.abort_if_path_doesnt_exist
    def post(self, path):
        from Resources.speaker_resource import SpeakerResource

        response = {
            path["path"]: getattr(SpeakerResource, Controller.path_schema.get(path['path']))()
        }

        return output_json(data=response, code=200, headers=Controller.header_response)


class SpeakersUnitController(Resource):
    """ Speakers Unit Controllers Objects """

    def __init__(self):
        super().__init__()
        self.object_name = "{}".format(self.__class__.__name__)

    @Controller.abort_if_path_doesnt_exist
    def get(self, unit_id, *args, **kwargs):
        from Resources.speaker_resource import SpeakerResource

        response = {
            kwargs["path"]: getattr(SpeakerResource, Controller.path_schema.get(kwargs['path']))(unit_id),
        }

        return output_json(data=response, code=200, headers=Controller.header_response)

    @Controller.abort_if_path_doesnt_exist
    def post(self, unit_id, *args, **kwargs):
        from Resources.speaker_resource import SpeakerResource

        response = {
            kwargs["path"]: getattr(SpeakerResource, Controller.path_schema.get(kwargs['path']))(unit_id)
        }

        return output_json(data=response, code=200, headers=Controller.header_response)
