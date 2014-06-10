import os
import shutil
import csv

src = "~/Documents/Shachi/CMPS209C/reviews/Goldstd/Goldstd3/"
destdir = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds2/"


#collect data folds :

def collect_data_folds():
	path = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds/"

	list_data = []
	for i in range(10) :
	    list_inside = []
	    fld_name = path+"fold"+str(i+1)
	    for dir_entry in os.listdir(fld_name):
	        if ( not dir_entry.startswith(".") and dir_entry != "all.csv") :
	            list_inside.append(dir_entry)
	    list_data.append(list_inside)

	return list_data


#Combine all csv's in each fold to one file - all.csv

def combine():
	for j in range(1,11):  
	    writedir = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds2/fold"+str(j)
	    writepath = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds2/fold"+str(j)+"/all.csv"
	    ofile = open(writepath, "wb")   
	    c = csv.writer(ofile, delimiter='\t',quotechar=' ')
	    for dir_entry in os.listdir(writedir):
	        if(dir_entry!="all.csv"):
	            f = open(writedir+"/"+dir_entry)       
	            lines = f.read().splitlines()
	            for k in range(0,len(lines)):
	                data = []
	                data.append(lines[k])
	                c.writerow(data)
	            f.close()
	            print dir_entry
	    ofile.close()

def extract_constant():
	path = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds/fold"
	path2 = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
	for i in range(1,11):
    readpath = path+str(i)+"/all.csv"
    df = pd.read_csv(readpath, 
                     header = None , 
                     sep = '\t',
                     names=['PhraseID', 'Polarity','Contrast','Previous','Text'])
    df['PhraseIDs'] =  df.PhraseID.map(lambda x : x.strip())
    
    writepath = path2+str(i)+"/all_contrast.csv"
    header = ["PhraseIDs","Contrast"]
    df.to_csv(writepath,
              sep='\t',
              cols = header,
              index=False)

def extract_polarity():
	path = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds2/fold"
	for i in range(1,11):
	    readpath = path+str(i)+"/all.csv"
	    df = pd.read_csv(readpath, 
	                     header = None , 
	                     sep = '\t',
	                     names=['PhraseID', 'Polarity','Contrast','Previous','Text'])
	    df['PhraseIDs'] =  df.PhraseID.map(lambda x : x.strip())
	    
	    writepath = path+str(i)+"/polarity.csv"

	    header = ["PhraseIDs","Polarity"]
	    df.to_csv(writepath,
	              sep='\t',
	              cols = header,
	              index=False)
	path1 = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds2/fold"
	path2 = "~/Documents/Shachi/CMPS209C/psl-example/data/sentiment/fold"
	for i in range(1,11):
	    readpath = path+str(i)+"/polarity.csv"
	    df = pd.read_csv(readpath,  
	                     sep = '\t', names = ['PhraseID', 'Polarity'])
	    
	    writepathneg = path2+str(i)+"/trueneg_other.csv"
	    writepathpos = path2+str(i)+"/truepos_other.csv"
	    header = ["PhraseID"]
	    df2 = df
	    df = df[df['Polarity']=="negative"]
	    df2 = df2[df2['Polarity']=="positive"]
	    df.to_csv(writepathneg,
	              sep='\t',
	              cols = header,
	              header = False,
	              index=False)
	    df2.to_csv(writepathpos,
	              sep='\t',
	              cols = header,
	              header = False,
	              index=False)

def extract_contrast():
	path2 = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
	for i in range(1,11):

    readpath = path2+str(i)+"/all_contrast.csv"
    df = pd.read_csv(readpath,  
                     sep = '\t')
    
    writepath = path2+str(i)+"/noncontrast.csv"
    writepath2 = path2+str(i)+"/contrast.csv"
    header = ["PhraseIDs"]
    df2 = df
    df = df[df['Contrast']==False]
    df2 = df2[df2['Contrast']==True]
    df.to_csv(writepath,
              sep='\t',
              cols = header,
              header = False,
              index=False)
    df2.to_csv(writepath2,
              sep='\t',
              cols = header,
              header = False,
              index=False)
        
def get_allIDs():
	path2 = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
	for i in range(1,11):

    readpath = path2+str(i)+"/all_contrast.csv"
    df = pd.read_csv(readpath,  
                     sep = '\t')
    
    writepath = path2+str(i)+"/allID.csv"
    
    header = ["PhraseIDs"]
    df.to_csv(writepath,
              sep='\t',
              cols = header,
              header = False,
              index=False)

def contrast_for_psl():
	path2 = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"
	for i in range(1,11):
    readpath = path2+str(i)+"/contrast.csv"
    writepath = path2+str(i)+"/contrast_ids.csv"
    data = []
    f = open(readpath, "rb")
    ofile = open(writepath, "wb")
    c = csv.writer(ofile, delimiter='\t')
    lines = f.readlines()
    for i in range(0,len(lines)):
        s = lines[i]
        n = int(s.split("_")[3])+1
        nextid = s.split("_")[0]+"_"+s.split("_")[1]+"_"+s.split("_")[2]+"_"+str(n)
        print nextid
        print s
        ofile.write(s.strip("\n")+"\t"+nextid+"\n")
    ofile.close()
    
def previous_for_psl():
	path = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds/fold"
	for i in range(1,11):
    readpath = path+str(i)+"/all.csv"
    df = pd.read_csv(readpath, 
                     header = None , 
                     sep = '\t',
                     names=['PhraseID', 'Polarity','Contrast','Previous','Text'])
    df['PhraseIDs'] =  df.PhraseID.map(lambda x : x.strip())
    
    writepath = path2+str(i)+"/all_prev.csv"
    header = ["PhraseIDs","Previous"]
    df.to_csv(writepath,
              sep='\t',
              cols = header,
              index=False)
 
def text_for_psl():
	path = "~/Documents/Shachi/CMPS209C/reviews/Goldstd_data_folds/fold"
	for i in range(1,11):
    readpath = path+str(i)+"/all.csv"
    df = pd.read_csv(readpath, 
                     header = None , 
                     sep = '\t',
                     names=['PhraseID', 'Polarity','Contrast','Previous','Text'])
    df['PhraseIDs'] =  df.PhraseID.map(lambda x : x.strip())
    
    writepath = path2+str(i)+"/all_text.csv"
    header = ["PhraseIDs","Text"]
    df.to_csv(writepath,
              sep='\t',
              cols = header,
              index=False)

#move data into cross validation buckets

list_data = collect_data_folds()
for dir_entry in os.listdir(src):
    if(not dir_entry.startswith(".")):
        for i in range(0,10):
            for j in range(0,12):
                if(dir_entry.endswith(list_data[i][j])):
                    shutil.copy(src+dir_entry,destdir+"fold"+str(i+1))

combine()
extract_constant()
extract_polarity()           
extract_contrast()
get_allIDs()
contrast_for_psl()
previous_for_psl()

	    