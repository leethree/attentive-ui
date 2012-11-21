#!/usr/bin/python

from eyetracker_facade import EyeTrackerFacade
from network import MonkeyServer, MonkeyFeeder
from pubsub import EventPubSub


class Conductor(object):

    def __init__(self):
        self._etf = EyeTrackerFacade(EventPubSub.publish)
        self._mserver = MonkeyServer()
        self._mfeeder = MonkeyFeeder()
        self._mhandler = None
        self._etready = False
        self._ettracking = False
        self._calib = None
        EventPubSub.subscribe('etf', self._handle_etf_event)
        EventPubSub.subscribe('calib', self._handle_calib_event)
        EventPubSub.subscribe('conn', self._handle_conn)
        EventPubSub.subscribe('cmd-start', self._handle_cmd_start)
        EventPubSub.subscribe('cmd-stop', self._handle_cmd_stop)
        EventPubSub.subscribe('cmd-calib_start', self._handle_cmd_calib_start)
        EventPubSub.subscribe('cmd-calib_add', self._handle_cmd_calib_add)
        EventPubSub.subscribe('cmd-calib_compute',
                              self._handle_cmd_calib_compute)
        EventPubSub.subscribe('cmd-calib_abort', self._handle_cmd_calib_abort)
        EventPubSub.subscribe('cmd-bye', self._handle_cmd_bye)
        EventPubSub.subscribe(EventPubSub.UNHANDLED, self._handle_unhandled)

    def main(self):
        with self._mserver:
            with self._mfeeder:
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
        self._etf.start_tracking()

    def _handle_cmd_stop(self):
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
        self._respond('error', "Unknown command: " + topic)

    def _respond(self, command, message=''):
        if self._mhandler is not None:
            self._mhandler.respond(command + ' ' + message)


if __name__ == '__main__':
    Conductor().main()
    print "Script terminated."
