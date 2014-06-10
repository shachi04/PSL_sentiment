import pandas as pd

readpath = "~/Documents/Shachi/CMPS209C/reviews/Results/unigram_negation_contrast/auc.csv"
df = pd.read_csv(readpath, sep = '\t')
dfpos = df[df["sentiment"] == "possentiment"]
dfneg = df[df["sentiment"] == "negsentiment"]


def calcavg(df1):    
    df1posauprc = []
    df1posauprc = df1.AUPRC.tolist()
    df1negauprc = []
    df1negauprc = df1.NEGAUPRC.tolist()
    avgdf1posauprc = sum(df1posauprc)/len(df1posauprc)
    avgdf1negauprc = sum(df1negauprc)/len(df1negauprc)
    print avgdf1posauprc
    print avgdf1negauprc

    
calcavg(dfpos)
calcavg(dfneg)