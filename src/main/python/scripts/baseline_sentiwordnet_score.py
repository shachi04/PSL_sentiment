import scipy as sp
import sklearn
import nltk
from sklearn.svm import LinearSVC
from nltk.classify.scikitlearn import SklearnClassifier
import os
import pandas as pd
from nltk.corpus import wordnet
import numpy
import operator

write =  "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/Baseline_data/fold"


negtrainall=[]
postrainall=[]
all_results = []
alldf = []

def avg():
    all_results = reduce(operator.add, all_results)
    average = numpy.sum(all_results)/10
    print average

def svm(total_train_feats,total_test_feats):
    y = []
    accuracy = []
    classifier = SklearnClassifier(LinearSVC(C=0.032))
    classifier.train(total_train_feats)
    print 'train on %d instances, test on %d instances' % (len(total_train_feats), len(total_test_feats))
    y.append( nltk.classify.util.accuracy(classifier, total_test_feats))
    print y
    del classifier
    all_results.append(y)

def find_senti_score(words):
    new ={}
    for word in words :
        new[word[0]+"_neg"] = score_neg(word)
        new[word[0]+"_pos"] = score_pos(word)
    return new
      
def score_neg(word):        
    if ( sw.get_score(word[0], wordnet_pos_code(word[1]))!=[]):
        return (sw.get_score(word[0], wordnet_pos_code(word[1]))[0]['neg'])
    else :
        return 0.0

def score_pos(word):        
    if ( sw.get_score(word[0], wordnet_pos_code(word[1]))!=[]):
        return (sw.get_score(word[0], wordnet_pos_code(word[1]))[0]['pos'])
    else :
        return 0.0

def wordnet_pos_code(tag):
    if tag.startswith('NN'):
        return wordnet.NOUN
    elif tag.startswith('VB'):
        return wordnet.VERB
    elif tag.startswith('JJ'):
        return wordnet.ADJ
    elif tag.startswith('RB'):
        return wordnet.ADV
    else:
        return ''

#!/usr/bin/env python
"""
Author : Jaganadh Gopinadhan <jaganadhg@gmail.com>
Copywright (C) : Jaganadh Gopinadhan

 Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
"""

import sys,os
import re

from nltk.corpus import wordnet

class SentiWordNet(object):
    """
    Interface to SentiWordNet
    """
    def __init__(self,swn_file):
        """
        """
        self.swn_file = swn_file
        self.pos_synset = self.__parse_swn_file()

    def __parse_swn_file(self):
        """
        Parse the SentiWordNet file and populate the POS and SynsetID hash
        """
        pos_synset_hash = {}
        swn_data = open(self.swn_file,'r').readlines()
        head_less_swn_data = filter((lambda line: not re.search(r"^\s*#",\
        line)), swn_data)

        for data in head_less_swn_data:
            fields = data.strip().split("\t")
            try:
                pos,syn_set_id,pos_score,neg_score,syn_set_score,\
                gloss = fields
            except:
                print "Found data without all details"
                pass

            if pos and syn_set_score:
                pos_synset_hash[(pos,int(syn_set_id))] = (float(pos_score),\
                float(neg_score))

        return pos_synset_hash

    def get_score(self,word,pos=None):
        """
        Get score for a given word/word pos combination
        """
        senti_scores = []
        synsets = wordnet.synsets(word,pos)
        for synset in synsets:
            if self.pos_synset.has_key((synset.pos,synset.offset)):
                pos_val, neg_val = self.pos_synset[(synset.pos,synset.offset)]
                senti_scores.append({"pos":pos_val,"neg":neg_val,\
                "obj": 1.0 - (pos_val - neg_val),'synset':synset})

        return senti_scores

sw=SentiWordNet("/Users/girishsk/Documents/Shachi/CMPS209C/home/swn/www/admin/dump/SentiWordNet_3.0.0_20130122.txt")



for trainset in range(1,11):
    readpathtrain = write+str(trainset)+"/baseline_softnegpos.csv" 
    alldf.append(pd.read_csv(readpathtrain, sep='\t', names = ["Polarity","NegPolarity","PosPolarity","Text"]))



for cvset in range(0,10):
    #cvset = 2
    #Configuring Train partitions
    
    train_neg_phr = []
    train_pos_phr = []
    negtrainfeats = []
    postrainfeats = []
    total_train_feats = []
    #Initialize empty dataframe
    traindf = pd.DataFrame(columns = ["Polarity","NegPolarity","PosPolarity","Text"])
    
    print "directory used for training " 
    for trainset in range(0,9):
        train_neg_phr = []
        train_pos_phr = []
       
        dirToUse = (cvset+trainset)%10
    #        if(dirToUse==0):
    #            dirToUse = 10
    #     readpathtrain = write+str(dirToUse)+"/baseline_softnegpos.csv"
    #     traindf = pd.read_csv(readpathtrain, sep='\t', names = ["Polarity","NegPolarity","PosPolarity","Text"])
        traindf = traindf.append(alldf[dirToUse], ignore_index = True)
        print dirToUse
        
        
        
        
    for i in range(len(traindf)):
        if traindf.Polarity[i]=="negative":
            train_neg_phr.append(i)
        else:
            train_pos_phr.append(i)
    
    
    negtrainfeats = ([(find_senti_score(x for x in nltk.pos_tag(traindf.Text[f].split())), 'neg') for f in train_neg_phr])
    postrainfeats = ([(find_senti_score(x for x in nltk.pos_tag(traindf.Text[f].split())), 'pos') for f in train_pos_phr])
    print "length of negative train examples " + str(len(negtrainfeats))
    print "length of positive train examples" + str(len(postrainfeats))
    
    total_train_feats = negtrainfeats+postrainfeats
    random.shuffle(total_train_feats)
    
    
    # Configuring test partition
    dirToUse = (cvset+9)%10
    #     if (dirToUse ==0):
    #         dirToUse = 10
    print dirToUse
   test_neg_phr = []
    test_pos_phr = []
    negtestfeats=[]
    postestfeats = []
    total_test_feats = []
    
    testdf = alldf[dirToUse]
    print "using "+str(dirToUse)+ " for test"
    for i in range(len(testdf)):
        if testdf.Polarity[i]=="negative":
            test_neg_phr.append(i)
        else:
            test_pos_phr.append(i)

        
    negtestfeats = ([(find_senti_score(x for x in nltk.pos_tag(testdf.Text[f].split())), 'neg') for f in test_neg_phr])
    postestfeats = ([(find_senti_score(x for x in nltk.pos_tag(testdf.Text[f].split())), 'pos') for f in test_pos_phr])
    print "length of negative test examples " + str(len(negtestfeats))
    print "length of positive test examples " +str(len(postestfeats))
    
    total_test_feats = negtestfeats+postestfeats
    random.shuffle(total_test_feats)
    print len(total_test_feats)
    
    svm(total_train_feats,total_test_feats)

avg(all_results)
    