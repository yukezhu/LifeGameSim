import sys

offset = 2

for filename in sys.argv[1:]:
	print 'Adding offset %d to file %s' % (offset, filename) 
	f = open(filename, 'r')
	height = int(f.readline())
	width = int(f.readline())
	bitmap = f.readlines()
	height = height + 4
	width = width + 4
	f.close()
	f = open(filename, 'w')
	f.write(str(height) + '\n')
	f.write(str(width) + '\n')
	f.write('.\n' * 2)
	for line in bitmap:
		newline = '..' + line[:-1] + '..\n'
		f.write(newline)
	f.write('.\n' * 2)
	f.close()


