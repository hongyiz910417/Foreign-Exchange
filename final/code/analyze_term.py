import csv
import json

term = raw_input("Please enter the term: ")
print "generating result now........"
scores = []
with open("model.csv", "r") as modelfile:
    reader = csv.reader(modelfile, delimiter = ",")
    count = 0
    for x in reader.next():
    	if x == term :
    		break
    	count += 1
    for row in reader:
    	scores.append(row[count])

name_dict = {"0": "genome sequences", "1": "risk study", "2": "cells", "3": "online users",\
             "4": "patients treatment", "5": "cancer",\
              "6": "infection study", "7": "health care", "8": "patients physical study",\
              "9": "data based methods", "10": "clinical patient study", "11": "genetic expression analysis"}
dic = {'name' : 'root', 'children' : []}

for i in range(0, len(scores)):
	score = scores[i]
	dic['children'].append({'name' : name_dict[str(i)], 'size' : float(score)})

with open("flare_term.json", 'w+') as outfile:
  outfile.write(json.dumps(dic))

print "result generated, please refer to web page for the visualized result!"

