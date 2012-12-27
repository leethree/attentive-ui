#!/usr/bin/python

import math
import pickle

from tobii.sdk.types import Point2D, Point3D

class P3(object):

    def __init__(self, x=0, y=0, z=0):
        self.x, self.y, self.z = x, y, z

    def __str__(p):
        return '(%.2f,%.2f,%.2f)' % (p.x, p.y, p.z)

    @staticmethod
    def sub(p1, p2):
        return P3(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z)

    @staticmethod
    def dot(v1, v2):
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z

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


def get_data():
    data_file = open('data.pk', 'rb')

    raw_data = []

    try:
        while(True):
            gaze = pickle.load(data_file)
            raw_data.append(gaze)
    except EOFError:
        print "EOF"

    data_file.close()

    data = []

    for gaze in raw_data:
        data.append(Gaze.of(gaze))

    return data


def main():
    # Savitzky-Golay smoothing filters
    filter_h = [-3, 12, 17, 12, -3]
    filter_h = [x / 35.0 for x in filter_h]
    filter_g = [-3, -2, -1, 0, 1, 2, 3]
    filter_g = [x / 28.0 for x in filter_g]

    data = get_data()
    lastv = None
    lastt = None
    theta = []
    deltat = []
    for (left, right) in data:
        v = P3.sub(left.p, left.h)
        if lastv is not None:
            try:
                cos = P3.dot(v, lastv) / math.sqrt(P3.dot(v, v) *
                      P3.dot(lastv, lastv))
                theta_i = math.degrees(math.acos(cos))
            except ArithmeticError:
                theta_i = 0
            deltat.append((left.t - lastt) / 1000000.0)
            theta.append(theta_i)
        else:
            theta.append(0)
            deltat.append(0)

        lastv = v
        lastt = left.t

    print 'deltat =\n', deltat
    print 'theta =\n', theta

    dtheta = []
    for i in xrange(0, len(theta) - len(filter_h)):
        dtheta_i = 0.0
        for j in xrange(0, len(filter_h)):
            dtheta_i = dtheta_i + theta[i + j] * filter_h[j]
        dtheta.append(dtheta_i / deltat[i + len(filter_h)])

    print 'dtheta =\n', dtheta

    ddtheta = []
    for i in xrange(0, len(dtheta) - len(filter_g)):
        ddtheta_i = 0.0
        for j in xrange(0, len(filter_g)):
            ddtheta_i = ddtheta_i + dtheta[i + j] * filter_g[j]
        ddtheta.append(ddtheta_i / deltat[i + len(filter_g)])

    print 'ddtheta =\n', ddtheta

    saccade = False
    lasti = 0
    sign = False
    for i in xrange(0, len(ddtheta)):
        accel = math.fabs(ddtheta[i])
        if saccade:
            if i - lasti > 12: # saccade longer than 300ms
                saccade = False
            elif ddtheta[i] < -200 and dtheta[i] < 100:
                saccade = False #if (ddtheta[i] > 0) is not sign else saccade

        if saccade is not True: # fixation
            if ddtheta[i] > 300:
                saccade = True
                lasti = i
                #sign = True if ddtheta[i] > 0 else False
        #print ddtheta[i], saccade
        print 1 if saccade else 0


if __name__ == '__main__':
    main()
