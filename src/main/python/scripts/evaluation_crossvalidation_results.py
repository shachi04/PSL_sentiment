import pandas as pd

readpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/Results/results.csv"
result = pd.read_csv(readpath, sep = '\t')


thres4 = result[result['Th']==0.4]
thres45 = result[result['Th']==0.45]
thres5 = result[result['Th']==0.5]
thres3 = result[result['Th']==0.3]

thres4pos = thres4[thres4['Pol']=="pos"]



thres4pos[0:10]
print "Accuracy mean : " + str(thres4pos.Accuracy.mean())
print "F1 mean :" + str(thres4pos.F1.mean())
print "Precision mean :" + str(thres4pos.Precision.mean())
print "Recall mean :" + str(thres4pos.Recall.mean())

thres45pos = thres45[thres45['Pol']=="pos"]
thres45pos[0:10]
print "Accuracy mean : " + str(thres45pos.Accuracy.mean())
print "F1 mean :" + str(thres45pos.F1.mean())
print "Precision mean :" + str(thres45pos.Precision.mean())
print "Recall mean :" + str(thres45pos.Recall.mean())

thres5pos = thres5[thres5['Pol']=="pos"]
thres5pos[0:10]
print "Accuracy mean : " + str(thres5pos.Accuracy.mean())
print "F1 mean :" + str(thres5pos.F1.mean())
print "Precision mean :" + str(thres5pos.Precision.mean())
print "Recall mean :" + str(thres5pos.Recall.mean())

thres45neg = thres45[thres45['Pol']=="neg"]
thres45neg[0:10]
print "Accuracy mean : " + str(thres45neg.Accuracy.mean())
print "F1 mean :" + str(thres45neg.F1.mean())
print "Precision mean :" + str(thres45neg.Precision.mean())
print "Recall mean :" + str(thres45neg.Recall.mean())

thres4neg = thres4[thres4['Pol']=="neg"]
thres4neg[0:10]
print "Accuracy mean : " + str(thres4neg.Accuracy.mean())
print "F1 mean :" + str(thres4neg.F1.mean())
print "Precision mean :" + str(thres4neg.Precision.mean())
print "Recall mean :" + str(thres4neg.Recall.mean())

thres5neg = thres5[thres5['Pol']=="neg"]
thres5neg[0:10]
print "Accuracy mean : " + str(thres5neg.Accuracy.mean())
print "F1 mean :" + str(thres5neg.F1.mean())
print "Precision mean :" + str(thres5neg.Precision.mean())
print "Recall mean :" + str(thres5neg.Recall.mean())

thres3neg = thres3[thres3['Pol']=="neg"]
thres3neg[0:10]
print "Accuracy mean : " + str(thres3neg.Accuracy.mean())
print "F1 mean :" + str(thres3neg.F1.mean())
print "Precision mean :" + str(thres3neg.Precision.mean())
print "Recall mean :" + str(thres3neg.Recall.mean())

thres3pos = thres3[thres3['Pol']=="pos"]
thres3pos[0:10]
print "Accuracy mean : " + str(thres3pos.Accuracy.mean())
print "F1 mean :" + str(thres3pos.F1.mean())
print "Precision mean :" + str(thres3pos.Precision.mean())
print "Recall mean :" + str(thres3pos.Recall.mean())