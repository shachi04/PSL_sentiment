import os

negation_words = ["none","no","cannot","n't", "never", "nowhere", "not", "nothing", "nor", "neither", "nobody","hardly",
                  "least", "merely"]

path = "~/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold"

# count_negation = 0

def senti_score_negation(x):
    s = nltk.pos_tag(nltk.word_tokenize(x))
    scorelist = []
    negation = False
    for i in range(0,len(s)):
        if (len(sw.get_score(s[i][0],wordnet_pos_code(s[i][1])))==0):
                pos_score = 0.0
                neg_score = 0.0
        else:
            if(negation==False) :
                if(s[i-1][0] in negation_words):
                    negation = True
                    print s[i][0]+" has a previous negation word, so flipping"
                    pos_score = sw.get_score(s[i][0],wordnet_pos_code(s[i][1]))[0]['neg']
                    neg_score = sw.get_score(s[i][0],wordnet_pos_code(s[i][1]))[0]['pos']
                else:
                    pos_score = sw.get_score(s[i][0],wordnet_pos_code(s[i][1]))[0]['pos']
                    neg_score = sw.get_score(s[i][0],wordnet_pos_code(s[i][1]))[0]['neg']
                    
            elif(negation == True):
                print " found one negation , so flipping all "
                pos_score = sw.get_score(s[i][0],wordnet_pos_code(s[i][1]))[0]['neg']
                neg_score = sw.get_score(s[i][0],wordnet_pos_code(s[i][1]))[0]['pos']
            
        scorelist.append([(neg_score,pos_score)])
    return scorelist
    
def countnonzero(x):
    nonzero = 0
    for item in x:
        if(item>0.0):
            nonzero = nonzero +1 
    return nonzero


for i in range(1,11):
    readpath = path+str(i)+"/all_text.csv"
    writepath = path+str(i)+"/all_wordnet_soft_negation_all.csv"
    if os.path.isfile(writepath):
        os.remove(writepath)
    
    df = pd.read_csv(readpath, sep='\t', names=['PhraseID', 'Text'], header=False)           
    df['Tokens'] = df.Text.map(lambda x: list(itertools.chain(*senti_score_negation(x))))
    
    
    df['SWN_Neg']= df.Tokens.map(lambda x: sum(list(zip(* x)[0]))/countnonzero((list(zip(* x)[0]))) 
                                 if countnonzero(list(zip(* x)[0]))!=0
                                 else 0.001)
    df['SWN_Pos'] = df.Tokens.map(lambda x: sum(list(zip(* x)[1]))/countnonzero((list(zip(* x)[1]))) 
                                  if countnonzero(list(zip(* x)[1]))!=0
                                 else 0.001)
    header = ["PhraseID","SWN_Neg","SWN_Pos"]
    df.to_csv(writepath, sep='\t', cols = header,index=False,header=False)

for i in range(1,11):
    readpath = path+str(i)+"/all_wordnet_soft_negation_all.csv"
    writepathneg = path+str(i)+"/wordnet_negation_flipall_softneg.csv"
    writepathpos = path + str(i) + "/wordnet_negation_flipall_softpos.csv"
    if os.path.isfile(writepathneg):
        os.remove(writepathneg)
    if os.path.isfile(writepathpos):
        os.remove(writepathpos)
    df = pd.read_csv(readpath, sep = '\t', names = ['PhraseID','NegPolarity','PosPolarity'])
    headerneg = ["PhraseID","NegPolarity"]
    df.to_csv(writepathneg, sep = '\t',cols = headerneg, index = False, header = False)
    headerpos = ["PhraseID", "PosPolarity"]
    df.to_csv(writepathpos , sep = '\t', cols = headerpos, index = False, header = False)
