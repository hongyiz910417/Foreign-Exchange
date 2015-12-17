from bs4 import BeautifulSoup
import urllib2
import json
import urllib
import os
import pickle

def annotate(doc):
    query = doc
    urlPostPrefixSpotlight = "http://spotlight.sztaki.hu:2222/rest/annotate"
    args = urllib.urlencode([("text", query)])
    request = urllib2.Request(urlPostPrefixSpotlight, data=args, headers={"Accept": "application/json"})
    response = urllib2.urlopen(request).read()
    pydict= json.loads(response)
    annotation =  pydict['Resources']

    entries = {}
    for keyword in annotation:
            if keyword["@URI"] not in entries.values():
                entries[keyword["@surfaceForm"]] = keyword["@URI"]
    return entries

count = 0
for direct in os.listdir("articles.A-B"):
    if os.path.isdir("articles.A-B/" + direct) != True:
        continue
    for f in os.listdir("articles.A-B/" + direct):
        if f.endswith(".nxml") != True:
            continue
        with open("articles.A-B/" + direct + "/" + f, 'r') as myfile:
            page=myfile.read().replace('\n', '')
        soup = BeautifulSoup(page, 'html.parser')
        keywords = annotate(soup)
        with open("keywords/" + direct + "@" + f.split(".")[0], 'w+') as f:
            pickle.dump(keywords, f)
        count = count + 1
        print count

