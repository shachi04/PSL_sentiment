import os
import pandas as pd
import nltk
from nltk.corpus import wordnet
path = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/PSL_data_folds2/fold"
readpath = path+str(i)+"/all_wordnet_polarity_soft.csv"
writepathneg = path+str(i)+"/softneg.csv"
writepathpos = path + str(i) + "/softpos.csv"


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


for i in range(1,11):
    readpath = path+str(i)+"/all_text.csv"
    writepath = path+str(i)+"/all_wordnet_polarity_soft.csv"
    if os.path.isfile(writepath):
        os.remove(writepath)
    
    df = pd.read_csv(readpath, sep='\t', names=['PhraseID', 'Text'], header=True)
    
    # Tokenize and obtain wordnet POS tags
    df['Tokens'] = df.Text.map(lambda x: [(a[0],wordnet_pos_code(a[1])) for a in nltk.pos_tag(nltk.word_tokenize(x))])
    
    # For each wordnet tagged phrase, obtain the sentiwordnet score, sum up the +ve and -ve scores
    df['SWN_Neg']= df.Tokens.map(lambda x: map(sum,zip(*[  
                                     ( (sw.get_score(a[0],a[1]))[0]['neg'],1) 
                                     if  (len(sw.get_score(a[0],a[1])) != 0 
                                          and (sw.get_score(a[0],a[1]))[0]['neg'] != 0.0)
                                     else (0.0,0) 
                                    for a in x])))
    df['SWN_Pos']= df.Tokens.map(lambda x: map(sum,zip(*[  
                                     ( (sw.get_score(a[0],a[1]))[0]['pos'],1) 
                                     if  (len(sw.get_score(a[0],a[1])) != 0 
                                          and (sw.get_score(a[0],a[1]))[0]['pos'] != 0.0 )
                                     else (0.0,0) 
                                    for a in x])))
    # Assign polarity of -1, if negative sentiment is greater than positive, 0 if neutral, 1 if positive. 
    #df['Polarity'] = df.SWN.map(lambda x : -1 if x[0]>x[1] else (1 if x[1]>x[0] else 0))

    df['PosPolarity'] = df.SWN_Pos.map(lambda x :x[0]/x[1] if x[1] != 0 else 0.0001)
    df['NegPolarity'] = df.SWN_Neg.map(lambda x :x[0]/x[1] if x[1] != 0 else 0.0001)
    header = ["PhraseID","NegPolarity","PosPolarity"]
    df.to_csv(writepath, sep='\t', cols = header,index=False,header=False)


for i in range(1,11):
    
    if os.path.isfile(writepathneg):
        os.remove(writepathneg)
    if os.path.isfile(writepathpos):
        os.remove(writepathpos)
    df = pd.read_csv(readpath, sep = '\t', names = ['PhraseID','NegPolarity','PosPolarity'])
    headerneg = ["PhraseID","NegPolarity"]
    df.to_csv(writepathneg, sep = '\t',cols = headerneg, index = False, header = False)
    headerpos = ["PhraseID", "PosPolarity"]
    df.to_csv(writepathpos , sep = '\t', cols = headerpos, index = False, header = False)
