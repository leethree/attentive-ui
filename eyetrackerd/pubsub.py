class EventPubSub(object):

    UNHANDLED = '~'

    _reg = {}

    @staticmethod
    def publish(topic, *args):
        if topic in EventPubSub._reg:
            for handler in EventPubSub._reg[topic]:
                handler(*args)
        else:
            if topic == EventPubSub.UNHANDLED:
                print "WARNING: Unhandled PubSub topic:", topic, args
            else:
                EventPubSub.publish(EventPubSub.UNHANDLED, topic, *args)

    @staticmethod
    def subscribe(topic, handler):
        if topic in EventPubSub._reg:
            EventPubSub._reg[topic].append(handler)
        else:
            EventPubSub._reg[topic] = [handler]

    # TODO (LeeThree): Unsubscribe is needed for completeness.
