import asynchat
import asyncore
import socket

import pubsub


class MonkeyFeeder(asynchat.async_chat):

    def __init__(self):
        asynchat.async_chat.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.set_terminator(None)

    def connect_to(self, host, port):
        self.connect((host, port))

    def handle_connect(self):
        pubsub.publish('mfeeder-conn')

    def handle_close(self):
        self.close()

    def collect_incoming_data(self, data):
        # Ignores all incoming data.
        pass

    def found_terminator(self):
        pass

    def send_data(self, data):
        self.push(data + '\n')


class MonkeyServer(asyncore.dispatcher):

    def __init__(self, host, port):
        asyncore.dispatcher.__init__(self)
        self._addr = (host, port)
        self._handler = None
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)

    def __enter__(self):
        self.bind(self._addr)
        self.listen(1)
        print "Listening to", self._addr
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
        self._ibuffer = []
        self._connected = True
        self.set_terminator('\n')

    def collect_incoming_data(self, data):
        self._ibuffer.append(data)

    def found_terminator(self):
        message = ''.join(self._ibuffer)
        self._ibuffer = []
        self._process_data(message)

    def handle_close(self):
        if self._connected:
            print "Connection closed."
            self.close()
            self._connected = False

    def respond(self, data, endline = True):
        self.push(data)
        if endline: self.push('\n')

    def _process_data(self, message):
        strs = message.split()
        if (len(strs) > 1):
            pubsub.publish('cmd-' + strs[0].lower(), *strs[1:])
        else:
            pubsub.publish('cmd-' + strs[0].lower())
