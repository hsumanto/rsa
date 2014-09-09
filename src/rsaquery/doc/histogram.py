import math

SCALE = 0.1
N = 5
BASE = 10

def logN(base, value):
    return math.log(value) / math.log(base)



def index(x):
    whole = N * math.floor(logN(BASE, x / SCALE))
    fraction = math.floor((N * math.pow(BASE, logN(BASE, x / SCALE) % 1.0)) / BASE)
    return whole + fraction

def bound(i):
    whole = math.floor(i / N)
    if i % N == 0:
        fraction = 0
    else:
        fraction = 1 + logN(BASE, (i % N) / N)
    return SCALE * math.pow(BASE, whole + fraction)

def lb(x):
    return bound(index(x))




def indexLog(x):
    return math.floor(N * logN(BASE, x / SCALE))

def boundLog(i):
    return SCALE * math.pow(BASE, i / N)

def lbLog(x):
    return boundLog(indexLog(x))

