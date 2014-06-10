//package org.zirn.sentimentAnalyzer.modules.mmax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import com.csvreader.CsvWriter;
//import org.apache.log4j.Logger;
import java.util.logging.*;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.discourse.MMAX2Discourse;

import edu.stanford.nlp.process.PTBTokenizer;

/**
 * Reads in mmax file and reads annotations from it, creates goldStandards for all reviews contained in this file
 * (normally: one file positive reviews, one file containing negative reviews)
 * 
 * public GoldStandardOneReview[] gs: contains GoldStandards of all Reviews as Array
 * 
 * @author czirn
 *
 */
public class GoldStandardSeparated {
	
	//static final  Logger logger = Logger.getLogger(GoldStandardSeparated.class); //log4j
	private static final Logger logger = Logger.getLogger( GoldStandardSeparated.class.getName() );
	
	//MMAX
	String mmaxPath;
	String projectName;

	//via this variable the goldStandard information is accessible
	//public GoldStandardOneReview[] gs; //each element contains gold standard (tokens + polarities) for 1 review
	
	
	
	public GoldStandardSeparated (String mmaxPathIn, String rawProjectName){
		
		
		MMAX2Discourse discourse;
		MarkableLevel polarityLevelGold;
		MarkableLevel wordLevel; 
		MarkableLevel reviewLevel;
		
		
		this.mmaxPath = mmaxPathIn;
		//this.projectName = "baby_negative.mmax";
		this.projectName = rawProjectName + ".mmax";
		
		//access mmax annotated files
		discourse = MMAX2Discourse.buildDiscourse(mmaxPath+ projectName);
		polarityLevelGold = discourse.getMarkableLevelByName("polarityGold", false);
		reviewLevel = discourse.getMarkableLevelByName("review", false);
		wordLevel = discourse.getMarkableLevelByName("word", false);  
		
	    //get all reviews and iterator for them
		ArrayList reviewMarkables = reviewLevel.getMarkables();
	    Iterator<Markable> reviewsIt = reviewMarkables.iterator();
	   
	    String tokenBuf = "";
	    String polarityBuf = "";
		
		
		String[] tokensArrayBuf; // tokensVectorBuf -> tokensVectorBufArray
		String[] polaritiesArrayBuf; // polarityVectorBuf -> polarityVectorBufArray
		//collects gold standards of all reviews, will be converted to GoldStandardOneReview[] gs
	//	Vector<GoldStandardOneReview> gsBuf = new Vector<GoldStandardOneReview>(); 

		/*
		 * ITERATE OVER REVIEWS (= review markables), COLLECT POLARITY ANNOTATIONS
		 */
		ArrayList<String> data = new ArrayList<String>();
	    String[] x = null ;
		try
		{
			 File file = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/kitchen_negative_word_polarity.csv");
	//	     BufferedWriter output = new BufferedWriter(new FileWriter(file));
		     
		     CsvWriter csvOutput = new CsvWriter(new FileWriter(file, true), '\t');
				
				// if the file didn't already exist then we need to write out the header line
				
				// else assume that the file already has the correct header line
			x = new String[] {"Count","Word","Polarity"};
				
		     csvOutput.writeRecord(x);
	    while (reviewsIt.hasNext()){
	    	Markable m = reviewsIt.next();
	    	String[] disel = m.getDiscourseElements();
	    	//output.write("\n \nprinting discourse elements for reveiew"+m.getID()+"\n");
	    	//for(int i=0;i<disel.length;++i)
	    	//output.write(disel[i]+"\n");
	    	String[] markableIdsInReview = m.getDiscourseElementIDs();
	    //	output.write("\n \nprinting discourse elements for reveiew"+m.getID()+"\n");
	    //	output.write(markableIdsInReview[0] + "\t " +markableIdsInReview[m.getSize()-1]);
	    	Vector<String> tokensVectorBuf = new Vector<String>();
	    	Vector<String> polaritiesVectorBuf = new Vector<String>();
	    	//iterate over element-IDs within review
	    	for (int j = 0; j < markableIdsInReview.length; j ++){
	    		//get polarities for element
	    		Markable[] markPolarGold = polarityLevelGold.getAllMarkablesAtDiscourseElement(markableIdsInReview[j], true);
	    		//iterate over markables of one element
	    		//i < 1 because there are 2 markables at this position,one as word markable and one as polarity markable (but markable is the same) 
	    		//output.write(markableIdsInReview[j]+"\n");
	    		if (markPolarGold.length > 0){
			    	for (int i = 0; i < 1; i++){
			    	
		    		//	output.write("\t\tMARKABLE STRING: " + markPolarGold[i].toString()+"\n");
		    			
		    			
			        	//read markable and remove brackets
			        	tokenBuf = markPolarGold[i].toString().replace("[","").replace("]",""); 
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
	   			
			        	//get value (polarity)  for markable
			         	polarityBuf = markPolarGold[i].getAttributeValue("polarity"); 
			     //   	output.write(m.getID()+"\t"+tokenBuf+"\t"+polarityBuf+"\n");
			        	//tokenize markable with stanford tokenizer ("don't" ->  "do" "n't")
			        	PTBTokenizer tokenizer = PTBTokenizer.newPTBTokenizer(new StringReader(tokenBuf)); 
			    		while (tokenizer.hasNext()){
			    			
			    			String buf = tokenizer.next().toString();
			    			// very ugly hack because tools handle "..." in different ways :(
			    			if (buf.equals("...")){
				    			tokensVectorBuf.add("."); 
				    			polaritiesVectorBuf.add(polarityBuf);
				    			tokensVectorBuf.add("."); 
				    			polaritiesVectorBuf.add(polarityBuf);
				    			tokensVectorBuf.add("."); 
				    			polaritiesVectorBuf.add(polarityBuf);
			    			}
			    			if (buf.equals(" ``"))
			    			{
			    				tokensVectorBuf.add(buf.replace(" ``", " \""));
			    				polaritiesVectorBuf.add(polarityBuf);
			    			}
			    			else {
			    				tokensVectorBuf.add(buf); //save token in array(tokenizer.next());
			    				polaritiesVectorBuf.add(polarityBuf);
			    			}
			    			
			    			x = new String[] {m.getID(), buf, polarityBuf};
			    			//System.out.println(x);
			    			
			    //			csvOutput.writeRecord(x);
			    		//	output.write(m.getID()+"\t"+buf+"\t"+polarityBuf+"\n");
			    		//	output.write("tokensvectorbuf"+j +":"+tokensVectorBuf+"\n");
			    			//output.write("polaritiesVectorBuf:"+polaritiesVectorBuf.toString()+"\n");
			    			
			    		} //end while
			    		
			    	}
			    	
	    		}
	    		
	    	}  	//System.out.println(tokensVectorBuf);
	    	for(int i =0;i<tokensVectorBuf.size();++i)
	    	{
	    		
	    		x = new String[] {m.getID(), tokensVectorBuf.get(i), polaritiesVectorBuf.get(i)};
	    		csvOutput.writeRecord(x);
	    	}
	    	
	    	//System.out.println(tokensVectorBuf.size()+ "\t"+ polaritiesVectorBuf.size());
	    	//output.close();
	    	//tokens and polarities that were collected for one review are added to accessible goldStandard
	    	// convert collected vectors to String[]:
	    	tokensArrayBuf = tokensVectorBuf.toArray(new String[tokensVectorBuf.size()]);
	    	polaritiesArrayBuf = polaritiesVectorBuf.toArray(new String[polaritiesVectorBuf.size()]);
	    	//System.out.println(tokensArrayBuf.length);
	    	//System.out.println(polaritiesArrayBuf.length);
	    	/*for(int i=0;i<tokensArrayBuf.length;++i)
	    	{
	    	output.write(tokensArrayBuf[i] +"\t"+ polaritiesArrayBuf[i]+"\n");
	    	}*/
	    //	GoldStandardOneReview gsorBuf = new GoldStandardOneReview(tokensArrayBuf,polaritiesArrayBuf);
	    //	gsBuf.add(gsorBuf);  
	     //end iterate over reviews
		}
	    csvOutput.close();
		}
		catch(Exception ie)
		{
			ie.printStackTrace();
		}
	   
	    
	  
	} //end  GoldStandardSeparated 
	
	



	public static void main(String[] args){
		
		System.out.println("GoldStandard");
		String path ="/Users/girishsk/Documents/Shachi/CMPS209C/Datasets/Sentiment_GOLDSTANDARD";
		String goldStandardPath = "/Users/girishsk/Documents/Shachi/CMPS209C/Datasets/Sentiment_GOLDSTANDARD/";
		String goldProjectNamePositive = "kitchen_negative";
		
		GoldStandardSeparated gs = new GoldStandardSeparated(goldStandardPath,goldProjectNamePositive);
	}
}
