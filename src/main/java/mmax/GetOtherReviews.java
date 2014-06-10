
//package org.zirn.sentimentAnalyzer.modules.mmax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import com.csvreader.CsvWriter;
//import org.apache.log4j.Logger;
import java.util.logging.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.fsm.TransducerGraph.OutputCombiningProcessor;
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
public class GetOtherReviews implements Serializable {
	static String writepath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/all_other_reviews/cellphone_positive_reviews.csv";
	static String xmlpath = "/Users/girishsk/Documents/Shachi/CMPS209C/reviews/reviewxml/cellphone_positive_review.xml";
	static String goldProjectNamePositive = "cellphone_positive";
	//static final  Logger logger = Logger.getLogger(GoldStandardSeparated.class); //log4j
	private static final Logger logger = Logger.getLogger( reviews.class.getName() );
	
	//MMAX
	String mmaxPath;
	String projectName;

	//via this variable the goldStandard information is accessible
	//public GoldStandardOneReview[] gs; //each element contains gold standard (tokens + polarities) for 1 review
	
public static void main(String[] args){
		
		System.out.println("GoldStandard");
		String path ="/Users/girishsk/Documents/Shachi/CMPS209C/Datasets/Sentiment_GOLDSTANDARD";
		String goldStandardPath = "/Users/girishsk/Documents/Shachi/CMPS209C/Datasets/Sentiment_GOLDSTANDARD/";
		
		
		GetOtherReviews gs = new GetOtherReviews(goldStandardPath,goldProjectNamePositive);
		
	}
	
	public GetOtherReviews (String mmaxPathIn, String rawProjectName){
		
		
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
	    ArrayList<String> mID = new ArrayList();
	    while (reviewsIt.hasNext()){
	    	Markable m = reviewsIt.next();
	    	String[] disel = m.getDiscourseElements();
	    	//output.write("\n \nprinting discourse elements for reveiew"+m.getID()+"\n");
	    	//for(int i=0;i<disel.length;++i)
	    	//output.write(disel[i]+"\n");
	    	String[] markableIdsInReview = m.getDiscourseElementIDs();
	    	mID.add(m.getID());
//	    	System.out.println("\n \nprinting discourse elements for reveiew"+m.getID()+"\n");
	    }
	    
	    HashMap<String,String> map = getHashMap();
	    try
	    {
//	    FileOutputStream fos = new FileOutputStream("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/raw_reviews/cellphone_neg/cellphone_negative_review_try.txt");
//    	ObjectOutputStream oos = new ObjectOutputStream(fos);
	    	File file = new File(writepath);
		    BufferedWriter output = new BufferedWriter(new FileWriter(file));
	    for(int i = 0;i<mID.size();++i)
	    {
		    Markable rev = (Markable) reviewMarkables.get(i);
//		    String s = rev.getDiscourseElements()[0].split("_")[0].trim();
//		    System.out.println(rev.getDiscourseElements()[0]);
		    String uniqid = (rev.getDiscourseElements()[0].split("_")[0].split("!")[0].trim());
		    System.out.println(uniqid);
//		    System.out.println(map.get(rev.getDiscourseElements()[0].split("_")[0].split("!")[0].trim()));
//		    oos.writeObject(map);
	    	map.remove(uniqid);
	    	
	}
	     
	      Iterator<String> iterator =  map.values().iterator();

	      // while loop
	      while (iterator.hasNext()) {
	      output.write(iterator.next());
	      }
	      output.close();
	    System.out.println(map.keySet());
	    System.out.println(map.size());
	    }
		catch(Exception ie)
		{
			ie.printStackTrace();
		}
	    }
	    
	   
	
	    
HashMap<String,String> getHashMap()
{
	try
	{
		File review = new File(xmlpath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(review);
		doc.getDocumentElement().normalize();
		System.out.println("root of xml file" + doc.getDocumentElement().getNodeName());
		NodeList nodes = doc.getElementsByTagName("review");
		System.out.println("==========================");

		HashMap<String,String> map = new HashMap<String,String>();
		for (int i = 0; i < nodes.getLength(); i++) {
		Node node = nodes.item(i);
		

		if (node.getNodeType() == Node.ELEMENT_NODE) {
		Element element = (Element) node;
		String s = getValue("unique_id", element);
		//System.out.println(s.split("_")[0].split("!")[0].trim());
		map.put(s.split("_")[0].split("!")[0].trim() , getValue("review_text", element));
		}
	  }	
	  return map;
	}
	catch(Exception e)
	{
		System.out.println(e.getMessage());
		return null;
	}
}
	    
	    
/*	    for(int i=0; i < reviewLevel.getMarkableCount(); i++) {
			Markable[] marks = reviewLevel.getAllMarkablesAtDiscoursePosition(i);
			//System.out.println(marks[0].getDiscourseElements()[0]);
			Markable m = reviewLevel.getMarkableByID(marks[0].getID());
			System.out.println(marks[0].getID()+"\t"+"\t"+ m.getDiscourseElements() +"\n"+ marks[0].getDiscourseElements()+"\n");
	    }
	
	*/
private static String getValue(String tag, Element element) {
	NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
	Node node = (Node) nodes.item(0);
	return node.getNodeValue();
	}




	
}
