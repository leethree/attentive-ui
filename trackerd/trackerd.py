#!/usr/bin/python

import socket

import pubsub
from eyetracker.facade import EyeTrackerFacade
from network import MonkeyServer, MonkeyFeeder
from smoothie import MovingWindow, FixationDetector, AccelDetector, DispersionDetector


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

        self._avg_enabled = True

        # moving averagers
        self._moving_avg_x = MovingWindow(15)
        self._moving_avg_y = MovingWindow(15)

    def set_fixation_detector(self, fixation_detector):
        self._detector = fixation_detector

    def set_output_method(self, output_method):
        self._output_method = output_method

    def process(self, gaze):
        left, right = gaze

        if left.validity == 0 and right.validity == 0:
            return

        # TODO(LeeThree): Mirror data in monocular cases instead of use data
        # from single eye.
        x = (float(left.p2d.x) * left.validity +
             float(right.p2d.x) * right.validity)
        y = (float(left.p2d.y) * left.validity +
             float(right.p2d.y) * right.validity)

        if self._upside_down:
            # Mirror position
            x = 1 - x
            y = 1 - y

        if self._detector.is_fixation(gaze):

            if self._avg_enabled:
                self._moving_avg_x.push(x)
                self._moving_avg_y.push(y)
                x = self._moving_avg_x.get_average()
                y = self._moving_avg_y.get_average()

            # do nothing if the point hasn't moved
            if (abs(x - self._lastx) * self._width < 5 and
                abs(y - self._lasty) * self._height < 5):
                return

            self._lastx = x
            self._lasty = y

            if -0.1 < x < 1.1 and -0.1 < y < 1.1:
                action = 'move' if self._entered else 'enter'
                self._send_command(action, x, y)
                self._entered = True

            elif self._entered:
                self._send_command('move', x, y)
                self._send_command('exit', x, y)
                self._entered = False

        elif self._entered: # start saccade
            self._moving_avg_x.clear()
            self._moving_avg_y.clear()
            self._send_command('exit', self._lastx, self._lasty)
            self._entered = False


    def _send_command(self, command, x, y):
        if self._output_method is not None:
            self._output_method('hover %s %d %d' % (
                                command, x * self._width, y * self._height))
        # print '%s %d %d' % (command, x * self._width, y * self._height)


class Switchboard(object):

    _helper = pubsub.PubSubHelper()

    _EMULATOR_MODE = False

    _DEFAULT_CONF = {
        # Use 'socket.gethostname()' for real devices.
        # Or use 'localhost' for emulators.
        'server_host': 'localhost' if _EMULATOR_MODE else socket.gethostname(),
        'server_port': 10800,
        'monkey_host': 'localhost',
        'monkey_port': 1080,
        'display_width': 480,
        'display_height': 800,
        'upside_down': False if _EMULATOR_MODE else True
        }

    def __init__(self):
        self._config = Switchboard._DEFAULT_CONF.copy()

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
                while True:
                    self._mserver.loop()
                    self._etf.loop()

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
        self._fprocessor.set_fixation_detector(DispersionDetector())
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
    try:
        Switchboard().main()
    except KeyboardInterrupt:
        print "Interrupted by user."
    print "Script terminated."
