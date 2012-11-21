import asynchat
import asyncore
import socket

from pubsub import EventPubSub


class MonkeyFeeder(object):

    _TCP_IP = '127.0.0.1'
    _TCP_PORT = 1080
    _WIDTH = 480
    _HEIGHT = 800

    # Debug option for printing commands without doing anything.
    _DRY_RUN = True

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

    _TCP_IP = 'localhost' # Use socket.gethostname() for real device.
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
