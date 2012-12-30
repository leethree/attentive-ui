#!/usr/bin/python

import collections
import math
import pickle
from decimal import Decimal

class P3(object):

    def __init__(self, x=0, y=0, z=0):
        self.x = x if isinstance(x, Decimal) else Decimal(repr(x))
        self.y = y if isinstance(y, Decimal) else Decimal(repr(y))
        self.z = z if isinstance(z, Decimal) else Decimal(repr(z))

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
        self.pupil = float() # Pupil
        self.validity = long() # Validity

    def __str__(self):
        return ('%.2f:' % ((self.t - 1167612915647489) / 29059.0) +
                '%s|' % self.h +
                '%s,%s,' % (self.p, self.p2d) +
                '%.2f,%d' % (self.pupil, self.validity)
                )

    @staticmethod
    def of(gaze):
        lp = Gaze()
        lp.t = gaze.Timestamp / 1000000.0
        lp.h = P3.of(gaze.LeftEyePosition3D)
        lp.h_relative = P3.of(gaze.LeftEyePosition3DRelative)
        lp.p = P3.of(gaze.LeftGazePoint3D)
        lp.p2d = P3.of(gaze.LeftGazePoint2D)
        lp.pupil = gaze.LeftPupil
        lp.validity = gaze.LeftValidity

        rp = Gaze()
        rp.t = gaze.Timestamp / 1000000.0
        rp.h = P3.of(gaze.RightEyePosition3D)
        rp.h_relative = P3.of(gaze.RightEyePosition3DRelative)
        rp.p = P3.of(gaze.RightGazePoint3D)
        rp.p2d = P3.of(gaze.RightGazePoint2D)
        rp.pupil = gaze.RightPupil
        rp.validity = gaze.RightValidity

        return lp, rp


class FirFilter(object):

    def __init__(self, filters, normalization=1):
        self._filter = [x / normalization for x in filters]
        mem_len = len(self._filter)
        self._mem = collections.deque([0] * mem_len, maxlen=mem_len)

    def clear(self):
        self._mem.extend([0] * len(self._mem))

    def filter(self, x):
        self._mem.append(x)
        ret = 0.0
        for v, f in zip(self._mem, self._filter):
            ret += v * f
        return ret


class Differentiator(object):

    def __init__(self, default, diff_func=lambda x, y: x - y):
        self._last_value = None
        self._default_value = default
        self._diff_func = diff_func

    def clear(self):
        self._last_value = None

    def diff(self, value):
        if self._last_value is not None:
            ret = self._diff_func(value, self._last_value)
        else:
            ret = self._default_value
        self._last_value = value
        return ret


class FixationDetector(object):

    def __init__(self):
        self._time_diff = Differentiator(1)
        self._left_diff = Differentiator(0, self._get_theta)
        self._right_diff = Differentiator(0, self._get_theta)

        # Savitzky-Golay smoothing filters
        filter_h = [-3, 12, 17, 12, -3]
        self._velocity_filter = FirFilter(filter_h, 35.0)
        filter_g = [-3, -2, -1, 0, 1, 2, 3]
        self._accel_filter = FirFilter(filter_g, 28.0)
        self._init_params()

    def clear(self):
        self._time_diff.clear()
        self._left_diff.clear()
        self._right_diff.clear()
        self._velocity_filter.clear()
        self._accel_filter.clear()
        self._init_params()

    def is_fixation(self, data_item):
        self._counter += 1
        left, right = data_item

        delta_t = self._time_diff.diff(left.t)

        theta, weight = 0.0, 0
        # use validity to determine weight of each eye in caculation
        if left.validity < 2:
            theta += self._left_diff.diff(left.p - left.h)
            weight += 1
        if right.validity < 2:
            theta += self._right_diff.diff(right.p - right.h)
            weight += 1

        if weight > 0:
            theta /= weight

        dtheta = self._velocity_filter.filter(theta) / delta_t
        ddtheta = self._accel_filter.filter(dtheta) / delta_t
        if self._saccade:
            if self._counter - self._last_counter > 12:
                # saccade should not be longer than 300ms
                self._saccade = False
            elif ddtheta < -200 and dtheta < 100:
                self._saccade = False
        if not self._saccade: # candidate for fixation
            if ddtheta > 300:
                self._saccade = True
                self._last_counter = self._counter

        print delta_t, theta, dtheta, ddtheta, 1 if self._saccade else 0

        return False if self._saccade else True # fixation if not in saccade

    def _init_params(self):
        self._last_v = None
        self._last_t = None
        self._counter = 0
        self._last_counter = 0
        self._saccade = False

    def _get_theta(self, v, last_v):
        try:
            cos = (v * last_v) / (abs(v) * abs(last_v))
            return math.degrees(math.acos(cos))
        except ArithmeticError:
            return 0


def get_data():
    data_file = open('data.pk', 'rb')

    raw_data = []

    try:
        while(True):
            item = pickle.load(data_file)
            raw_data.append(item)
    except EOFError:
        pass

    data_file.close()

    data = []

    for item in raw_data:
        data.append(Gaze.of(item))

    return data


def main():
    detector = FixationDetector()
    s_indicator = []
    for item in get_data():
        s_indicator.append(0 if detector.is_fixation(item) else 1)


if __name__ == '__main__':
    main()
