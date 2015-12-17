from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from sklearn.decomposition import NMF
from collections import defaultdict
from bs4 import BeautifulSoup, NavigableString
import os
import lda
import numpy as np
import csv

corpus = []
titles = []
count = 0
data_folder = 'abstracts'
for f in os.listdir(data_folder):
    try:
        with open(data_folder + "/" + f, 'r') as myfile:
            corpus.append(unicode(myfile.read().replace('\n', ''), "utf-8"))
            titles.append(f)
    except UnicodeDecodeError:
        count = count + 1
 
n_topics = 15
n_top_words = 50
n_features = 6000
 
vectorizer = CountVectorizer(analyzer='word', ngram_range=(1,1), min_df = 0, stop_words = 'english')
matrix =  vectorizer.fit_transform(corpus)
feature_names = vectorizer.get_feature_names()
 
vocab = feature_names
 
model = lda.LDA(n_topics=12, n_iter=500, random_state=1) #30 for abstracts
model.fit(matrix)
topic_word = model.topic_word_
n_top_words = 6
 
# for i, topic_dist in enumerate(topic_word):
#     topic_words = np.array(vocab)[np.argsort(topic_dist)][:-n_top_words:-1]
#     print('Topic {}: {}'.format(i, ' '.join(topic_words)))

doc_topic = model.doc_topic_
with open('model.csv', 'wb') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter=',', quotechar='|', quoting=csv.QUOTE_MINIMAL)
    spamwriter.writerow([x.encode('utf-8') for x in feature_names])
    for y in range(len(model.topic_word_)):
        spamwriter.writerow([x for x in model.topic_word_[y]])
# for i in range(0, len(titles)):
#     print("{}".format(titles[i], doc_topic[i].argmax()))
#     if len(doc_topic[i]) < 3:
#         print doc_topic[i]
#     else:
#         sublist = doc_topic[i].argsort()[::-1][:3]
#         outstr = ""
#         for e in sublist:
#             if len(outstr) != 0:
#                 outstr += ","
#             outstr += e.tostring()
#         print outstr
 