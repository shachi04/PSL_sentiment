import pandas as pd
import os
import random
import scipy as sp
import sklearn
import nltk
from sklearn.svm import LinearSVC
from nltk.classify import NaiveBayesClassifier
from nltk.classify.scikitlearn import SklearnClassifier


path = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds2/fold"
goldstdpath = "~/Documents/Shachi/CMPS209C/reviews/GoldStd/Goldstd3/"
baselinepath = "~/Documents/Shachi/CMPS209C/reviews/Baseline_data/all_phrases_polarity.csv"
baseline_shuffle = "~/Documents/Shachi/CMPS209C/reviews/Baseline_data/all_phrases_polarity_shuffled.csv"


def word_feats(words):
    return dict([(word, True) for word in words])


def svm(trainfeats, testfeats):
	y = []
	accuracy = []
	classif = SklearnClassifier(LinearSVC(C=0.032))
	classif.train(trainfeats)
	print "SVM output"
	print 'train on %d instances, test on %d instances' % (len(trainfeats), len(testfeats))
	y.append( nltk.classify.util.accuracy(classif, testfeats))
	print y


def naivebayes(trainfeats, testfeats):
	classifier = NaiveBayesClassifier.train(trainfeats)
	print "NaiveBayes output"
	print 'train on %d instances, test on %d instances' % (len(trainfeats), len(testfeats))

	print 'accuracy:', nltk.classify.util.accuracy(classifier, testfeats)
	print classifier.show_most_informative_features()




for dir_entry in os.listdir(goldstdpath):
        if not dir_entry.startswith("."):
            df = pd.read_csv(goldstdpath+dir_entry, sep = '\t', names = ["PhraseID","Polarity","Contrast","Previous","Text"])
            header = ['Text','Polarity']
            df.to_csv(baselinepath, sep = '\t', cols= header, header = False, index = False, mode = 'a')

lines = open(baselinepath).readlines()
random.shuffle(lines)
open(baseline_shuffle, 'w').writelines(lines)

total = pd.read_csv(baseline_shuffle, delimiter='\t', names = ["Phrase","Sentiment"])



neg_phr = []
pos_phr = []
for i in range(len(total)):
    if total.Sentiment[i]=="negative":
        neg_phr.append(i)
    else:
        pos_phr.append(i)
        
#print pos_phr
#print neg_phr

negfeats = [(word_feats(x for x in total.Phrase[f].split()), 'neg') for f in neg_phr]
posfeats = [(word_feats(x for x in total.Phrase[f].split()), 'pos') for f in pos_phr]

totalfeats = negfeats + posfeats
random.shuffle(totalfeats)
#print len(totalfeats)
trainfeats = totalfeats[1:(9*len(totalfeats)/10)]
testfeats = totalfeats[(9*len(totalfeats)/10+1):len(totalfeats)]
svm(trainfeats,testfeats)
naivebayes(trainfeats,testfeats)

