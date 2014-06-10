import pandas as pd

genpath = "~/Documents/Shachi/CMPS209C/reviews/Results/unigram_otherpos_negation/fold10/"
threshold=0.4
neg = genpath+"negsentiment.csv"
pos = genpath+"possentiment.csv"
tneg = genpath+"trueneg_other.csv"
tpos = genpath+"truepos_other.csv"
writeneg = genpath+"thresholdneg.csv"
writepos = genpath+"thresholdpos.csv"
dfneg = pd.read_csv(neg, sep='\t', names = ["id","score"])
dfpos = pd.read_csv(pos, sep='\t', names = ["id","score"])
dfneg = dfneg[dfneg["score"]>=threshold]
dfpos = dfpos[dfpos["score"]>=threshold]
dfneg.to_csv(writeneg, sep = '\t', header = False, index = False)
dfpos.to_csv(writepos, sep = '\t', header = False, index = False)
f1 = open(writeneg,"rb")
linesn = f1.readlines()
f2 = open(writepos, "rb")
linesp = f2.readlines()
trueneg = pd.read_csv(tneg, sep = '\t', names = ["id"])
truepos = pd.read_csv(tpos, sep = '\t', names = ["id"])
print len(set(trueneg["id"].tolist())& set(dfneg["id"]))
print len(set(truepos["id"].tolist())& set(dfpos["id"]))
print(len(set(truepos["id"].tolist())) + len(set(trueneg["id"].tolist())))
print (len ( set(dfpos["id"]) & set(dfneg["id"])))
tn_maj = len(set(trueneg["id"].tolist())& set(dfneg["id"]))
tp_maj = len(set(truepos["id"].tolist())& set(dfpos["id"]))
total = len(set(truepos["id"].tolist())) + len(set(trueneg["id"].tolist()))
sub_maj = len ( set(dfpos["id"]) & set(dfneg["id"]))
negline = []
posline = []
d_neg = {}
d_pos = {}
for i in linesn:
    t = i.strip().split("\t")
    d_neg[t[0]]=t[1]
for i in linesp:
    t = i.strip().split("\t")
    d_pos[t[0]]=t[1]

    
    
for k in  set(dfpos["id"]) & set(dfneg["id"]):
    if float(d_neg[k]) > float(d_pos[k]) :
        negline.append(k)
    else:
        posline.append(k)

print len(set(negline)&set(trueneg["id"]))
print len(set(posline)&set(truepos["id"]))
add_neg = len(set(negline)&set(trueneg["id"]))
add_pos = len(set(posline)&set(truepos["id"]))
print (add_neg+add_pos + tp_maj + tn_maj - sub_maj ) * 1.0 /total 