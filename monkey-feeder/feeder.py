#!/usr/bin/python

import functools
import Queue
import socket

import tobii.sdk.browsing
import tobii.sdk.eyetracker
import tobii.sdk.mainloop


class EyetrackerFeed(object):

    def __init__(self, queue, callback):
        self.eyetracker = None
        self.eyetrackers = {}
        self.browser = None
        self._q = queue
        self._data_callback = callback

        tobii.sdk.init()
        self.mainloop_thread = tobii.sdk.mainloop.MainloopThread(
            mainloop=None, delay_start=True)

    def __enter__(self):
        self.mainloop_thread.start()
        self.browser = tobii.sdk.browsing.EyetrackerBrowser(
            self.mainloop_thread,
            self._q.bind(self._on_eyetracker_browser_event))
        return self

    def __exit__(self, type, value, traceback):
        if self.eyetracker is not None:
            self.eyetracker.StopTracking()
            self.eyetracker.events.OnGazeDataReceived -= \
                self._q.bind(self._on_gazedata)
        if self.mainloop_thread is not None:
            self.mainloop_thread.stop()
        return False

    def _on_eyetracker_browser_event(self, event_type, event_name,
                                     eyetracker_info):
        if event_type == tobii.sdk.browsing.EyetrackerBrowser.FOUND:
            self.eyetrackers[eyetracker_info.product_id] = eyetracker_info
            print ('%s' % eyetracker_info.product_id, eyetracker_info.model,
                  eyetracker_info.status)
            print "Connecting to:", eyetracker_info
            tobii.sdk.eyetracker.Eyetracker.create_async(
                self.mainloop_thread, eyetracker_info,
                self._q.bind(self._on_eyetracker_created))

        return False

    def _on_eyetracker_created(self, error, eyetracker):
        if error:
            print "Connection failed because of an exception: %s" % (error)
            if error == 0x20000402:
                print ("The selected unit is too old, a unit which supports "
                       "protocol version 1.0 is required.\n\n<b>Details:</b> "
                       "<i>%s</i>" % error)
            else:
                print "Could not connect to eye tracker."
            return False

        self.eyetracker = eyetracker
        print "   --- Connected!"

        if self.eyetracker is not None:
            self.eyetracker.events.OnGazeDataReceived += \
                self._q.bind(self._on_gazedata)
            self.eyetracker.StartTracking()
        return False

    def _on_gazedata(self, error, gaze):
        x = None
        y = None
        if gaze.LeftValidity < 2:
            left = gaze.LeftGazePoint2D
            x = left.x
            y = left.y
        if gaze.RightValidity < 2:
            right = gaze.RightGazePoint2D
            x = (x + right.x) / 2 if x is not None else right.x
            y = (y + right.y) / 2 if y is not None else right.y

        if (x is not None) and (y is not None):
            if self._data_callback is not None:
                self._data_callback(x, y)
        return False


class CallbackQueue(object):

    def __init__(self):
        self._q = Queue.Queue()
        self._counter = 0

    def bind(self, callback):
        return functools.partial(self.put, callback)

    def empty(self):
        return self._q.empty()

    def put(self, callback, *args):
        partial = functools.partial(callback, *args)
        self._q.put(partial)

    def get(self, block=False, timeout=None):
        try:
            return self._q.get(block, timeout)
        except Queue.Empty:
            return None

    def pop(self):
        p = self.get(True, 1)
        if p is not None:
            p()
            self._q.task_done()
            self._counter += 1

    @property
    def counter(self):
        return self._counter


class MonkeyController(object):

    _TCP_IP = '127.0.0.1'
    _TCP_PORT = 1080
    _WIDTH = 480
    _HEIGHT = 800

    _DRY_RUN = True

    def __init__(self):
        self._s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._entered = False
        self._lastx = None
        self._lasty = None

    def __enter__(self):
        if not MonkeyController._DRY_RUN:
            self._s.connect((MonkeyController._TCP_IP,
                             MonkeyController._TCP_PORT))
        return self

    def __exit__(self, type, value, traceback):
        self._s.close()
        pass

    def _send_command(self, command):
        if not MonkeyController._DRY_RUN:
            self._s.send(command + '\n')
        print "Sent: ", command

    def move(self, x, y):
        width = MonkeyController._WIDTH
        height = MonkeyController._HEIGHT

        # TODO (LeeThree): Workaround for emulator window.
        # Remove when ready for actual device.
        x = (x * 1280 - 150) / width
        y = (y * 1024 - 115) / height

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


def main():
    queue = CallbackQueue()
    with MonkeyController() as mctrl:
        with EyetrackerFeed(queue, mctrl.move):
            try:
                while True: queue.pop()
            except KeyboardInterrupt:
                print "Interrupted by user."
                print "%d events processed." % queue.counter

    print "Script terminated."


if __name__ == '__main__':
    main()
