import java.io.BufferedWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.csvreader.CsvWriter;
/*
 * This class reads the line separated 
 * <review number> <segment id> <discourse segment>
 */

import edu.stanford.nlp.process.PTBTokenizer;
public class DiscourseSegmentTokenizer {
	static String dirNamePhrases = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/phrases_all/cell_neg";
	static String wordPolarityFile = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/cellphone_negative_word_polarity.csv";
	static String defaultPolarity = "negative";
	static String writeFolderPrefix ="/Users/girishsk/Documents/Shachi/CMPS209C/reviews/GoldStd/Goldstd3/";
	
	public static void main(String args[])
	{
		
		File[] files = new File(dirNamePhrases).listFiles();
		DiscourseSegmentTokenizer d = new DiscourseSegmentTokenizer();
		for (File file : files) {
		    if (file.isFile() && (!file.isHidden())) {

		//        System.out.println(file.toString());
		        d.goldStdPolarity(file.toString());
		    }
		}
		  
	}
	
	void goldStdPolarity(String filepath)
	{
		
		
		Map<String, String> word_polarity = new HashMap<String, String>();
		
		String line;
		String[] b;
		String[] splitcol;
		String tokenBuf;
		int i =1;
		
		List<String> phrases = new ArrayList<String>();
		List<String> wordlist = new ArrayList<String>();
		List<String> polarity = new ArrayList<String>();
		List<String> contrast = new ArrayList<String>();
		//List<String> filelist = new ArrayList<String>();
		
		try{
			
			
		String reviewid = null ;
		//String filename = file.toString();
		System.out.println(filepath);
		String path = filepath.split("/")[9].toString().split(".csv")[0];
		//String path = path1.split(".");
		System.out.println(path);
		//String path = "contrast_cellphone_negative_review20";
		
//		File file = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/phrases_all/cell_pos/"+path+".csv");
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		while(((line= reader.readLine()))!=null)
		{
			b = line.split("\t");
			phrases.add(b[2]); 
			reviewid = b[0];
			contrast.add(b[3]);
		}
		
        System.out.println("no.of phrases = "+phrases.size());
        ArrayList<String> replaced_phrases = new ArrayList<String>();
	    	for(i = 0;i<phrases.size();++i)
	    	{
	    		tokenBuf = phrases.get(i);
	        	tokenBuf = tokenBuf.replace("[","").replace("]","");
	        	tokenBuf = tokenBuf.replace("You're", "You ` re");
	        	tokenBuf = tokenBuf.replace("you're", "you ` re");
	        	tokenBuf = tokenBuf.replace("You've", "You ` ve");
	        	tokenBuf = tokenBuf.replace("you've", "you ` ve");
	        	tokenBuf = tokenBuf.replace("i've", "i ` ve");
	        	tokenBuf = tokenBuf.replace("I'll", "I ` ll");
	        	tokenBuf = tokenBuf.replace("i'll", "i ` ll");
	        	tokenBuf = tokenBuf.replace("You'll", "You ` ll");
	        	tokenBuf = tokenBuf.replace("you'll", "you ` ll");
	        	tokenBuf = tokenBuf.replace("I've", "I ` ve");
	        	tokenBuf = tokenBuf.replace("i've", "i ` ve");
	        	tokenBuf = tokenBuf.replace("I'll", "I ` ll");
	        	tokenBuf = tokenBuf.replace("i'll", "i ` ll");
	        	tokenBuf = tokenBuf.replace("we've", "we ` ve");
	        	tokenBuf = tokenBuf.replace("We've", "We ` ve");
	        	tokenBuf = tokenBuf.replace("we're", "we ` re");
	        	tokenBuf = tokenBuf.replace("We're", "We ` re");
	        	tokenBuf = tokenBuf.replace("we'll", "we ` ll");
	        	tokenBuf = tokenBuf.replace("We'll", "We ` ll");
	        	tokenBuf = tokenBuf.replace("they're", "they ` re");
	        	tokenBuf = tokenBuf.replace("They're", "They ` re");
	            tokenBuf = tokenBuf.replace("'ve", "` ve");
	            tokenBuf = tokenBuf.replace("'re", "` re");
	            tokenBuf = tokenBuf.replace("'ll", "` ll");
	            tokenBuf = tokenBuf.replace("``", "");
	            
	        	//tokenize markable with stanford tokenizer ("don't" ->  "do" "n't")
	        	PTBTokenizer tokenizer = PTBTokenizer.newPTBTokenizer(new StringReader(tokenBuf)); 
	        	String concatStr = new String();
	    		while (tokenizer.hasNext()){
	    			
	    			String buf = tokenizer.next().toString();
	    			// very ugly hack because tools handle "..." in different ways :(
	    			if (buf.equals("...") && (!buf.equals("...."))){
	    				
	    				concatStr = concatStr+" . . .";
		    			
		    			
	    			}
	    			
	    			else {
	    				concatStr = concatStr + " "+buf;
	    				
	    			}
	    			
	    		} //end while
//	    	    System.out.println(concatStr.substring(1));
	    	    String st = concatStr.replaceAll("''", "\"").replaceAll("``","\"").replaceAll(" '' ", " \" ").replaceAll(" \" \" ", " \" ").replaceAll(" \" \"", " \"").substring(1);
	    	    if(st.startsWith("\" "))
	    	    	st = st.replaceFirst("\" ", "");
	    	    
	    	    replaced_phrases.add(st);
	    		//replaced_phrases.add(concatStr.replaceAll(" '' ''", " ''").replaceAll("`` ","\" ").replaceAll("'' ","\"").replaceAll(" ''","\"").substring(1));
	    	}
	    	

//	    	for (i = 0;i<replaced_phrases.size();++i)
//	    	{
//	    		System.out.println(replaced_phrases.get(i));
//	    	}
	    	
	System.out.println("i = "+i);
	int startpos=0;
	int j=0;
	int notfound=0;
	//Reading the wordlist file :
	
		File file1 = new File(wordPolarityFile);
		BufferedReader read = new BufferedReader(new FileReader(file1));
		while(((line= read.readLine()))!=null)
		{
			splitcol = line.split("\t");
	//		System.out.println(splitcol[0]);
			wordlist.add(splitcol[1]);
			polarity.add(splitcol[2]);
		}
		System.out.println("size of replaced phrases"+replaced_phrases.size());
		
		for(i=0;i<replaced_phrases.size();++i)
		{
		List<String> sublist= Arrays.asList(replaced_phrases.get(i).split(" "));
		startpos = Collections.indexOfSubList(wordlist, sublist);
		String[] phrase_polarity;
		String[] newpath = path.split("_");
		//.split(".")[0]
		String writepath = newpath[1]+"_"+newpath[2]+"_"+newpath[3].split("w")[1]+"_contrast_prev.csv";
		File file2 = new File(writeFolderPrefix+writepath);
	     
	    CsvWriter csvOutput = new CsvWriter(new FileWriter(file2, true), '\t');
	    int countneg=0; int countpos=0;
		if(startpos!=-1)
		{
		for(j=startpos;j<startpos+sublist.size();++j)
		{
			word_polarity.put(wordlist.get(j), polarity.get(j));
			if((polarity.get(j).equals("negative")) || (polarity.get(j).equals("negative_other_product")))
			{
				countneg++;
			}
			else if((polarity.get(j).equals("positive")) || (polarity.get(j).equals("positive_other_product")))
			{
				countpos++;
			}		
		}
//		System.out.println("i= "+i);
//		System.out.println("Review id = "+reviewid);
//		System.out.println("countneg= "+countneg+ " countpos = "+countpos);
		if(countneg==countpos)
		{
//			System.out.println("\n countneg=countpos, hence review is marked as negative");
			phrase_polarity = new String[] {reviewid+"_"+(i+1),defaultPolarity,contrast.get(i),reviewid+"_"+(i), replaced_phrases.get(i)};
			csvOutput.writeRecord(phrase_polarity);
			
		}
		else if(countneg>countpos)
		{			
//			System.out.println("\n giving a negative polarity for the phrase "+i);
			
			phrase_polarity = new String[] {reviewid+"_"+(i+1),"negative",contrast.get(i),reviewid+"_"+(i), replaced_phrases.get(i)};
			csvOutput.writeRecord(phrase_polarity);
		}
		else if(countpos>countneg)
		{
//			System.out.println("\n giving a positive polarity for the phrase " +i);
			
			phrase_polarity = new String[] {reviewid+"_"+(i+1),"positive",contrast.get(i),reviewid+"_"+(i), replaced_phrases.get(i)};
			csvOutput.writeRecord(phrase_polarity);
		}
		
		}
		else
		{
			System.out.println("Did not find this phrase "+i + "\n");
			notfound++;
			System.out.println(replaced_phrases.get(i));
			phrase_polarity = new String[] {reviewid+"_"+(i+1),defaultPolarity,contrast.get(i),reviewid+"_"+(i), replaced_phrases.get(i)};
			csvOutput.writeRecord(phrase_polarity);
		}
//		System.out.println("written to file!! ");
//		System.out.println("printing review id : " + reviewid);
//		System.out.println("printing i "+i);
//		System.out.println(i);
		
	csvOutput.close();	
	
	}System.out.println("Notfound phrases "+notfound);
	
	}
	catch(Exception ie)
	{
		ie.printStackTrace();
	}
	
	}
	
}
