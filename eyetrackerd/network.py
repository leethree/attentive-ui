import asynchat
import asyncore
import socket

import pubsub


class MonkeyFeeder(asynchat.async_chat):

    _TCP_IP = '127.0.0.1'
    _TCP_PORT = 1080

    # Debug option for printing commands without doing anything.
    _DRY_RUN = False

    def __init__(self):
        asynchat.async_chat.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.set_terminator(None)

    def connect_to(self):
        if not MonkeyFeeder._DRY_RUN:
            self.connect((MonkeyFeeder._TCP_IP, MonkeyFeeder._TCP_PORT))

    def handle_connect(self):
        print "Feeder connected."

    def handle_close(self):
        self.close()

    def collect_incoming_data(self, data):
        # Ignores all incoming data.
        pass

    def found_terminator(self):
        pass

    def send_data(self, data):
        if not MonkeyFeeder._DRY_RUN:
            self.push(data + '\n')
        print "Sent: ", data


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
        pubsub.publish('conn', addr, self._handler)

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
            pubsub.publish('cmd-' + strs[0].lower(), *strs[1:])
        else:
            pubsub.publish('cmd-' + strs[0].lower())
