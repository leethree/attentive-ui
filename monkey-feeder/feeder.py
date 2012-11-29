#!/usr/bin/python

import asynchat
import asyncore
import socket

from et_facade import EyeTrackerFacade


class MonkeyFeeder(object):

    _TCP_IP = '127.0.0.1'
    _TCP_PORT = 1080
    _WIDTH = 800
    _HEIGHT = 1205

    # Debug option for printing commands without doing anything.
    _DRY_RUN = False

    def __init__(self):
        self._s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._entered = False
        self._lastx = None
        self._lasty = None

    def __enter__(self):
        if not MonkeyFeeder._DRY_RUN:
            self._s.connect((MonkeyFeeder._TCP_IP, MonkeyFeeder._TCP_PORT))
        EventPubSub.subscribe('data', self.move)
        return self

    def __exit__(self, type, value, traceback):
        self._s.close()
        return False

    def _send_command(self, command):
        if not MonkeyFeeder._DRY_RUN:
            self._s.send(command + '\n')
        print "Sent: ", command

    def move(self, x, y):
        width = MonkeyFeeder._WIDTH
        height = MonkeyFeeder._HEIGHT

        # Mouse is not moved.
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


class MonkeyServer(asyncore.dispatcher):

    _TCP_IP = socket.gethostname() # Use 'localhost' for emulators.
    _TCP_PORT = 10800

    def __init__(self):
        asyncore.dispatcher.__init__(self)
        self._handler = None
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)

    def __enter__(self):
        self.bind((MonkeyServer._TCP_IP, MonkeyServer._TCP_PORT))
        self.listen(1)
        print "Listening to", MonkeyServer._TCP_IP, MonkeyServer._TCP_PORT
        return self

    def __exit__(self, type, value, traceback):
        self.handle_close()
        return False

    def handle_accept(self):
        conn, addr = self.accept()

        # Drop existing connection.
        if self._handler is not None:
            self._handler.handle_close()

        self._handler = MonkeyHandler(conn)
        EventPubSub.publish('conn', addr, self._handler)

    def handle_close(self):
        if self._handler is not None:
            self._handler.handle_close()
        print "Server shutdown."
        self.close()

    def loop(self):
        asyncore.loop(timeout=0, count=1)


class MonkeyHandler(asynchat.async_chat):

    def __init__(self, sock):
        asynchat.async_chat.__init__(self, sock)
        self.ibuffer = []
        self.set_terminator('\n')

    def collect_incoming_data(self, data):
        self.ibuffer.append(data)

    def found_terminator(self):
        message = ''.join(self.ibuffer)
        self.ibuffer = []
        self._process_data(message)

    def handle_close(self):
        print "Connection closed."
        self.close()

    def respond(self, data, endline = True):
        self.push(data)
        if endline: self.push('\n')

    def _process_data(self, message):
        strs = message.split()
        if (len(strs) > 1):
            EventPubSub.publish('cmd-' + strs[0].lower(), *strs[1:])
        else:
            EventPubSub.publish('cmd-' + strs[0].lower())


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


class Conductor(object):
# TODO (LeeThree): Move Conductor to separate file because it's too long.

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
            self._respond('error', error)

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
