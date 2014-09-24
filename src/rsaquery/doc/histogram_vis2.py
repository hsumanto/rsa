#!/usr/bin/env python
# coding: utf-8
from itertools import chain
import matplotlib.pyplot as plt

import histogram

indices = range(0, 21)
bounds = map(histogram.boundLog, indices)

xs = chain(*zip(bounds, bounds[1:], [float('NaN')] * len(bounds)))
xs = list(xs)
ys = chain(*zip(bounds, bounds[:-1], [float('NaN')] * len(bounds)))
ys = list(ys)

fig = plt.figure(1)
fig.patch.set_facecolor('white')

plt.subplot(211)
#plt.plot(r, log)
plt.plot(xs, ys)
plt.xlabel("value")
plt.ylabel("lower bound")

bounds = map(histogram.bound, indices)

xs = chain(*zip(bounds, bounds[1:], [float('NaN')] * len(bounds)))
xs = list(xs)
ys = chain(*zip(bounds, bounds[:-1], [float('NaN')] * len(bounds)))
ys = list(ys)

plt.subplot(212)
#plt.plot(r, logRegular)
plt.plot(xs, ys)
plt.xlabel("value")
plt.ylabel("lower bound")

plt.annotate('order of magnitude', xy=(100, 100), xytext=(50, 500),
            arrowprops=dict(facecolor='black', shrink=0.05),
            )

plt.show()
