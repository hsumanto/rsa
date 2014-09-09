#!/usr/bin/env python
# coding: utf-8
import matplotlib.pyplot as plt
import histogram

r = range(1, 1000)
logQuantile = map(histogram.lb, r)
log = map(histogram.lbLog, r)

#plt.plot(r, log, r, logQuantile)
#plt.show()

fig = plt.figure(1)
fig.patch.set_facecolor('white')

plt.subplot(211)
#plt.plot(r, log)
plt.plot(log, r)

plt.subplot(212)
#plt.plot(r, logQuantile)
plt.plot(logQuantile, r)

plt.annotate('order of magnitude', xy=(100, 100), xytext=(50, 500),
            arrowprops=dict(facecolor='black', shrink=0.05),
            )

plt.show()
