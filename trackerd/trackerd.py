#!/usr/bin/python

import socket

import pubsub
from eyetracker.facade import EyeTrackerFacade
from network import MonkeyServer, MonkeyFeeder


class FeedProcessor(object):

    def __init__(self, width, height, upside_down=False):
        self._width = width
        self._height = height
        self._upside_down = upside_down

        self._entered = False
        self._lastx = None
        self._lasty = None
        self._output_method = None

        self._ofile = open('data.pk', 'wb')

    def __del__(self):
        self._ofile.close()

    def set_output_method(self, output_method):
        self._output_method = output_method

    def process(self, x, y, gaze):
        import pickle
        pickle.dump(gaze, self._ofile)
        return

        width = self._width
        height = self._height

        if self._upside_down:
            # Mirror position
            x = 1 - x
            y = 1 - y

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


class Conductor(object):

    _helper = pubsub.PubSubHelper()

    _DEFAULT_CONF = {
        # Use 'socket.gethostname()' for real devices.
        # Or use 'localhost' for emulators.
        'server_host': 'localhost',
        'server_port': 10800,
        'monkey_host': 'localhost',
        'monkey_port': 1080,
        'display_width': 480,
        'display_height': 800,
        'upside_down': True
        }

    def __init__(self):
        self._config = Conductor._DEFAULT_CONF.copy()

        self._etf = EyeTrackerFacade()
        self._mserver = MonkeyServer(self._config['server_host'],
                                     self._config['server_port'])
        self._mfeeder = None
        self._mhandler = None
        self._fprocessor = None
        self._calib = None

    @_helper.bind_all_handlers
    def main(self):
        with self._mserver:
            with self._etf:
                try:
                    while True:
                        self._mserver.loop()
                        self._etf.loop()
                except KeyboardInterrupt:
                    print "Interrupted by user."

    @_helper.handles('etf')
    def _handle_etf_event(self, event, *args):
        print "ETF Event:", event
        if event == 'connected':
            self._respond('status ready')
        elif event == 'start_tracking':
            self._respond('tracking_started')
        elif event == 'stop_tracking':
            self._respond('tracking_stopped')
        elif event == 'stop_calib':
            self._calib = None
            self._respond('calib_stopped')

    @_helper.handles('calib')
    def _handle_calib_event(self, event, *args):
        print "Calib Event:", event
        if event == 'started':
            self._respond('calib_started')
        elif event == 'added':
            self._respond('calib_added')
        elif event == 'done':
            self._respond('calib_done')
        elif event == 'error':
            self._respond('error', str(args[0]))

    @_helper.handles('conn')
    def _handle_conn(self, addr, mhandler):
        print "Connected by", addr
        self._mhandler = mhandler

    @_helper.handles('cmd-set')
    def _handle_cmd_set(self, param, value):
        print "Set", param, "=", value
        # Try to parse value as int.
        try:
            self._config[param] = int(value)
        except ValueError:
            self._config[param] = value

    #@_helper.handles('cmd-start')
    def _handle_cmd_start(self):
        self._mfeeder = MonkeyFeeder()
        self._mfeeder.connect_to(self._config['monkey_host'],
                                 self._config['monkey_port'])
        print "Feeder connecting..."

    @_helper.handles('cmd-start')
    def _handle_mfeeder_conn(self):
        print "Feeder connected."
        self._fprocessor = FeedProcessor(self._config['display_width'],
                                         self._config['display_height'],
                                         self._config['upside_down'])
        #self._fprocessor.set_output_method(self._mfeeder.send_data)
        pubsub.subscribe('data', self._fprocessor.process)
        self._etf.start_tracking()

    @_helper.handles('cmd-stop')
    def _handle_cmd_stop(self):
        #self._mfeeder.handle_close()
        #self._mfeeder = None
        self._fprocessor.set_output_method(None)
        pubsub.unsubscribe('data', self._fprocessor.process)
        self._etf.stop_tracking()

    @_helper.handles('cmd-bye')
    def _handle_cmd_bye(self):
        if self._mhandler is not None:
            self._mhandler.respond('bye')
            self._mhandler.handle_close()

    @_helper.handles('cmd-status')
    def _handle_cmd_status(self):
        self._respond('status', self._etf.get_status())

    @_helper.handles('cmd-calib_start')
    def _handle_cmd_calib_start(self):
        if self._calib is not None:
            self._calib.abort()
        self._calib = self._etf.start_calibration()

    @_helper.handles('cmd-calib_add')
    def _handle_cmd_calib_add(self, x, y):
        if self._calib is not None:
            if self._config['upside_down']:
                self._calib.add_point(1 - float(x), 1 - float(y))
            else:
                self._calib.add_point(float(x), float(y))

    @_helper.handles('cmd-calib_compute')
    def _handle_cmd_calib_compute(self):
        if self._calib is not None:
            self._calib.compute()

    @_helper.handles('cmd-calib_abort')
    def _handle_cmd_calib_abort(self):
        if self._calib is not None:
            self._calib.abort()

    @_helper.handles(pubsub.UNHANDLED)
    def _handle_unhandled(self, topic, *args):
        print "ERROR: Unhandled topic:", topic

    def _respond(self, command, message=''):
        if self._mhandler is not None:
            response = command + (' ' + message if len(message) > 0 else '')
            self._mhandler.respond(response)


if __name__ == '__main__':
    Conductor().main()
    print "Script terminated."
