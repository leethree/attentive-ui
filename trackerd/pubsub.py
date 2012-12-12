# Module for publishing and subscribing to events.

import functools


UNHANDLED = '~'

_reg = {}


def publish(topic, *args):
    if topic in _reg:
        for handler in _reg[topic]:
            handler(*args)
    else:
        if topic == UNHANDLED:
            print "WARNING: Unhandled PubSub topic:", topic, args
        else:
            publish(UNHANDLED, topic, *args)


def subscribe(topic, handler):
    if topic in _reg:
        _reg[topic].append(handler)
    else:
        _reg[topic] = [handler]


def unsubscribe(topic, handler):
    if topic in _reg:
        if handler in _reg[topic]:
            _reg[topic].remove(handler)


class PubSubHelper(object):

    def __init__(self):
        self._handlers = {}

    def handles(self, topic):
        # Decorator for handlers.
        def decorator(handler):
            self._handlers[topic] = handler
            return handler
        return decorator

    def bind_all_handlers(self, func):
        # Decorator for subscribe and unsubscribe all handlers.
        @functools.wraps(func)
        def wrapper(instance, *args, **kwargs):
            for topic, handler in self._handlers.iteritems():
                subscribe(topic, handler.__get__(instance))
            ret = func(instance, *args, **kwargs)
            for topic, handler in self._handlers.iteritems():
                unsubscribe(topic, handler.__get__(instance))
            return ret
        return wrapper
