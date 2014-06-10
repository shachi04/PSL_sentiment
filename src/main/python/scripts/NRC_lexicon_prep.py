import pandas as pd 
import numpy as np
import os

readpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/NRC-Emotion-Lexicon-v0.92/NRC-emotion-lexicon.txt"


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


path = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"


for i in range(1,11):
    readpath = path+str(i)+"/all_text.csv"
    writepath = path+str(i)+"/all_NRC_lexicon.csv"
    if os.path.isfile(writepath):
       os.remove(writepath)
    
    df = pd.read_csv(readpath, sep='\t', names = ["PhraseID","Text"], header = False)
    
    df['Tokens'] = df.Text.map(lambda x: nltk.word_tokenize(str(x).replace("\"", "")))
    
    df['neg'] = df.Tokens.map(lambda x: map(sum, zip(*[(negdict[a],1) 
                                                       if (negdict.has_key(a) and negdict[a]==1) else (0,0) for a in x])))
    df['pos'] = df.Tokens.map(lambda x: map(sum, zip(*[(posdict[a],1) 
                                         if (posdict.has_key(a) and posdict[a]==1) else (0,0) for a in x])))
    
    df['avgneg'] = df.neg.map(lambda x: x[0]/x[1] if x[1]!=0 else 0.001)
    df['avgpos'] = df.pos.map(lambda x: x[0]/x[1] if x[1]!=0 else 0.001)
    
    header = ["PhraseID","avgneg","avgpos"]
    df.to_csv(writepath, sep='\t', cols = header,index=False,header=False)

writepath = "/Users/girishsk/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold"
read = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
for i in range(1,11):
    readpath = read+str(i)+"/all_NRC_lexicon.csv"
    writepathneg = writepath+str(i)+"/NRC_neg.csv"
    writepathpos = writepath + str(i) + "/NRC_pos.csv"
    if os.path.isfile(writepathneg):
        os.remove(writepathneg)
    if os.path.isfile(writepathpos):
        os.remove(writepathpos)
    df = pd.read_csv(readpath, sep = '\t', names = ['PhraseID','NegPolarity','PosPolarity'])
    headerneg = ["PhraseID","NegPolarity"]
    df.to_csv(writepathneg, sep = '\t',cols = headerneg, index = False, header = False)
    headerpos = ["PhraseID", "PosPolarity"]
    df.to_csv(writepathpos , sep = '\t', cols = headerpos, index = False, header = False)


