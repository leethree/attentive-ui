#!/usr/bin/python

import functools
import Queue

import tobii.sdk.browsing
import tobii.sdk.eyetracker
import tobii.sdk.mainloop

from calibration import Calibration

class EyeTrackerFacade(object):

    def __init__(self, callback):
        self.eyetracker = None
        self.eyetrackers = {}
        self.browser = None
        self._q = CallbackQueue()
        self._data_callback = functools.partial(callback, 'data')
        self._event_callback = functools.partial(callback, 'etf')
        self._calib_callback = functools.partial(callback, 'calib')

        self._tracking = False
        self._calibrating = False

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
            if self._tracking:
                self.eyetracker.StopTracking()
                self.eyetracker.events.OnGazeDataReceived -= \
                    self._q.bind(self._on_gazedata)
                self._tracking = False
            if self._calibrating:
                self.eyetracker.StopCalibration()
                self._calibrating = False
        if self.mainloop_thread is not None:
            self.mainloop_thread.stop()
        print "%d events processed." % self._q.count()
        return False

    def loop(self):
        self._q.pop()

    def _report_event(self, event, *args):
        self._event_callback(event, *args)

    def _on_eyetracker_browser_event(self, event_type, event_name,
                                     eyetracker_info):
        if self.eyetracker is not None:
            # already connected.
            return False
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

        self._report_event('connected')
        return False

    def start_tracking(self):
        if self._calibrating or self._tracking:
            return None
        if self.eyetracker is not None:
            self.eyetracker.events.OnGazeDataReceived += \
                self._q.bind(self._on_gazedata)
            self.eyetracker.StartTracking()
            self._tracking = True
            self._report_event('start_tracking')

    def stop_tracking(self):
        if self.eyetracker is not None:
            self.eyetracker.StopTracking()
            self.eyetracker.events.OnGazeDataReceived -= \
                self._q.bind(self._on_gazedata)
            self._tracking = False
            self._report_event('stop_tracking')

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

    def start_calibration(self):
        if self._calibrating or self._tracking:
            return None
        if self.eyetracker is not None:
            calib = Calibration(self._q, self._calib_callback)
            calib.run(self.eyetracker, self._on_calib_done)
            self._calibrating = True
            return calib

    def _on_calib_done(self, status):
        self._calibrating = False
        self._report_event('stop_calib')


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

    def get(self, block=True, timeout=None):
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
            return True
        else:
            return False

    def popall(self):
        # Pop until queue is empty.
        while self.pop(): pass

    def count(self):
        return self._counter
