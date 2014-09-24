#!/usr/bin/env python
# coding: utf-8
import matplotlib.pyplot as plt
import histogram

r = range(1, 1000)
logRegular = map(histogram.lb, r)
log = map(histogram.lbLog, r)

#plt.plot(r, log, r, logRegular)
#plt.show()

fig = plt.figure(1)
fig.patch.set_facecolor('white')

plt.subplot(211)
#plt.plot(r, log)
plt.plot(log, r)
plt.ylabel("value")
plt.xlabel("lower bound")

plt.subplot(212)
#plt.plot(r, logRegular)
plt.plot(logRegular, r)
plt.ylabel("value")
plt.xlabel("lower bound")

plt.annotate('order of magnitude', xy=(100, 100), xytext=(50, 500),
            arrowprops=dict(facecolor='black', shrink=0.05),
            )

plt.show()
