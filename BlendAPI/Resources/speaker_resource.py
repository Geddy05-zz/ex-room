#!/usr/bin/python
# coding=utf-8

# Speakers (hypothetically)
UNITS = {"0": True, "1": True, "2": False, "3": False}


class SpeakerResource(object):
    """ This class is not finished as my college are working
     to controlling the speakers with lower level language(s).
     This resource class is the Object to call the
     required components when finished """

    @classmethod
    def unit_count(cls):
        """
        This method is to check how many speakers there are plugged in.
        :return: integer
        """
        return len(UNITS)

    @classmethod
    def unit_states(cls):
        """
        What is the state of the speakers.
        :return: dictionary with the states of all speakers.
        """
        return UNITS
