import sys
import csv
import datetime

#round datetime by 10 minutes, to floor
def round(tm):
	discard = datetime.timedelta(minutes=tm.minute % 10,
                             seconds=tm.second,
                             microseconds=tm.microsecond)
	rounded = tm - discard
	return rounded

filename = sys.argv[1]
windowSize = int(sys.argv[2])

f = open(filename, 'rt');
try:
    reader = csv.reader(f)
    window = datetime.timedelta(0,windowSize)
    for row in reader:
    	res = datetime.datetime.strptime(row[1], "%Y%m%d %H:%M:%S.%f")
    	#print datetime.datetime.combine(res.date(), datetime.datetime.min.time())
    	#print res + datetime.timedelta(0,windowSize)
    	print round(res)
finally:
    f.close()
