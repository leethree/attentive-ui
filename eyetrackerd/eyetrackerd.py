#!/usr/bin/python

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
        self._send_command = self._default_send_command

    def set_output_method(self, output_method):
        self._send_command = (output_method if output_method is not None
                              else self._default_send_command)

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

    def _default_send_command(self, command):
        # Do nothing.
        pass


class Conductor(object):

    def __init__(self):
        self._etf = EyeTrackerFacade(pubsub.publish)
        self._mserver = MonkeyServer()
        self._mfeeder = None
        self._mhandler = None
        self._fprocessor = FeedProcessor()
        self._etready = False
        self._ettracking = False
        self._calib = None
        pubsub.subscribe('etf', self._handle_etf_event)
        pubsub.subscribe('calib', self._handle_calib_event)
        pubsub.subscribe('conn', self._handle_conn)
        pubsub.subscribe('cmd-start', self._handle_cmd_start)
        pubsub.subscribe('cmd-stop', self._handle_cmd_stop)
        pubsub.subscribe('cmd-calib_start', self._handle_cmd_calib_start)
        pubsub.subscribe('cmd-calib_add', self._handle_cmd_calib_add)
        pubsub.subscribe('cmd-calib_compute', self._handle_cmd_calib_compute)
        pubsub.subscribe('cmd-calib_abort', self._handle_cmd_calib_abort)
        pubsub.subscribe('cmd-bye', self._handle_cmd_bye)
        pubsub.subscribe(pubsub.UNHANDLED, self._handle_unhandled)

    def main(self):
        with self._mserver:
            with self._etf:
                try:
                    while True:
                        self._mserver.loop()
                        self._etf.loop()
                except KeyboardInterrupt:
                    print "Interrupted by user."

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

    def _handle_cmd_start(self):
        self._mfeeder = MonkeyFeeder()
        self._mfeeder.connect_to()
        self._fprocessor.set_output_method(self._mfeeder.send_data)
        pubsub.subscribe('data', self._fprocessor.process)
        self._etf.start_tracking()

    def _handle_cmd_stop(self):
        self._mfeeder.handle_close()
        self._mfeeder = None
        self._fprocessor.set_output_method(None)
        pubsub.unsubscribe('data', self._fprocessor.process)
        self._etf.stop_tracking()

    def _handle_cmd_bye(self):
        if self._mhandler is not None:
            self._mhandler.respond('bye')
            self._mhandler.handle_close()

    def _handle_cmd_calib_start(self):
        if self._calib is not None:
            self._calib.abort()
        self._calib = self._etf.start_calibration()

    def _handle_cmd_calib_add(self, x, y):
        if self._calib is not None:
            self._calib.add_point(float(x), float(y))

    def _handle_cmd_calib_compute(self):
        if self._calib is not None:
            self._calib.compute()

    def _handle_cmd_calib_abort(self):
        if self._calib is not None:
            self._calib.abort()

    def _handle_unhandled(self, topic, *args):
        print "Error: unknown command ", topic

    def _respond(self, command, message=''):
        if self._mhandler is not None:
            response = command + (' ' + message if len(message) > 0 else '')
            self._mhandler.respond(response)


if __name__ == '__main__':
    Conductor().main()
    print "Script terminated."
