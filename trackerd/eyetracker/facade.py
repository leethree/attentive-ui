import functools
import Queue
from decimal import Decimal

import tobii.sdk.browsing
import tobii.sdk.eyetracker
import tobii.sdk.mainloop

import pubsub
from calibration import Calibration


class EyeTrackerFacade(object):

    def __init__(self):
        self._browser = None
        self._tracker = None
        self._trackers = {}

        self._q = CallbackQueue()

        self._tracking = False
        self._calibrating = False

        tobii.sdk.init()
        self._mainloop = tobii.sdk.mainloop.MainloopThread(
            mainloop=None, delay_start=True)

    def __enter__(self):
        self._mainloop.start()
        self._browser = tobii.sdk.browsing.EyetrackerBrowser(
            self._mainloop,
            self._q.bind(self._on_eyetracker_browser_event))
        return self

    def __exit__(self, type, value, traceback):
        if self._tracker is not None:
            if self._tracking:
                self._tracker.StopTracking()
                self._tracker.events.OnGazeDataReceived -= \
                    self._q.bind(self._on_gazedata)
                self._tracking = False
            if self._calibrating:
                self._tracker.StopCalibration()
                self._calibrating = False
            self._tracker = None
        if self._mainloop is not None:
            self._mainloop.stop()
        print "%d events processed." % self._q.count()
        return False

    def loop(self):
        self._q.pop()

    def get_status(self):
        if self._tracker is None:
            return 'disconnected'
        elif self._tracking:
            return 'tracking'
        elif self._calibrating:
            return 'calibrating'
        else:
            return 'ready'

    def _report_event(self, event, *args):
        pubsub.publish('etf', event, *args)

    def _on_eyetracker_browser_event(self, event_type, event_name,
                                     eyetracker_info):
        if self._tracker is not None:
            # already connected.
            return False
        if event_type == tobii.sdk.browsing.EyetrackerBrowser.FOUND:
            self._trackers[eyetracker_info.product_id] = eyetracker_info
            print ('%s' % eyetracker_info.product_id, eyetracker_info.model,
                  eyetracker_info.status)
            print "Connecting to:", eyetracker_info
            tobii.sdk.eyetracker.Eyetracker.create_async(
                self._mainloop, eyetracker_info,
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

        self._tracker = eyetracker
        print "   --- Connected!"

        self._report_event('connected')
        return False

    def start_tracking(self):
        if self._calibrating or self._tracking:
            return None
        if self._tracker is not None:
            self._tracker.events.OnGazeDataReceived += \
                self._q.bind(self._on_gazedata)
            self._tracker.StartTracking()
            self._tracking = True
            self._report_event('start_tracking')

    def stop_tracking(self):
        if self._tracker is not None:
            self._tracker.StopTracking()
            self._tracker.events.OnGazeDataReceived -= \
                self._q.bind(self._on_gazedata)
            self._tracking = False
            self._report_event('stop_tracking')

    def _on_gazedata(self, error, gaze):
        pubsub.publish('data', Gaze.of(gaze))
        return False

    def start_calibration(self):
        if self._calibrating or self._tracking:
            return None
        if self._tracker is not None:
            calib = Calibration(self._q)
            calib.run(self._tracker, self._on_calib_done)
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


class P3(object):

    def __init__(self, x=0, y=0, z=0):
        self.x = x if isinstance(x, Decimal) else Decimal(repr(x))
        self.y = y if isinstance(y, Decimal) else Decimal(repr(y))
        self.z = z if isinstance(z, Decimal) else Decimal(repr(z))
        # TODO(LeeThree): add property x,y,z as float.

    def __str__(self):
        return '(%.2f,%.2f,%.2f)' % (self.x, self.y, self.z)

    def __sub__(self, other):
        if isinstance(other, P3):
            return P3(self.x - other.x, self.y - other.y, self.z - other.z)
        else:
            return NotImplemented

    def __mul__(self, other):
        if isinstance(other, P3):
            return self.x * other.x + self.y * other.y + self.z * other.z
        else:
            return NotImplemented

    def __abs__(self):
        return (self * self).sqrt()

    @staticmethod
    def of(p):
        x = p.x if hasattr(p, 'x') else 0
        y = p.y if hasattr(p, 'y') else 0
        z = p.z if hasattr(p, 'z') else 0
        return P3(x, y, z)


class Gaze(object):

    def __init__(self):
        self.t = float() # Timestamp

        self.h = P3() # EyePosition3D
        self.h_relative = P3() # EyePosition3DRelative
        self.p = P3() # GazePoint3D
        self.p2d = P3() # GazePoint2D
        self.pupil = 0.0 # Pupil
        self.validity = 0.0 # Validity

    def __str__(self):
        # TODO(LeeThree): Remove magic numbers.
        return ('%.2f:' % ((self.t - 1167612915647489) / 29059.0) +
                '%s|' % self.h +
                '%s,%s,' % (self.p, self.p2d) +
                '%.2f,%d' % (self.pupil, self.validity)
                )

    @staticmethod
    def of(gaze):
        lp = Gaze()
        lp.t = gaze.Timestamp / 1000000.0 # microseconds to seconds
        lp.h = P3.of(gaze.LeftEyePosition3D)
        lp.h_relative = P3.of(gaze.LeftEyePosition3DRelative)
        lp.p = P3.of(gaze.LeftGazePoint3D)
        lp.p2d = P3.of(gaze.LeftGazePoint2D)
        lp.pupil = gaze.LeftPupil

        rp = Gaze()
        rp.t = gaze.Timestamp / 1000000.0
        rp.h = P3.of(gaze.RightEyePosition3D)
        rp.h_relative = P3.of(gaze.RightEyePosition3DRelative)
        rp.p = P3.of(gaze.RightGazePoint3D)
        rp.p2d = P3.of(gaze.RightGazePoint2D)
        rp.pupil = gaze.RightPupil

        if gaze.LeftValidity == 0 and gaze.RightValidity == 0:
            lp.validity, rp.validity = 0.5, 0.5
        elif gaze.LeftValidity < 2:
            lp.validity, rp.validity = 1, 0
        elif gaze.RightValidity < 2:
            lp.validity, rp.validity = 0, 1
        else:
            lp.validity, rp.validity = 0, 0

        return lp, rp
