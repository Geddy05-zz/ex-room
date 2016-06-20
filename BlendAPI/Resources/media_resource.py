#!/usr/bin/python
# coding=utf-8

from flask_restful import marshal_with, fields, abort

RESPONSE = {
    "is_playing": fields.Boolean,
    "is_playing_id": fields.Integer,
    "is_stopped": fields.Boolean,
    "is_paused": fields.Boolean,
}


def mark_as_endpoint(func):
    """ This function takes care of endpoint which
    cannot/may not receive any endpoint.values """

    def exists(*args, **kwargs):
        if len(args):
            abort(http_status_code=404, message="The request url: {} does not exists".format(args))
        return func(args, kwargs)

    return exists


class MediaRecourse(object):
    """ This Resource Class is the class which will comunicate with the
     third party components """

    __play_by_id = None
    __is_playing = False
    __is_playing_id = None
    __is_paused = False
    __is_stopped = False

    def __init__(self):
        super().__init__()
        self.object_name = str(self.__class__.__name__)

    @marshal_with(RESPONSE)
    def response(self):
        """
        This method is called within this object as a standardize return statement.
        The return value is first to be approved by it's decorator. All field elements must be
        correct.
        :rtype: dict
        :return: dictionary
        """
        return dict(is_playing=self.__is_playing, is_playing_id=self.__is_playing_id, is_stopped=self.__is_stopped,
                    is_paused=self.__is_paused)

    @classmethod
    def play(cls, song_id=None, *args, **kwargs):
        """ http://...:port/path/play/id to play the audiostream """

        cls.__play_by_id = song_id

        if cls.__play_by_id is not None:
            cls.__is_playing = True
            cls.__is_playing_id = song_id

        return cls.response(cls)

    @classmethod
    def stop(cls, *args, **kwargs):
        """ Stop and set values to default """

        if len(args):
            try:
                abort(http_status_code=404, message="The request url: {} does not exists".format(args[0]))
            except IndexError as e:
                raise "{}".format(e.args)
        else:
            cls.__play_by_id = None
            cls.__is_playing = False
            cls.__is_playing_id = None
            cls.__is_pause = False
            cls.__is_stopped = True

        return cls.response(cls)

    @classmethod
    def pause(cls, *args, **kwargs):
        """ Pause the song """

        if cls.__is_playing:
            cls.__is_playing = False
            cls.__is_paused = True

            return True

        return "Cannot pause, there is nothing playing"

    @classmethod
    def resume(cls, *args, **kwargs):
        """ Void :: Resume if Pause is True"""

        if cls.__is_paused:
            cls.__is_playing = True
            cls.__is_paused = False

        # TODO: Fix this condition first thing
        return True if (cls.__is_paused == True) else "Cannot resume, there is nothing on pause"

    @classmethod
    def is_playing(cls, *args, **kwargs):
        """ Returns Boolean """

        if len(args):
            return {"is_playing_id": True if (cls.__is_playing_id == args[0]) else False}

        return cls.response(cls)
