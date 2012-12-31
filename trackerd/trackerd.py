#!/usr/bin/python

import socket

import pubsub
from eyetracker.facade import EyeTrackerFacade
from network import MonkeyServer, MonkeyFeeder
from smoothie import MovingWindow, FixationDetector


class FeedProcessor(object):

    def __init__(self, width, height, upside_down=False):
        self._width = width
        self._height = height
        self._upside_down = upside_down

        self._entered = False
        self._lastx = 0.0
        self._lasty = 0.0
        self._last_weight = 0
        self._output_method = None

        self._detector = FixationDetector()

        # moving averagers
        self._moving_avg_x = MovingWindow(7)
        self._moving_avg_y = MovingWindow(7)

    def set_output_method(self, output_method):
        self._output_method = output_method

    def process(self, gaze):
        left, right = gaze
        x = None
        y = None

        if left.validity < 2:
            x = left.p2d.x
            y = left.p2d.y
        if right.validity < 2:
            x = (x + right.p2d.x) / 2 if x is not None else right.p2d.x
            y = (y + right.p2d.y) / 2 if y is not None else right.p2d.y

        if (x is None) or (y is None):
            return

        # convert to ordinary float
        x = float(x)
        y = float(y)

        is_fixation = self._detector.is_fixation(gaze)

        if self._upside_down:
            # Mirror position
            x = 1 - x
            y = 1 - y

        if is_fixation:
            self._moving_avg_x.push(x)
            x = self._moving_avg_x.get_average()
            self._moving_avg_y.push(y)
            y = self._moving_avg_y.get_average()

            # do nothing if the point hasn't moved
            if (abs(x - self._lastx) * self._width < 1 and
                abs(y - self._lasty) * self._height < 1):
                return
            self._lastx = x
            self._lasty = y

            if x > 0 and x < 1 and y > 0 and y < 1:
                action = 'move' if self._entered else 'enter'
                self._send_command(action, x, y)
                self._entered = True

            elif self._entered:
                self._send_command('move', x, y)
                self._send_command('exit', x, y)
                self._entered = False

        else: # saccade
            self._moving_avg_x.clear()
            self._moving_avg_y.clear()
            if (self._entered):
                self._send_command('exit', x, y)
                self._entered = False


    def _send_command(self, command, x, y):
        if self._output_method is not None:
            self._output_method('hover %s %d %d' % (
                                command, x * self._width, y * self._height))


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
        'upside_down': False
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

    @_helper.handles('cmd-start')
    def _handle_cmd_start(self):
        self._mfeeder = MonkeyFeeder()
        self._mfeeder.connect_to(self._config['monkey_host'],
                                 self._config['monkey_port'])
        print "Feeder connecting..."

    @_helper.handles('mfeeder-conn')
    def _handle_mfeeder_conn(self):
        print "Feeder connected."
        self._fprocessor = FeedProcessor(self._config['display_width'],
                                         self._config['display_height'],
                                         self._config['upside_down'])
        self._fprocessor.set_output_method(self._mfeeder.send_data)
        pubsub.subscribe('data', self._fprocessor.process)
        self._etf.start_tracking()

    @_helper.handles('cmd-stop')
    def _handle_cmd_stop(self):
        self._mfeeder.handle_close()
        self._mfeeder = None
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
