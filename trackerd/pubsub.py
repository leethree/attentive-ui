# Module for publishing and subscribing to events.

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
