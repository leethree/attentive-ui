#!/usr/bin/python

import collections
import math
import pickle


class MovingWindow(object):

    def __init__(self, maxlen):
        self._maxlen = maxlen
        self._window = collections.deque(maxlen=maxlen)

    def clear(self):
        self._window.clear()

    def push(self, value):
        self._window.append(value)

    def __iter__(self):
        return iter(self._window)

    def __len__(self):
        return len(self._window)

    def is_full(self):
        return len(self._window) == self._maxlen

    def get_average(self):
        return sum(self._window) / float(len(self._window))


# Finite impulse response filter
class FirFilter(MovingWindow):

    def __init__(self, filters, normalization=None):
        self._filter = filters
        self._norm = (1.0 / sum(filters) if normalization is None
                      else normalization)
        super(FirFilter, self).__init__(len(self._filter))

    def filter(self, x):
        self.push(x)
        ret = 0.0
        # (right-aligned) reversed zip
        for v, f in zip(reversed(self._window), reversed(self._filter)):
            ret += v * f
        return ret * self._norm


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


# Base class of fixation detectors
class FixationDetector(object):

    def __init__(self, initial=True):
        self._initial_saccade = not initial
        self._saccade = self._initial_saccade

    def clear(self):
        self._saccade = self._initial_saccade

    def is_fixation(self, data_item):
        self._process(data_item, self._saccade)
        return not self._saccade

    def _set_saccade(self, is_saccade):
        self._saccade = is_saccade

    def _process(self, data_item, saccade):
        pass # to be implemented by subclasses


class AccelDetector(FixationDetector):

    def __init__(self):
        super(AccelDetector, self).__init__(True)
        self._time_diff = Differentiator(1)
        self._left_diff = Differentiator(0, self._get_theta)
        self._right_diff = Differentiator(0, self._get_theta)

        # Savitzky-Golay smoothing filters
        self._time_filter = FirFilter([0, -1, 1, 0, 0], 1)
        self._velocity_filter = FirFilter([-3, 12, 17, 12, -3], 1.0 / 35)
        self._accel_filter = FirFilter([-2, -1, 0, 1, 2], 1.0 / 10)
        self._init_params()

    def clear(self):
        super(DispersionDetector, self).clear()
        self._time_diff.clear()
        self._left_diff.clear()
        self._right_diff.clear()
        self._velocity_filter.clear()
        self._accel_filter.clear()
        self._init_params()

    def _process(self, data_item, saccade):
        self._counter += 1
        left, right = data_item

        delta_t = self._time_filter.filter(left.t)
        if delta_t == 0: return

        # use validity to determine weight of each eye in caculation
        theta = (self._left_diff.diff(left.p - left.h) * left.validity +
                 self._right_diff.diff(right.p - right.h) * right.validity)

        dtheta = self._velocity_filter.filter(theta) / delta_t
        ddtheta = self._accel_filter.filter(theta) / delta_t / delta_t
        if saccade:
            if self._counter - self._last_counter > 12:
                # saccade should not be longer than 300ms
                saccade = False
            elif ddtheta < self._threshold and dtheta < 50:
                saccade = False
        if not saccade: # candidate for fixation
            if ddtheta > 200:
                saccade = True
                self._threshold = -ddtheta * 0.6
                self._last_counter = self._counter

        self._set_saccade(saccade)

    def _init_params(self):
        self._last_v = None
        self._last_t = None
        self._counter = 0
        self._last_counter = 0
        self._threshold = -300

    def _get_theta(self, v, last_v):
        try:
            cos = (v * last_v) / (abs(v) * abs(last_v))
            return math.degrees(math.acos(cos))
        except ArithmeticError:
            return 0


class DispersionDetector(FixationDetector):

    def __init__(self):
        super(DispersionDetector, self).__init__(False)
        self._memory = MovingWindow(6) # 6 / 40Hz = 150ms
        self._fix_x = 0
        self._fix_y = 0

    def clear(self):
        super(DispersionDetector, self).clear()
        self._memory.clear()
        self._fix_x = 0
        self._fix_y = 0

    def _process(self, data_item, saccade):
        left, right = data_item
        if left.validity == 0 and right.validity == 0:
            return

        # TODO(LeeThree): Mirror data in monocular cases instead of use data
        # from single eye.
        x = (float(left.p2d.x) * left.validity +
             float(right.p2d.x) * right.validity)
        y = (float(left.p2d.y) * left.validity +
             float(right.p2d.y) * right.validity)

        self._memory.push((x, y))
        if saccade:
            if self._memory.is_full():
                xlist = [x for x, y in self._memory]
                ylist = [y for x, y in self._memory]
                xdispersion = max(xlist) - min(xlist)
                ydispersion = max(ylist) - min(ylist)
                if xdispersion + ydispersion < 0.2:
                    saccade = False
                    self._fix_x = sum(xlist) / len(xlist)
                    self._fix_y = sum(ylist) / len(ylist)
        else:
            x, y = float(left.p2d.x), float(left.p2d.y)
            if abs(self._fix_x - x) + abs(self._fix_y - y) > 0.2:
                saccade = True

        self._set_saccade(saccade)


def get_data():
    from eyetracker.facade import Gaze

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


def printout(x):
    print x


# used for testing
def main():
    from trackerd import FeedProcessor
    processor = FeedProcessor(1000, 1000)
    processor.set_fixation_detector(DispersionDetector())
    processor.set_output_method(printout)
    for item in get_data():
        processor.process(item)


if __name__ == '__main__':
    main()
