import os
import itertools
import json

topic_count = 12
input_file = 'output_abstract5.txt'
output_file = 'flare_abstracts.json'

name_dict = {"0": "genome sequences", "1": "risk study", "2": "cells", "3": "online users",\
             "4": "patients treatment", "5": "cancer",\
              "6": "infection study", "7": "health care", "8": "patients physical study",\
              "9": "data based methods", "10": "clinical patient study", "11": "genetic expression analysis"}

with open(input_file) as f:
  content = f.readlines()

def add_to_matrix(matrix, strs):
  if (strs[0] in matrix.keys()) == False:
    #print "into here"
    matrix[strs[0]] = {}
  if (strs[1] in matrix[strs[0]].keys()) == False:
    matrix[strs[0]][strs[1]] = {}
  if (strs[2] in matrix[strs[0]][strs[1]].keys()) == False:
    matrix[strs[0]][strs[1]][strs[2]] = 0
  matrix[strs[0]][strs[1]][strs[2]] += 1

def matrix_to_json(matrix):
  json_dict = {}
  json_dict['name'] = 'root'
  json_dict['children'] = []

  for key1, item1 in matrix.iteritems():
    element1 = {}
    element1['name'] = name_dict[key1]
    element1['children'] = []
    for key2, item2 in item1.iteritems():
      element2 = {}
      element2['name'] = name_dict[key2]
      element2['children'] = []
      for key3, item3 in item2.iteritems():
        element3 = {}
        element3['name'] = name_dict[key3]
        element3['size'] = item3
        element2['children'].append(element3)
      element1['children'].append(element2)
    json_dict['children'].append(element1)

  return json.dumps(json_dict)

topic_matrix = {}
for i in range(0, len(content)):
  if i < topic_count or i % 2 == 0:
    continue
  line = content[i]
  #print line
  strs = line.replace("[", "").replace("]", "").strip().split()
  if len(strs) != 3:
    print line
  permutations = list(itertools.permutations(strs))
  for permutation in permutations:
    add_to_matrix(topic_matrix, permutation)

json_str = matrix_to_json(topic_matrix)

with open(output_file, 'w+') as outfile:
  outfile.write(json_str)
