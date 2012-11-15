#!/usr/bin/python

import tobii.sdk.eyetracker

from tobii.sdk.types import Point2D


class Calibration(object):

    def __init__(self, queue, callback):
        self._q = queue
        self._callback = callback

    def run(self, tracker, on_calib_done):
        self._tracker = tracker
        self._on_calib_done = on_calib_done
        self._tracker.StartCalibration(
            self._q.bind(self._on_calib_start))
        print "Calibration start."

    def add_point(self, x, y):
        print "Adding point:", x, y
        p = Point2D(x, y)
        self._tracker.AddCalibrationPoint(
            p, self._q.bind(self._on_add_completed))

    def compute(self):
        self._tracker.ComputeCalibration(self._q.bind(self._on_calib_compute))

    def abort(self):
        self._tracker.StopCalibration()
        print "Calibration aborted by user."
        self._on_calib_done(False)

    def _report_event(self, event, *args):
        self._callback(event, *args)

    def _on_calib_start(self, error, r):
        if error:
            print "ERROR: Could not start calibration."
            self._report_event('error', error)
        else:
            self._report_event('started')

    def _on_add_completed(self, error, r):
        if error:
            print "ERROR: Add Calibration Point failed."
            self._report_event('error', error)
        else:
            self._report_event('added')

    def _on_calib_compute(self, error, r):
        self._tracker.StopCalibration()
        if error == 0x20000502:
            print ("Calibration failed because not enough"
                   "data was collected:", error)
            self._report_event('error', error)
            self._on_calib_done(False)
        elif error != 0:
            print "Calibration failed because of a server error:", error
            self._report_event('error', error)
            self._on_calib_done(False)
        else:
            print "Calibration end."
            self._report_event('done')
            self._on_calib_done(True)
