#author: Hongyi Zhang(andrew id: hongyiz)


import sys
import csv
import datetime
import glob

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
    prevRow = []
    for row in reader:
    	newDateTime = round(datetime.datetime.strptime(row[1], "%Y%m%d %H:%M:%S.%f"))
    	if curDateTime == None:
    		curDateTime = newDateTime
        #if newDateTime != curDateTime, it means now we are sliding to the next window
    	if newDateTime != curDateTime:
            #flag is the label, if this row's avg bid is lower than the next row, it will be labeled as False
            #else it will be labeled as True
            flag = None
            if len(prevRow) != 0:
                flag = float(prevRow[4]) < (bidSum / count)
                print prevRow[0] + "," + prevRow[1] + "," + prevRow[2] + "," + prevRow[3] + "," + prevRow[4] + "," + prevRow[5] + "," + str(flag)
            prevRow = [row[0], curDateTime.strftime("%Y%m%d %H:%M:%S.%f"), str(bidMin), str(bidMax), str(bidSum / count), str((askSum - bidSum) / count)]
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
    	if bid > bidMax:
    		bidMax = bid
    	if bid < bidMin:
    		bidMin = bid
    #print the remaining line after finishing scanning the file
    print prevRow[0] + "," + prevRow[1] + "," + prevRow[2] + "," + prevRow[3] + "," + prevRow[4] + "," + prevRow[5] + "," + str(None)
finally:
    f.close()

