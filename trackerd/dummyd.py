#!/usr/bin/python

import pubsub
from trackerd import Conductor, PubSubHelper
from network import MonkeyServer

class DummyConductor(object):

    _helper = PubSubHelper()

    def __init__(self):
        self._config = Conductor._DEFAULT_CONF.copy()

        self._mserver = MonkeyServer(self._config['server_host'],
                                     self._config['server_port'])
        self._mhandler = None

        self._etready = True
        self._ettracking = False
        self._calib = False

    @_helper.bind_all_handlers
    def main(self):
        with self._mserver:
            try:
                while True:
                    self._mserver.loop()
            except KeyboardInterrupt:
                print "Interrupted by user."

    @_helper.handles('data')
    def _handle_data(self, *args):
        print args

    @_helper.handles('conn')
    def _handle_conn(self, addr, mhandler):
        print "Connected by", addr
        self._mhandler = mhandler

    @_helper.handles('cmd-set')
    def _handle_cmd_set(self, param, value):
        print "Set", param, "=", value
        self._config[param] = value

    @_helper.handles('cmd-start')
    def _handle_cmd_start(self):
        self._ettracking = True
        self._respond('tracking_started')

    @_helper.handles('cmd-stop')
    def _handle_cmd_stop(self):
        self._ettracking = False
        self._respond('tracking_stopped')

    @_helper.handles('cmd-status')
    def _handle_cmd_status(self):
        if self._ettracking:
            self._respond('status', 'tracking')
        elif self._calib:
            self._respond('status', 'calibrating')
        elif self._etready:
            self._respond('status', 'ready')
        else:
            self._respond('status', 'disconnected')

    @_helper.handles('cmd-calib_start')
    def _handle_cmd_calib_start(self):
        self._calib = True
        self._respond('calib_started')

    @_helper.handles('cmd-calib_add')
    def _handle_cmd_calib_add(self, x, y):
        self._respond('calib_added')

    @_helper.handles('cmd-calib_compute')
    def _handle_cmd_calib_compute(self):
        self._respond('calib_done')
        self._calib = False
        self._respond('calib_stopped')

    @_helper.handles('cmd-calib_abort')
    def _handle_cmd_calib_abort(self):
        self._respond('calib_stopped')

    @_helper.handles(pubsub.UNHANDLED)
    def _handle_unhandled(self, topic, *args):
        print "ERROR: Unhandled topic:", topic

    def _respond(self, command, message=''):
        if self._mhandler is not None:
            response = command + (' ' + message if len(message) > 0 else '')
            self._mhandler.respond(response)


if __name__ == '__main__':
    print "Running in dummy mode."
    DummyConductor().main()
    print "Script terminated."
