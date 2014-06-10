import os
import csv
import shutil

path = r'~/Documents/Shachi/CMPS209C/reviews/GoldStd' 
#writepath = r'~/Documents/Shachi/CMPS209C/reviews/phrases_all/kitchen_neg'
data = {}
cell_neg_list = []
cell_pos_list = []
kitchen_neg_list = []
kitchen_pos_list = []
gourmet_neg_list = []
gourmet_pos_list = []
for dir_entry in os.listdir(path):
    count = 0
#    fulltext = ""
    if(dir_entry.startswith(".")==0):
        dir_entry_path = os.path.join(path, dir_entry)
        if os.path.isfile(dir_entry_path):
            if(dir_entry.find("cellphone_negative")!= -1):
                cell_neg_list.append(dir_entry_path)
            if(dir_entry.find("cellphone_positive")!= -1):
                cell_pos_list.append(dir_entry_path)
            if(dir_entry.find("kitchen_negative")!= -1):
                kitchen_neg_list.append(dir_entry_path)
            if(dir_entry.find("kitchen_positive")!= -1):
                kitchen_pos_list.append(dir_entry_path)
            if(dir_entry.find("gourmetfood_negative")!= -1):
                gourmet_neg_list.append(dir_entry_path)
            if(dir_entry.find("gourmetfood_positive")!= -1):
                gourmet_pos_list.append(dir_entry_path)

#shuffle all the reviews, to be picked in random
random.shuffle(cell_neg_list)
random.shuffle(cell_pos_list)
random.shuffle(gourmet_neg_list)
random.shuffle(gourmet_pos_list)
random.shuffle(kitchen_neg_list)
random.shuffle(kitchen_pos_list)



for j in range(1,11):
    writepath = "~/Documents/Shachi/CMPS209C/reviews/PSL_data_folds/fold"+str(j)+"/all.csv"
    ofile = open(writepath, "wb")   
    c = csv.writer(ofile, delimiter='\t',quotechar=' ')
    for i in range((2*j-2),2*j): 
        f = open(cell_neg_list[i])       
        lines = f.read().splitlines()
        for k in range(0,len(lines)):
            data = []
            data.append(lines[k])
            c.writerow(data)
        f.close()
        f1 = open(cell_pos_list[i])
        lines = f1.read().splitlines()
        for k in range(0,len(lines)):
            data = []
            data.append(lines[k])
            c.writerow(data)
        f1.close()
        f1 = open(kitchen_pos_list[i])
        lines = f1.read().splitlines()
        for k in range(0,len(lines)):
            data = []
            data.append(lines[k])
            c.writerow(data)
        f1.close()
        f1 = open(kitchen_neg_list[i])
        lines = f1.read().splitlines()
        for k in range(0,len(lines)):
            data = []
            data.append(lines[k])
            c.writerow(data)
        f1.close()
        f1 = open(gourmet_pos_list[i])
        lines = f1.read().splitlines()
        for k in range(0,len(lines)):
            data = []
            data.append(lines[k])
            c.writerow(data)
        f1.close()
        f1 = open(gourmet_neg_list[i])
        lines = f1.read().splitlines()
        for k in range(0,len(lines)):
            data = []
            data.append(lines[k])
            c.writerow(data)
        f1.close()
    ofile.close()
        #shutil.copy(writepath,destination)
