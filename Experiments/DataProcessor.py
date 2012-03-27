from __future__ import division
import pylab
import sys
import datetime

st = 0
ed = 0

cnt = 1
x = []
y = []

now = datetime.datetime.now()

for line in sys.stdin:
	if line.startswith('Time'):
		line = line[line.find(':')+1:]
		ed = float(line)
		cycle = 20 * 1000 / (ed - st)
		x += [cnt]
		y += [cycle]
		cnt = cnt + 1
		#print x, y
	elif line.startswith('Start time'):
		line = line[12:]
		st = float(line)
	else:
		continue

pylab.plot(x, y)
pylab.title('Performance Test Result')
pylab.xlabel('Number of Clients')
pylab.ylabel('Cycles / Sec')
pylab.savefig('result.png')
pylab.show()
