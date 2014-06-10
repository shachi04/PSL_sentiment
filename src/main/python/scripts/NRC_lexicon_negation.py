import pandas as pd 
import numpy as np
import os

readpath = "~/Documents/Shachi/CMPS209C/reviews/NRC-Emotion-Lexicon-v0.92/NRC-emotion-lexicon.txt"


nrcdf = pd.read_table(readpath, sep = '\t', names = ["Word","Polarity","Value"])
nrcdf_neg = nrcdf[nrcdf['Polarity']=="negative"]
nrcdf_pos = nrcdf[nrcdf['Polarity']=="positive"]

#create neg dictionary
neg=[]
neg = nrcdf_neg.Word.tolist()
value = nrcdf_neg.Value.tolist()
negdict = {}
for i in range(0,len(neg)):
    if(not negdict.has_key(neg[i])):
        negdict[neg[i]] = value[i]

#create pos dictionary
pos=[]
pos = nrcdf_pos.Word.tolist()
valuepos = nrcdf_pos.Value.tolist()
posdict = {}
for i in range(0,len(pos)):
    if(not posdict.has_key(pos[i])):
        posdict[pos[i]] = valuepos[i]


negation_words = ["none","no","cannot","n't", "never", "nowhere", "not", "nothing", "nor", "neither", "nobody","hardly",
                  "least", "merely"]

def nrc_score_negation(x):
    s = nltk.word_tokenize(str(x).replace("\"",""))
    scorelist = []
    negation = False
    for i in range(0,len(s)):
        
        if(negdict.has_key(s[i])):
            print " found key " 
            if(negation==False) :
                if(s[i-1] in negation_words):
                    print " found negation word " 
                    negation = True
                    print negation
                    print s[i]+" has a previous negation word, so flipping"
                    pos_score = negdict[s[i]]
                    neg_score = posdict[s[i]]                
                else:     
                    pos_score = posdict[s[i]]
                    neg_score = negdict[s[i]]   
                    
            elif(negation == True):
                print " found one negation , so flipping all "
                pos_score = negdict[s[i]]
                neg_score = posdict[s[i]]
        else:
            print " key not found"
            pos_score = 0
            neg_score = 0
               
        scorelist.append([(neg_score,pos_score)])
    return scorelist

def countnonzero(x):
    nonzero = 0
    for item in x:
        if(item>0.0):
            nonzero = nonzero +1 
    return nonzero

path = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
write = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds2/fold"
for i in range(1,11):

    readpath = path+str(i)+"/all_text.csv"
    writepath = path+str(i)+"/all_NRC_lexicon_negation.csv"
    writepath2 = write + str(i)+"/all_NRC_lexicon_negation.csv"
    if os.path.isfile(writepath):
        os.remove(writepath)
    if os.path.isfile(writepath2):
        os.remove(writepath2)
    df = pd.read_csv(readpath, sep='\t',names = ["PhraseID","Text"], header = False)
    df['Tokens'] = df.Text.map(lambda x: list(itertools.chain(*nrc_score_negation(x))))
    
        
    df['SWN_Neg']= df.Tokens.map(lambda x: sum(list(zip(* x)[0]))/countnonzero((list(zip(* x)[0]))) 
                                      if countnonzero(list(zip(* x)[0]))!=0
                                      else 0.001)
    df['SWN_Pos'] = df.Tokens.map(lambda x: sum(list(zip(* x)[1]))/countnonzero((list(zip(* x)[1]))) 
                                       if countnonzero(list(zip(* x)[1]))!=0
                                      else 0.001)
    header = ["PhraseID","SWN_Neg","SWN_Pos"]
    df.to_csv(writepath, sep='\t', cols = header,index=False,header=False)
    df.to_csv(writepath2, sep='\t', cols = header,index=False,header=False)


#path = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
path = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds2/fold"
writepath2 = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
writepath = "~/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold"

for i in range(1,11):
    readpath = path+str(i)+"/all_NRC_lexicon_negation.csv"
    writepathneg = writepath+str(i)+"/NRC_negation_neg.csv"
    writepathpos = writepath + str(i) + "/NRC_negation_pos.csv"
    writepathneg2 = writepath2+str(i)+"/NRC_negation_neg.csv"
    writepathpos2 = writepath2 + str(i) + "/NRC_negation_pos.csv"
    if os.path.isfile(writepathneg):
        os.remove(writepathneg)
    if os.path.isfile(writepathpos):
        os.remove(writepathpos)
    df = pd.read_csv(readpath, sep = '\t', names = ['PhraseID','NegPolarity','PosPolarity'])
    headerneg = ["PhraseID","NegPolarity"]
    df.to_csv(writepathneg, sep = '\t',cols = headerneg, index = False, header = False)
    df.to_csv(writepathneg2, sep = '\t',cols = headerneg, index = False, header = False)
    headerpos = ["PhraseID", "PosPolarity"]
    df.to_csv(writepathpos , sep = '\t', cols = headerpos, index = False, header = False)
    df.to_csv(writepathpos2 , sep = '\t', cols = headerpos, index = False, header = False)