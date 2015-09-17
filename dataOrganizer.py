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
f = open(filename, 'rt');
try:
    reader = csv.reader(f)
    curDateTime = None
    count = 0
    bidSum = 0.0
    askSum = 0.0
    bidMax = 0.0
    bidMin = sys.float_info.max
    prevBidSum = None
    for row in reader:
    	newDateTime = round(datetime.datetime.strptime(row[1], "%Y%m%d %H:%M:%S.%f"))
    	if(curDateTime == None):
    		curDateTime = newDateTime
    	if(newDateTime != curDateTime):
    		flag = None
    		if(prevBidSum != None):
    			flag = prevBidSum < bidSum
    		prevBidSum = bidSum
    		print row[0] + "," + curDateTime.strftime("%Y%m%d %H:%M:%S.%f") + "," + str(bidMin) + "," \
    				+ str(bidMax) + "," + str(bidSum / count) + "," + str((askSum - bidSum) / count) + "," + str(flag)
    		curDateTime = newDateTime
    		count = 0
    		bidSum = 0.0
    		askSum = 0.0
    		bidMax = 0.0
    		bidMin = sys.float_info.max
    	count += 1
    	bid = float(row[2])
    	ask = float(row[3])
    	bidSum += bid
    	askSum += ask
    	if(bid > bidMax):
    		bidMax = bid
    	if(bid < bidMin):
    		bidMin = bid
    print row[0] + "," + curDateTime.strftime("%Y%m%d %H:%M:%S.%f") + "," + str(bidMin) + "," \
    				+ str(bidMax) + "," + str(bidSum / count) + "," + str((askSum - bidSum) / count) + "," + str(flag)
finally:
    f.close()

