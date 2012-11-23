#!/usr/bin/python

import functools

import pubsub
from eyetracker_facade import EyeTrackerFacade
from network import MonkeyServer, MonkeyFeeder


class FeedProcessor(object):

    _WIDTH = 480
    _HEIGHT = 800

    def __init__(self):
        self._entered = False
        self._lastx = None
        self._lasty = None
        self._output_method = None

    def set_output_method(self, output_method):
        self._output_method = output_method

    def process(self, x, y):
        width = FeedProcessor._WIDTH
        height = FeedProcessor._HEIGHT

        # Point is not moved.
        if (x == self._lastx and y == self._lasty):
            return False

        if (x > 0 and x < 1 and y > 0 and y < 1):
            action = 'move' if self._entered else 'enter'
            self._send_command('hover %s %d %d' % (
                               action, x * width, y * height))
            self._entered = True

        elif (self._entered):
            self._send_command('hover move %d %d' % (x * width, y * height))
            self._send_command('hover exit %d %d' % (x * width, y * height))
            self._entered = False

        self._lastx = x
        self._lasty = y

        return True

    def _send_command(self, command):
        if self._output_method is not None:
            self._output_method(command)


class _handles(object):
    # Decorator for handlers.

    _reg = {}

    def __init__(self, topic):
        self._topic = topic

    def __call__(self, handler):
        _handles._reg[self._topic] = handler
        return handler

    @staticmethod
    def subscribe_all(instance):
        for topic, handler in _handles._reg.iteritems():
            pubsub.subscribe(topic, functools.partial(handler, instance))


class Conductor(object):

    def __init__(self):
        self._etf = EyeTrackerFacade()
        self._mserver = MonkeyServer()
        self._mfeeder = None
        self._mhandler = None
        self._fprocessor = FeedProcessor()
        self._etready = False
        self._ettracking = False
        self._calib = None

        # Register all handlers.
        _handles.subscribe_all(self)

    def main(self):
        with self._mserver:
            with self._etf:
                try:
                    while True:
                        self._mserver.loop()
                        self._etf.loop()
                except KeyboardInterrupt:
                    print "Interrupted by user."

    @_handles('etf')
    def _handle_etf_event(self, event, *args):
        print "ETF Event:", event
        if event == 'connected':
            self._etready = True
            self._respond('ready')
        elif event == 'start_tracking':
            self._ettracking = True
            self._respond('tracking_started')
        elif event == 'stop_tracking':
            self._ettracking = False
            self._respond('tracking_stopped')
        elif event == 'stop_calib':
            self._calib = None
            self._respond('calib_stopped')

    @_handles('calib')
    def _handle_calib_event(self, event, *args):
        print "Calib Event:", event
        if event == 'started':
            self._respond('calib_started')
        elif event == 'added':
            self._respond('calib_added')
        elif event == 'done':
            self._respond('calib_done')
        elif event == 'error':
            self._respond('error', args[0])

    @_handles('conn')
    def _handle_conn(self, addr, mhandler):
        print "Connected by", addr
        self._mhandler = mhandler
        # Report current status upon connection.
        if self._etready:
            self._respond('ready')
            if self._ettracking:
                self._respond('tracking_started')
        else:
            self._respond('not_connected')

    @_handles('cmd-start')
    def _handle_cmd_start(self):
        self._mfeeder = MonkeyFeeder()
        self._mfeeder.connect_to()
        self._fprocessor.set_output_method(self._mfeeder.send_data)
        pubsub.subscribe('data', self._fprocessor.process)
        self._etf.start_tracking()

    @_handles('cmd-stop')
    def _handle_cmd_stop(self):
        self._mfeeder.handle_close()
        self._mfeeder = None
        self._fprocessor.set_output_method(None)
        pubsub.unsubscribe('data', self._fprocessor.process)
        self._etf.stop_tracking()

    @_handles('cmd-bye')
    def _handle_cmd_bye(self):
        if self._mhandler is not None:
            self._mhandler.respond('bye')
            self._mhandler.handle_close()

    @_handles('cmd-calib_start')
    def _handle_cmd_calib_start(self):
        if self._calib is not None:
            self._calib.abort()
        self._calib = self._etf.start_calibration()

    @_handles('cmd-calib_add')
    def _handle_cmd_calib_add(self, x, y):
        if self._calib is not None:
            self._calib.add_point(float(x), float(y))

    @_handles('cmd-calib_compute')
    def _handle_cmd_calib_compute(self):
        if self._calib is not None:
            self._calib.compute()

    @_handles('cmd-calib_abort')
    def _handle_cmd_calib_abort(self):
        if self._calib is not None:
            self._calib.abort()

    @_handles(pubsub.UNHANDLED)
    def _handle_unhandled(self, topic, *args):
        print "Error: unhandled topic ", topic

    def _respond(self, command, message=''):
        if self._mhandler is not None:
            response = command + (' ' + message if len(message) > 0 else '')
            self._mhandler.respond(response)


if __name__ == '__main__':
    Conductor().main()
    print "Script terminated."
