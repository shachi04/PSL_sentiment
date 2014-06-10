import nltk
from nltk.corpus import stopwords
from collections import Counter

readpath = "~/Documents/Shachi/CMPS209C/reviews/all_other_reviews/gourmet_negative_reviews.csv"

cnt_gourmetneg = Counter()
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0, len(lines)):
    for word in nltk.word_tokenize(lines[i].lower().replace(".","")):
        if(word not in stopwords.words('english')):
            cnt_gourmetneg[word] += 1


readpath = "~/Documents/Shachi/CMPS209C/reviews/all_other_reviews/gourmet_positive_reviews.csv"

cnt_gourmetpos = Counter()
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0, len(lines)):
    for word in nltk.word_tokenize(lines[i].lower().replace(".","")):
        if (word not in stopwords.words('english')):
            cnt_gourmetpos[word] += 1


cnt_bothgourmet = Counter()
cnt_bothgourmet = cnt_gourmetneg+cnt_gourmetpos
print len(cnt_bothgourmet)
print len(cnt_gourmetneg)
print len(cnt_gourmetpos)

len(set(cnt_gourmetneg) & set(cnt_gourmetpos))

neggourmetdict = {}
for word_freq in cnt_gourmetneg.items():
    totalfreq = cnt_bothgourmet.get(word_freq[0])
    negfreq = word_freq[1]
    ratio = negfreq*1.0/totalfreq
#    print word_freq[0],negfreq,totalfreq , ratio
    neggourmetdict[word_freq[0]] = ratio

posgourmetdict = {}
for word_freq in cnt_gourmetpos.items():
    totalfreq = cnt_bothgourmet.get(word_freq[0])
    posfreq = word_freq[1]
    ratio = posfreq*1.0/totalfreq
#    print word_freq[0],negfreq,totalfreq , ratio
    posgourmetdict[word_freq[0]] = ratio



readpath = "~/Documents/Shachi/CMPS209C/reviews/all_other_reviews/kitchen_positive_reviews.csv"

cnt_kitchenpos = Counter()
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0, len(lines)):
    for word in nltk.word_tokenize(lines[i].lower().replace(".","")):
        if (word not in stopwords.words('english')):
            cnt_kitchenpos[word] += 1
            
        
readpath = "~/Documents/Shachi/CMPS209C/reviews/all_other_reviews/kitchen_negative_reviews.csv"

cnt_kitchenneg = Counter()
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0, len(lines)):
    for word in nltk.word_tokenize(lines[i].lower().replace(".","")):
        if(word not in stopwords.words('english')):
            cnt_kitchenneg[word] += 1



cnt_bothkitchen = Counter()
cnt_bothkitchen = cnt_kitchenneg+cnt_kitchenpos
print len(cnt_bothkitchen)
print len(cnt_kitchenneg)
print len(cnt_kitchenpos)

len(set(cnt_kitchenpos) & set(cnt_kitchenneg))

negkitchendict = {}
for word_freq in cnt_kitchenneg.items():
    totalfreq = cnt_bothkitchen.get(word_freq[0])
    negfreq = word_freq[1]
    ratio = negfreq*1.0/totalfreq
    negkitchendict[word_freq[0]] = ratio
    
    
poskitchendict = {}
for word_freq in cnt_kitchenpos.items():
    totalfreq = cnt_bothkitchen.get(word_freq[0])
    posfreq = word_freq[1]
    ratio = posfreq*1.0/totalfreq
    poskitchendict[word_freq[0]] = ratio


readpath = "~/Documents/Shachi/CMPS209C/reviews/all_other_reviews/cellphone_positive_reviews.csv"

cnt_cellpos = Counter()
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0, len(lines)):
    for word in nltk.word_tokenize(lines[i].lower().replace(".","")):
        if (word not in stopwords.words('english')):
            cnt_cellpos[word] += 1
            
        
readpath = "~/Documents/Shachi/CMPS209C/reviews/all_other_reviews/cellphone_negative_reviews.csv"

cnt_cellneg = Counter()
f = open(readpath, "rb")
lines = f.readlines()
for i in range(0, len(lines)):
    for word in nltk.word_tokenize(lines[i].lower().replace(".","")):
        if(word not in stopwords.words('english')):
            cnt_cellneg[word] += 1


cnt_bothcell = Counter()
cnt_bothcell = cnt_cellneg + cnt_cellpos
print len(cnt_bothcell)
print len(cnt_cellneg)
print len(cnt_cellpos)

len(set(cnt_cellpos) & set(cnt_cellneg))

negcelldict = {}
for word_freq in cnt_cellneg.items():
    totalfreq = cnt_bothcell.get(word_freq[0])
    negfreq = word_freq[1]
    ratio = negfreq*1.0/totalfreq
    negcelldict[word_freq[0]] = ratio
    
    
poscelldict = {}
for word_freq in cnt_cellpos.items():
    totalfreq = cnt_bothcell.get(word_freq[0])
    posfreq = word_freq[1]
    ratio = posfreq*1.0/totalfreq
    poscelldict[word_freq[0]] = ratio

