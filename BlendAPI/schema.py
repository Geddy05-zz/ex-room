from flask_restful import fields

MEDIA_RESPONSE_FIELDS = {
    'play': {
        'status_code', fields.Integer
    },
    'stop': {
        'status_code', fields.Integer
    },
    'pause': {
        'status_code', fields.Integer
    },
    'is_playing': {
        'is_playing': fields.Boolean
    },
    'is_stopped': {
        'is_stopped': fields.Boolean
    },
    'is_pause': {
        'is_pause': fields.Boolean
    }
}

PATH_SPEAKERS = {
    'count': 'unit_count',
    'calibrate': 'calibrate',
    'volume': 'volume',
    'unit': 'unit',
    'state': 'unit_states',
}

PATH_MEDIA = {
    'play': {
        'id': {
            'type': int,
            'minlength': 1,
            'maxlength': 3,
            'required': True
        }
    },
    'is_playing': 'is_playing',
    'stop': 'stop',
    'is_stopped': 'is_stopped',
    'pause': 'pause',
    'resume': 'resume',
}

DOMAIN = {
    'media': PATH_MEDIA,
    'speakers': PATH_SPEAKERS,
}
