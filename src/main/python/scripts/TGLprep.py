import pandas as pd
adjpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/SODictionariesV1.11Eng/adj_dictionary1.11.txt"
writepath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/SODictionariesV1.11Eng/all_TGL.csv"
advpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/SODictionariesV1.11Eng/adv_dictionary1.11.txt"
nounpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/SODictionariesV1.11Eng/noun_dictionary1.11.txt"
verbpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/SODictionariesV1.11Eng/verb_dictionary1.11.txt"
intpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/SODictionariesV1.11Eng/int_dictionary1.11.txt"

def convert_posscore(x):
    if (x==5):
        return 1
    elif(x==4):
        return 0.8
    elif(x==3):
        return 0.6
    elif(x==2): 
        return 0.4
    elif(x==1): 
        return 0.2
    else : return 0.001
def convert_negscore(x):
    if (x==-5):
        return 1
    elif(x==-4):
        return 0.8
    elif(x==-3):
        return 0.6
    elif(x==-2): 
        return 0.4
    elif(x==-1): 
        return 0.2 
    else: return 0.001

adjdf = pd.read_csv(adjpath, sep = '\t', names = ["words", "score"])
adjdf["wordadj"] = adjdf.words.map (lambda x: (str(x).replace("_"," ") ,"JJ"))

adjdf["pos"]= adjdf.score.map (lambda x : convert_posscore(x))
adjdf["neg"] = adjdf.score.map(lambda x : convert_negscore(x))  
header = ["wordadj","pos", "neg"]
adjdf.to_csv(writepath, sep = "\t", cols = header, header = False, index = False)

advdf = pd.read_csv(advpath, sep = '\t', names = ["words", "score"])
advdf["wordadv"] = advdf.words.map (lambda x: (str(x).replace("_"," "),"RB"))
advdf["pos"]= advdf.score.map (lambda x : convert_posscore(x))
advdf["neg"] = advdf.score.map(lambda x : convert_negscore(x))  
header = ["wordadv","pos", "neg"]
advdf.to_csv(writepath, sep = "\t", mode = 'a',cols = header, header = False, index = False)

noundf = pd.read_csv(nounpath, sep = '\t', names = ["words", "score"])
noundf["wordnoun"] = noundf.words.map (lambda x: (str(x).replace("_"," "),"NN"))
noundf["pos"]= noundf.score.map (lambda x : convert_posscore(x))
noundf["neg"] = noundf.score.map(lambda x : convert_negscore(x))  
header = ["wordnoun","pos", "neg"]
noundf.to_csv(writepath, sep = "\t", mode = 'a',cols = header, header = False, index = False)

verbdf = pd.read_csv(verbpath, sep = '\t', names = ["words", "score"])
verbdf["wordverb"] = verbdf.words.map (lambda x: (str(x).replace("_"," "),"VB"))
verbdf["pos"]= verbdf.score.map (lambda x : convert_posscore(x))
verbdf["neg"] = verbdf.score.map(lambda x : convert_negscore(x))  
header = ["wordverb","pos", "neg"]
verbdf.to_csv(writepath, sep = "\t", mode = 'a',cols = header, header = False, index = False)

intdf = pd.read_csv(intpath, sep = '\t', names = ["words", "score"])
intdf["wordint"] = intdf.words.map (lambda x: (str(x).replace("_"," ")," "))
intdf["pos"]= intdf.score.map (lambda x : x if(x>0) else 0.001)
intdf["neg"] = intdf.score.map(lambda x : -(x) if(x<0) else 0.001)  
header = ["wordint","pos", "neg"]
intdf.to_csv(writepath, sep = "\t", mode = 'a',cols = header, header = False, index = False)