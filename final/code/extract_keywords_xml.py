import os
import xml.etree.ElementTree

count = 0
for direct in os.listdir("articles.A-B"):
    if os.path.isdir("articles.A-B/" + direct) != True:
        continue
    for f in os.listdir("articles.A-B/" + direct):
        if f.endswith(".nxml") != True:
            continue
        e = xml.etree.ElementTree.parse("articles.A-B/" + direct + "/" + f).getroot()
        keys = ""
        abstract = ""
        for key in e.iter('kwd'):
            if key.text != None:
                try:
                    key.text.decode('ascii')
                    if len(keys) != 0:
                        keys = keys + ','
                    keys = keys + key.text
                except UnicodeEncodeError:
                    print "uho"
        for abst in e.iter('abstract'):
            for p in abst.iter('p'):
                if p.text != None:
                    try:
                        p.text.decode('ascii')
                        abstract = abstract + p.text
                    except UnicodeEncodeError:
                        print "uho"
        if len(keys) != 0:
            with open("keywords2/" + direct + "@" + f.split(".")[0], 'w+') as f1:
                f1.write(keys)
        if len(abstract) != 0:
            with open("abstracts/" + direct + "@" + f.split(".")[0], 'w+') as f2:
                f2.write(abstract)

