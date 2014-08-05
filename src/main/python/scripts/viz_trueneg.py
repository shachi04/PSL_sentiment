import pandas as pd

i = 5
genpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/Results/negation_tweak/fold"
tneg = genpath+str(i)+"/trueneg_other.csv"
tpos = genpath+str(i)+"/truepos_other.csv"

df1 = pd.read_csv(tneg, sep = "\t", names = ["ID"])
df1["pol"] = "neg"
#header = ["ID","score","pol"]
#df1.to_csv(newwriteneg, sep = "\t", index = False, cols = header, header = False)

df2 = pd.read_csv(tpos, sep = "\t", names = ["ID"])
df2["pol"] = "pos"
#header = ["ID","score","pol"]
#df2.to_csv(newwritepos, sep = "\t", index = False, cols = header, header = False)
bigdata=df1.append(df2,ignore_index = True)
sortedpath = genpath+str(i)+"/alltrueposneg.csv"
header = ["ID","pol"]
bigdata.to_csv(sortedpath, sep = "\t", index = False, cols = header, header = False)
alldict = {}
f = open(sortedpath, "rb")
lines = f.readlines() 
for line in lines : 
    x = line.split("\t")
    if(not alldict.has_key(x[0])):
        alldict[x[0]] = (x[1].strip())
    else : 
        y = alldict[x[0]]
        if (y[0]<x[1]):
            print "entered here"
            alldict[x[0]] = x[1]
filetoread = "/Users/girishsk/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold5/all_text.csv"
f = open(filetoread, "rb")
lines = f.readlines()
textdict = {}
for line in lines:
    s = line.split("\t")
    if(not textdict.has_key(s[0])):
        textdict[s[0]] = s[1].strip()
for key, value in sorted(alldict.items()):
#    print key
    if(textdict.has_key(key)):
#        print key
        if(value == "pos"):
#            print "\033[01;41m" +value[1]
            print  "\033[01;42m" + textdict[key]
        elif(value == "neg"):
#            print "\033[01;46m"  + value[1]
            print "\033[01;41m"  + textdict[key]
        