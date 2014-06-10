scores = {}

import numpy as np
import os
path = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"

readpath = "~/Documents/Shachi/CMPS209C/reviews/subjectivity_clues.csv"
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0,len(lines)):
    s = lines[i].split('\t')
    #print s[0],s[1],s[2],s[3].split("\n")[0]
    if(not scores.has_key((s[0],s[1]))):
         scores[(s[0],s[1])] = (s[2], s[3].split("\n")[0])


def pos(tag):
    if tag.startswith("NN"):
        return "NN"
    elif tag.startswith("VB"):
        return "VB"
    elif tag.startswith("JJ"):
        return "JJ"
    elif tag.startswith("RB"):
        return "RB"
    else:
        return ' '

for i in range(1,11):
    readpath = path+str(i)+"/all_text.csv"
    writepath = path+str(i)+"/all_subjectivity_clues_soft.csv"
    if os.path.isfile(writepath):
        os.remove(writepath)
    
    df = pd.read_csv(readpath, sep='\t', names=['PhraseID', 'Text'], header=False)
    
    

    # Tokenize and obtain wordnet POS tags
    df['Tokens'] = df.Text.map(lambda x: [(a[0].lower(),pos(a[1])) for a in nltk.pos_tag(nltk.word_tokenize(x))])

    
    
    df['neg'] = df.Tokens.map(lambda x: map(sum,zip(*[(float(scores[a][1]),1) if (scores.has_key(a) and scores[a][0]=="negative")
                              else (0.0,0) for a in x])))
    df['pos'] = df.Tokens.map(lambda x: map(sum,zip(*[(float(scores[a][1]),1) if (scores.has_key(a) and scores[a][0]=="positive")
                              else (0.0,0) for a in x])))
    
    df['avgneg'] = df.neg.map(lambda x :x[0]/x[1] if x[1] != 0 else 0.0001)
    df['avgpos'] = df.pos.map(lambda x :x[0]/x[1] if x[1] != 0 else 0.0001)
    
    header = ["PhraseID","avgneg","avgpos"]
    df.to_csv(writepath, sep='\t', cols = header,index=False,header=False)

for i in range(1,11):
    readpath = path+str(i)+"/all_subjectivity_clues_soft.csv"
    writepathneg = path+str(i)+"/subjectivity_neg.csv"
    writepathpos = path + str(i) + "/subjectivity_pos.csv"
    if os.path.isfile(writepathneg):
        os.remove(writepathneg)
    if os.path.isfile(writepathpos):
        os.remove(writepathpos)
    df = pd.read_csv(readpath, sep = '\t', names = ['PhraseID','NegPolarity','PosPolarity'])
    headerneg = ["PhraseID","NegPolarity"]
    df.to_csv(writepathneg, sep = '\t',cols = headerneg, index = False, header = False)
    headerpos = ["PhraseID", "PosPolarity"]
    df.to_csv(writepathpos , sep = '\t', cols = headerpos, index = False, header = False)

  