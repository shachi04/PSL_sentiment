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
public class reviews implements Serializable {
	
	//static final  Logger logger = Logger.getLogger(GoldStandardSeparated.class); //log4j
	private static final Logger logger = Logger.getLogger( reviews.class.getName() );
	
	//MMAX
	String mmaxPath;
	String projectName;

	//via this variable the goldStandard information is accessible
	//public GoldStandardOneReview[] gs; //each element contains gold standard (tokens + polarities) for 1 review
	
	
	
	public reviews (String mmaxPathIn, String rawProjectName){
		
		
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
	   
	    for(int i = 0;i<mID.size();++i)
	    {
	    
	    	/*
	    	*/
	    	File file = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/raw_reviews/kitchen_neg/kitchen_negative_review"+ mID.get(i).split("_")[1] +".txt");
		    BufferedWriter output = new BufferedWriter(new FileWriter(file));
		    
		    Markable rev = (Markable) reviewMarkables.get(i);
//		    String s = rev.getDiscourseElements()[0].split("_")[0].trim();
//		    System.out.println(rev.getDiscourseElements()[0]);
		    
		    System.out.println(map.get(rev.getDiscourseElements()[0].split("_")[0].split("!")[0].trim()));
//		    oos.writeObject(map);
	    	
		    //output.write(map.values());
		    
		    output.write(map.get(rev.getDiscourseElements()[0].split("_")[0].split("!")[0].trim())); // .split(":")[0]));
			output.close();
	    	 
//	    	System.out.println(mID.get(i)+"\t"+rev.getDiscourseElements()[0] +"\t"+rev.getDiscourseElements()[1]); //.split(":")[0]);
//	    	System.out.println(map.get(rev.getDiscourseElements()[0]));
	    	
//	    	System.out.println("attr value "+rev.getAttributeValue("unique_id"));
		}
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
		File review = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/reviewxml/kitchen_negative_review.xml");
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




	public static void main(String[] args){
		
		System.out.println("GoldStandard");
		String path ="/Users/girishsk/Documents/Shachi/CMPS209C/Datasets/Sentiment_GOLDSTANDARD";
		String goldStandardPath = "/Users/girishsk/Documents/Shachi/CMPS209C/Datasets/Sentiment_GOLDSTANDARD/";
		String goldProjectNamePositive = "kitchen_negative";
		
		reviews gs = new reviews(goldStandardPath,goldProjectNamePositive);
		
	}
}
