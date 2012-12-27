#!/usr/bin/python

import collections
from decimal import Decimal
import math
import pickle

from tobii.sdk.types import Point2D, Point3D

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
        self.t = long() # Timestamp

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
        lp.t = gaze.Timestamp
        lp.h = P3.of(gaze.LeftEyePosition3D)
        lp.h_relative = P3.of(gaze.LeftEyePosition3DRelative)
        lp.p = P3.of(gaze.LeftGazePoint3D)
        lp.p2d = P3.of(gaze.LeftGazePoint2D)
        lp.pupil = gaze.LeftPupil
        lp.validity = gaze.LeftValidity

        rp = Gaze()
        rp.t = gaze.Timestamp
        rp.h = P3.of(gaze.RightEyePosition3D)
        rp.h_relative = P3.of(gaze.RightEyePosition3DRelative)
        rp.p = P3.of(gaze.RightGazePoint3D)
        rp.p2d = P3.of(gaze.RightGazePoint2D)
        rp.pupil = gaze.RightPupil
        rp.validity = gaze.RightValidity

        return lp, rp


class FirFilter(object):

    def __init__(self, filters, normalization):
        self._filter = [x / normalization for x in filters]
        mem_len = len(self._filter)
        self._mem = collections.deque([0] * mem_len, maxlen=mem_len)

    def filter(self, x):
        self._mem.append(x)
        ret = 0.0
        for v, f in zip(self._mem, self._filter):
            ret = ret + v * f
        return ret


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
    # Savitzky-Golay smoothing filters
    filter_h = [-3, 12, 17, 12, -3]
    velocity_filter = FirFilter(filter_h, 35.0)
    filter_g = [-3, -2, -1, 0, 1, 2, 3]
    accel_filter = FirFilter(filter_g, 28.0)

    data = get_data()
    lastv = None
    lastt = None
    theta = []
    deltat = []
    for (left, right) in data:
        v = left.p - left.h
        if lastv is not None:
            try:
                cos = (v * lastv) / (abs(v) * abs(lastv))

                theta_i = math.degrees(math.acos(cos))
            except ArithmeticError:
                theta_i = 0
            deltat.append((left.t - lastt) / 1000000.0)
            theta.append(theta_i)
        else:
            theta.append(0)
            deltat.append(1)

        lastv = v
        lastt = left.t

    print 'deltat =\n', deltat, len(deltat)
    print 'theta =\n', theta, len(theta)

    dtheta = []
    for i in xrange(0, len(theta)):
        dtheta_i = velocity_filter.filter(theta[i])
        dtheta.append(dtheta_i / deltat[i])

    print 'dtheta =\n', dtheta, len(dtheta)

    ddtheta = []
    for i in xrange(0, len(dtheta)):
        ddtheta_i = accel_filter.filter(dtheta[i])
        ddtheta.append(ddtheta_i / deltat[i])

    print 'ddtheta =\n', ddtheta, len(ddtheta)

    saccade = False
    lasti = 0
    sign = False
    s_indicator = []
    for i in xrange(0, len(ddtheta)):
        if saccade:
            if i - lasti > 12: # saccade longer than 300ms
                saccade = False
            elif ddtheta[i] < -200 and dtheta[i] < 100:
                saccade = False

        if saccade is not True: # fixation
            if ddtheta[i] > 300:
                saccade = True
                lasti = i
        s_indicator.append(1 if saccade else 0)
    print 's_indicator =\n', s_indicator, len(s_indicator)


if __name__ == '__main__':
    main()
