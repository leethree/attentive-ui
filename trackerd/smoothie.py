#!/usr/bin/python

import math
import pickle

from tobii.sdk.types import Point2D, Point3D

class Gaze(object):

    def __init__(self):
        self.t = long() # Timestamp

        self.h = Point3D() # EyePosition3D
        self.h_relative = Point3D() # EyePosition3DRelative
        self.p = Point3D() # GazePoint3D
        self.p2d = Point2D() # GazePoint2D
        self.pupil = float() # Pupil
        self.validity = long() # Validity

    def __str__(self):
        return ('%.2f:' % ((self.t - 1167612915647489) / 29059.0) +
                '%s|' % _p3d(self.h) +
                '%s,%s,' % (_p3d(self.p), _p2d(self.p2d)) +
                '%.2f,%d' % (self.pupil, self.validity)
                )

def _p2d(p):
    return '(%.2f,%.2f)' % (p.x, p.y)

def _p3d(p):
    return '(%.2f,%.2f,%.2f)' % (p.x, p.y, p.z)


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
        lp = Gaze()
        lp.t = gaze.Timestamp
        lp.h = gaze.LeftEyePosition3D
        lp.h_relative = gaze.LeftEyePosition3DRelative
        lp.p = gaze.LeftGazePoint3D
        lp.p2d = gaze.LeftGazePoint2D
        lp.pupil = gaze.LeftPupil
        lp.validity = gaze.LeftValidity

        rp = Gaze()
        rp.t = gaze.Timestamp
        rp.h = gaze.RightEyePosition3D
        rp.h_relative = gaze.RightEyePosition3DRelative
        rp.p = gaze.RightGazePoint3D
        rp.p2d = gaze.RightGazePoint2D
        rp.pupil = gaze.RightPupil
        rp.validity = gaze.RightValidity
        data.append((lp, rp))

    return data

def sub(p1, p2):
    return Point3D(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z)

def dot(v1, v2):
    return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z

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
        v = sub(left.p, left.h)
        try:
            if lastv is not None:
                cos = dot(v, lastv) / math.sqrt(dot(v, v) * dot(lastv, lastv))
                theta_i = math.degrees(math.acos(cos))
                theta.append(theta_i)
                deltat.append((left.t - lastt) / 1000000.0)
        except ArithmeticError:
            pass
        lastv = v
        lastt = left.t

    print 'deltat =\n', deltat
    print 'theta =\n', theta

    dtheta = []
    for i in xrange(0, len(theta) - len(filter_h)):
        dtheta_i = 0.0
        for j in xrange(0, len(filter_h)):
            dtheta_i = dtheta_i + theta[i + j] * filter_h[j]
        dtheta.append(dtheta_i / deltat[i])

    print 'dtheta =\n', dtheta

    ddtheta = []
    for i in xrange(0, len(dtheta) - len(filter_g)):
        ddtheta_i = 0.0
        for j in xrange(0, len(filter_g)):
            ddtheta_i = ddtheta_i + dtheta[i + j] * filter_g[j]
        ddtheta.append(ddtheta_i / deltat[i])

    print 'ddtheta =\n', ddtheta

    saccade = False
    sign = False
    for i in xrange(0, len(ddtheta)):
        accel = math.fabs(ddtheta[i])
        if accel > 300:
            saccade = True
            sign = True if ddtheta[i] > 0 else False
        elif saccade and accel > 200:
            saccade = False if (ddtheta[i] > 0) is not sign else saccade
        print saccade



if __name__ == '__main__':
    main()
