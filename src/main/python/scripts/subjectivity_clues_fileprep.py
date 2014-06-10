import pandas as pd


def pos_code(tag):
    if tag.startswith("noun"):
        return "NN"
    elif tag.startswith("verb"):
        return "VB"
    elif tag.startswith("adj"):
        return "JJ"
    elif tag.startswith("adverb"):
        return "RB"
    else:
        return ''


readpath = "~/Documents/Shachi/CMPS209C/reviews/subjectivity_clues_hltemnlp05/subjclueslen1-HLTEMNLP05.tff"
writepath = "~/Documents/Shachi/CMPS209C/reviews/subjectivity_clues.csv"

df = pd.read_table(readpath, sep = " ", names = ["type","len", "word", "pos", "stemmed", "polarity"])
df["strength"] = df.type.map(lambda x: 0.5 if((x.split("=")[1])=="weaksubj") 
                             else 1)
df["word"] = df.word.map(lambda x: (x.split("=")[1]))
df['pos'] = df.pos.map(lambda x: pos_code(x.split("=")[1]))
df['polarity'] = df.polarity.map(lambda x: x.split("=")[1] if (len(x.split("="))==2)
                else "neutral")


header = ["word","pos","polarity","strength"]
df.to_csv(writepath, sep = '\t', cols = header, index = False)