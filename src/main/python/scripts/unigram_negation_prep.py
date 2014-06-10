negation_words = ["none","no","cannot","n't", "never", "nowhere", "not", "nothing", "nor", "neither", "nobody","hardly",
                  "least", "merely"]
import itertools
def unigram_score_negation(x, category):
    if(category=="cell"):
        negdict = negcelldict
        posdict = poscelldict
    elif(category=="kitchen"):
        negdict = negkitchendict
        posdict = poskitchendict
    elif(category == "gourmet"):
        negdict = neggourmetdict
        posdict = posgourmetdict
    s = nltk.word_tokenize(str(x).lower().replace("\"","").replace(".",""))
    scorelist = []
    negation = False
    for i in range(0,len(s)):
        if(negdict.has_key(s[i]) or posdict.has_key(s[i])):
  #          print " found key " 
            if(negation==False) :
                if(s[i-1] in negation_words):
 #                   print " found negation word " 
                    negation = True
                    print negation
 #                   print s[i]+" has a previous negation word, so flipping"
                    if(negdict.has_key(s[i])):
                        pos_score = negdict[s[i]]
                    else:
                        pos_score = 0
                    if(posdict.has_key(s[i])):
                        neg_score = posdict[s[i]] 
                    else:
                        neg_score = 0
                else:   
                    if(posdict.has_key(s[i])):
                        pos_score = posdict[s[i]]
                    else:
                        pos_score = 0
                    if(negdict.has_key(s[i])):
                        neg_score = negdict[s[i]]  
                    else:
                        neg_score = 0
                    
            elif(negation == True):
   #             print " found one negation , so flipping all "
                if(negdict.has_key(s[i])):
                    pos_score = negdict[s[i]]
                else: 
                    pos_score = 0
                if(posdict.has_key(s[i])):
                    neg_score = posdict[s[i]]
                else:
                    neg_score = 0
        else:
    #        print " key not found"
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

for i in range(1,11):
    #i = 5
    readpath = path+str(i)+"/all_text.csv"
    writepath = path+str(i)+"/all_unigram_lexicon_negation.csv"
    print writepath
    if os.path.isfile(writepath):
       os.remove(writepath)
    
    df = pd.read_csv(readpath, sep='\t', names = ["PhraseID","Text"], header = False)
    
    dfcell = df[df.PhraseID.str.contains("cell")]
    dfgourmet = df[df.PhraseID.str.contains("gourmet")]
    dfkitchen = df[df.PhraseID.str.contains("kitchen")]
    dfcell['Tokens'] = dfcell.Text.map(lambda x: list(itertools.chain(*unigram_score_negation(x,"cell"))) )
    dfgourmet['Tokens'] = dfgourmet.Text.map(lambda x: list(itertools.chain(*unigram_score_negation(x,"gourmet"))))
    dfkitchen['Tokens'] = dfkitchen.Text.map(lambda x: list(itertools.chain(*unigram_score_negation(x, "kitchen"))))
    
    
    
    dfcell['Neg']= dfcell.Tokens.map(lambda x: ((sum(list(zip(* x)[0]))/countnonzero((list(zip(* x)[0]))) 
                                          if (countnonzero(list(zip(* x)[0]))!=0)
                                          else 0.001) if x!=[] else 0.001))
    dfcell['Pos'] = dfcell.Tokens.map(lambda x: ((sum(list(zip(* x)[1]))/countnonzero((list(zip(* x)[1]))) 
                                          if (countnonzero(list(zip(* x)[1]))!=0)
                                          else 0.001) if x!=[] else 0.001))
    dfkitchen['Neg']= dfkitchen.Tokens.map(lambda x: ((sum(list(zip(* x)[0]))/countnonzero((list(zip(* x)[0]))) 
                                          if (countnonzero(list(zip(* x)[0]))!=0)
                                          else 0.001) if x!=[] else 0.001))
    dfkitchen['Pos'] = dfkitchen.Tokens.map(lambda x: ((sum(list(zip(* x)[1]))/countnonzero((list(zip(* x)[1]))) 
                                          if (countnonzero(list(zip(* x)[1]))!=0)
                                          else 0.001) if x!=[] else 0.001))
    dfgourmet['Neg']= dfgourmet.Tokens.map(lambda x: ((sum(list(zip(* x)[0]))/countnonzero((list(zip(* x)[0]))) 
                                          if (countnonzero(list(zip(* x)[0]))!=0)
                                          else 0.001) if x!=[] else 0.001))
    dfgourmet['Pos'] = dfgourmet.Tokens.map(lambda x: ((sum(list(zip(* x)[1]))/countnonzero((list(zip(* x)[1]))) 
                                           if countnonzero(list(zip(* x)[1]))!=0
                                          else 0.001) if x!=[] else 0.001))
    
    header = ["PhraseID", "Neg", "Pos"]
    print "going to write"
    dfcell.to_csv(writepath, mode = 'a', sep = '\t', cols = header, header = False, index = False)
    print "written to writepath"
    dfkitchen.to_csv(writepath, mode = 'a', sep = '\t', cols = header, header = False, index = False)
    dfgourmet.to_csv(writepath, mode = 'a', sep = '\t', cols = header, header = False, index = False)

for i in range(1,11):
    readpath = path+str(i)+"/all_unigram_lexicon_negation.csv"
    writepathneg = "~/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold"+str(i)+"/unigram_neg_negation.csv"
    writepathpos = "~/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold" + str(i) + "/unigram_pos_negation.csv"
    if os.path.isfile(writepathneg):
        os.remove(writepathneg)
    if os.path.isfile(writepathpos):
        os.remove(writepathpos)
    df = pd.read_csv(readpath, sep = '\t', names = ['PhraseID','NegPolarity','PosPolarity'])
    headerneg = ["PhraseID","NegPolarity"]
    df.to_csv(writepathneg, sep = '\t',cols = headerneg, index = False, header = False)
    headerpos = ["PhraseID", "PosPolarity"]
    df.to_csv(writepathpos , sep = '\t', cols = headerpos, index = False, header = False)
