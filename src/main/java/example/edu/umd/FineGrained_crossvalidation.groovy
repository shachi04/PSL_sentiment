package example.edu.umd;
import edu.umd.cs.psl.application.inference.LazyMPEInference;
import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.random.GroundSliceRandOM;
import edu.umd.cs.psl.application.learning.weight.maxmargin.MaxMargin;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.maxmargin.PositiveMinNormProgram;
import edu.umd.cs.psl.config.*
import edu.umd.cs.psl.database.DataStore
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.database.ResultList
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type
import edu.umd.cs.psl.evaluation.result.FullInferenceResult
import edu.umd.cs.psl.evaluation.statistics.DiscretePredictionComparator
import edu.umd.cs.psl.evaluation.statistics.DiscretePredictionStatistics
import edu.umd.cs.psl.evaluation.statistics.SimpleRankingComparator
import edu.umd.cs.psl.groovy.PSLModel;
import edu.umd.cs.psl.groovy.PredicateConstraint;
import edu.umd.cs.psl.groovy.SetComparison;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom
import edu.umd.cs.psl.model.function.ExternalFunction;
import edu.umd.cs.psl.ui.functions.textsimilarity.*
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;
import edu.umd.cs.psl.evaluation.statistics.RankingScore;
import edu.umd.cs.psl.evaluation.statistics.filter.MaxValueFilter


/*
 * Config bundle changed to accept String as UniqueID
 */
ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("fine-grained")
File file1 = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/Results/possentiment.csv");
File file2 = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/Results/negsentiment.csv");
File file3 = new File("/Users/girishsk/Documents/Shachi/CMPS209C/reviews/Results/results.csv");
/* Uses H2 as a DataStore and stores it in a temp. directory by default */
def defaultPath = System.getProperty("java.io.tmpdir")
String dbpath = config.getString("dbpath", defaultPath + File.separator + "fine-grained")
DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)

/*
 * Initialize PSL model
 */
PSLModel m = new PSLModel(this, data)


/*
 * Predicates
 */
m.add predicate: "prev" , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
m.add predicate: "contrast" , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
m.add predicate: "priorpos", types: [ArgumentType.UniqueID]
m.add predicate: "priorneg", types: [ArgumentType.UniqueID]
m.add predicate: "possentiment", types: [ArgumentType.UniqueID]
m.add predicate: "negsentiment", types: [ArgumentType.UniqueID]
m.add predicate: "all", types: [ArgumentType.UniqueID]

/*
 * Adding rules
 */
m.add rule : (possentiment(A) ) >> ~negsentiment(A), weight :1
m.add rule : (negsentiment(A) ) >> ~possentiment(A), weight :1
m.add rule : (priorpos(A) ) >> possentiment(A), weight :1
m.add rule : (priorneg(A) ) >> negsentiment(A), weight :1

/*
 * Without ^
 *//*
m.add rule : (prev(A,B) & possentiment(B)) >> possentiment(A), weight :1
m.add rule : (prev(A,B) & negsentiment(B)) >> negsentiment(A), weight :1
*/
m.add rule : (prev(A,B) & possentiment(B) & (A ^ B)) >> possentiment(A), weight :1
m.add rule : (prev(A,B) & negsentiment(B) & (A ^ B)) >> negsentiment(A), weight :1
/*
m.add rule : (contrast(A,B) & possentiment(A) & (A ^ B)) >> negsentiment(B)  , weight :1
m.add rule : (contrast(A,B) & negsentiment(B) & (A ^ B)) >> possentiment(B)  , weight :1
*/
/*
 * Printing model
 */
println m;

/*
 * loading the predicates from the data files
 */
int folds = 10
List<Partition> trainPartition = new ArrayList<Partition>(folds)
List<Partition> trueDataPartition = new ArrayList<Partition>(folds)
List<Partition> testDataPartition = new ArrayList<Partition>(folds)
List<Partition> trueTestDataPartition = new ArrayList<Partition>(folds)
//List<Partition> trueTestNeg = new ArrayList<Partition>(folds)
int i = 0, j = 0;

for ( i = 0; i <folds; i++) {
	trainPartition.add(i, new Partition(i))
	trueDataPartition.add(i, new Partition(i + folds))
	testDataPartition.add(i, new Partition(i + 2*folds))
	trueTestDataPartition.add(i, new Partition(i + 3*folds))
}

for(i =0 ;i<10;++i)
{
//i = 5;
	/*
	 * Train data partition, each partition has 9 folders, one kept aside for testing... 
	 */
	for (j = 1 ; j<=9;++j)
	{
		k = (i+j)%10
		if(k==0) k = 10;
			
		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold'+k+java.io.File.separator;
		println "accessing directory "+filename+ "and train partition " +trainPartition.get(i)
		
		insert = data.getInserter(prev, trainPartition.get(i))
		InserterUtils.loadDelimitedData(insert, filename+"all_prev.csv");
		insert = data.getInserter(priorpos, trainPartition.get(i))
		InserterUtils.loadDelimitedData(insert, filename+"positive.csv");
		insert = data.getInserter(priorneg, trainPartition.get(i))
		InserterUtils.loadDelimitedData(insert,filename+"negative.csv");
		insert = data.getInserter(all, trainPartition.get(i))
		InserterUtils.loadDelimitedData(insert, filename+"allID.csv");
		insert = data.getInserter(contrast, trainPartition.get(i))
		InserterUtils.loadDelimitedData(insert, filename+"contrast_ids.csv");
		
//		println "directory :"+filename+"trueneg.csv, truedataPartition : " + trueDataPartition.get(i)
//		println "accessing directory "+filename+ "and truedata partition " +trueDataPartition.get(i)
		insert = data.getInserter(negsentiment, trueDataPartition.get(i))
		InserterUtils.loadDelimitedData(insert, filename+"trueneg.csv");
		insert = data.getInserter(possentiment, trueDataPartition.get(i))
		InserterUtils.loadDelimitedData(insert, filename+"truepos.csv");
		
	}
	/*
	 * For test data partition - it needs only one fold in each partition.... Start with 10,1,2,3.... so on. 
	 */
	k = (i+10)%10
	if(k==0) k = 10;
	filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold'+k+java.io.File.separator;
	println "accessing directory "+filename+ "and TestDatapartition " +testDataPartition.get(i)
	
	insert = data.getInserter(prev, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert, filename+"all_prev.csv");
	
	insert = data.getInserter(priorpos, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert, filename+"positive.csv");
	
	insert = data.getInserter(priorneg, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert,filename+"negative.csv");
	
	insert = data.getInserter(all, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert, filename+"allID.csv");
	
	insert = data.getInserter(contrast, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert, filename+"contrast_ids.csv");

//	println "accessing directory "+filename+ "and trueTestData partition " +trueTestDataPartition.get(i)
	insert = data.getInserter(possentiment, trueTestDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert, filename+"truepos.csv");
	
	insert = data.getInserter(negsentiment, trueTestDataPartition.get(i))
	InserterUtils.loadDelimitedData(insert, filename+"trueneg.csv");

	
	Database trainDB = data.getDatabase(trainPartition.get(i), [Contrast, Prev,Priorpos, Priorneg, All] as Set);

	/*
	 * Setting the predicates possentiment and negsentiment to an initial value for all groundings
	 */
	
	ResultList allGroundings = trainDB.executeQuery(Queries.getQueryForAllAtoms(all))
	print allGroundings.size();
	for (j = 0; j < allGroundings.size(); j++) {
		GroundTerm [] grounding = allGroundings.get(j)
		RandomVariableAtom atom1 = trainDB.getAtom(possentiment, grounding);
		RandomVariableAtom atom2 = trainDB.getAtom(negsentiment, grounding);
		atom1.setValue(0.0);
		atom2.setValue(0.0);
		atom1.commitToDB();
		atom2.commitToDB();
	}
	
	MPEInference inferenceApp = new MPEInference(m,trainDB, config)
	//LazyMPEInference inferenceApp = new LazyMPEInference(m, trainDB, config);
	inferenceApp.mpeInference();
	inferenceApp.close();
	println "trudatapartition : "+trueDataPartition.get(i)
	Database trueDataDB = data.getDatabase(trueDataPartition.get(i), [possentiment,negsentiment] as Set);
	MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(m, trainDB, trueDataDB, config);
	//LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(m, db, trueDataDB, config);
	weightLearning.learn();
	weightLearning.close();
	/*
	 * Newly learned weights
	 */
	
	println "Learned model:"
	println m
	
	
	/*Test database setup*/

	Database testDB = data.getDatabase(testDataPartition.get(i), [Contrast, Prev, Priorpos, Priorneg, All] as Set);
	
	
	ResultList groundings = testDB.executeQuery(Queries.getQueryForAllAtoms(all))
	print groundings.size();
	for (j = 0; j < groundings.size(); j++) {
		GroundTerm [] grounding = groundings.get(j)
		RandomVariableAtom atom1 = testDB.getAtom(possentiment, grounding);
		RandomVariableAtom atom2 = testDB.getAtom(negsentiment, grounding);
		atom1.setValue(0.0);
		atom2.setValue(0.0);
		atom1.commitToDB();
		atom2.commitToDB();
	}
	inferenceApp = new MPEInference(m, testDB,config)
	inferenceApp.mpeInference();
	inferenceApp.close();
	
	
	println "test results";
	file1.append("Partition:" + testDataPartition.get(i)+"\n")
	count = 0
	println "Inference results with hand-defined weights:"
	for (GroundAtom atom : Queries.getAllAtoms(testDB, possentiment)){
//		println atom.toString() + "\t" + atom.getValue();
		file1.append(atom.toString().substring(atom.toString().indexOf('(')+1 ,atom.toString().indexOf(')')) + "\t" + atom.getValue()+"\n");
		count = count+1;
		}
	println count
	
	count = 0
	file2.append("Partition:" + testDataPartition.get(i)+"\n")
	println "Inference results with hand-defined weights:"
	for (GroundAtom atom : Queries.getAllAtoms(testDB, negsentiment))
	{
//		println atom.toString() + "\t" + atom.getValue();
		file2.append(atom.toString().substring(atom.toString().indexOf('(')+1 ,atom.toString().indexOf(')') ) + "\t" + atom.getValue()+"\n");
		count = count + 1
	}
	println count
	println "Truetestdatapartition "+trueTestDataPartition.get(i)
	Database trueTestDB = data.getDatabase(trueTestDataPartition.get(i), [possentiment, negsentiment] as Set);
	
	
	
	Set<GroundAtom> groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
	int totalPosTestExamples = groundings1.size()
	println "possentiment total: "+totalPosTestExamples
	
	groundings1 = Queries.getAllAtoms(trueTestDB, negsentiment)
	int totalNegTestExamples = groundings1.size()
	println "negsentiment total: "+totalNegTestExamples
	
	def comparator = new SimpleRankingComparator(testDB)
	comparator.setBaseline(trueTestDB)
	file3.append("\n\n TEST RESULTS FOR TESTING ON FOLD "+(i+1) +"\n")
	// Choosing what metrics to report
	def metrics = [ RankingScore.AUPRC, RankingScore.NegAUPRC,  RankingScore.AreaROC]
	double [] score = new double[metrics.size()]
	double [] score2 = new double[metrics.size()]
	try {
		for (j = 0; j < metrics.size(); j++) {
				comparator.setRankingScore(metrics.get(j))
				score[j] = comparator.compare(possentiment)
		}
		println "Written pos AUC to file3!! "
		file3.append("\n \n Writing possentiment AUC scores"+"\n")
		file3.append("\nArea under positive-class PR curve: " + score[0]+"\n")
		file3.append("Area under negetive-class PR curve: " + score[1]+"\n")
		file3.append("Area under ROC curve: " + score[2]+"\n")
	
	}
	catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No evaluation data! Terminating!");
		}
	
	comparator.setBaseline(trueTestDB)
	// Choosing what metrics to report
	
	try {
		for (j = 0; j < metrics.size(); j++) {
				comparator.setRankingScore(metrics.get(j))
				score2[j] = comparator.compare(negsentiment)
		}
		
		file3.append("\n \n Writing negsentiment AUC scores"+"\n")
		file3.append("\nArea under positive-class PR curve: " + score2[0]+"\n")
		file3.append("Area under negetive-class PR curve: " + score2[1]+"\n")
		file3.append("Area under ROC curve: " + score2[2]+"\n")
		println "Written neg AUC to file3!! "
	}
	catch (ArrayIndexOutOfBoundsException e) {
		System.out.println("No evaluation data! Terminating!");
	}
	
	
	/*
	 * Accuracy
	 */
	
	file3.append("\n \n Writing Accuracy, F1, P and R scores for possentiment"+"\n")
	comparator = new DiscretePredictionComparator(testDB)
	comparator.setBaseline(trueTestDB)
	comparator.setResultFilter(new MaxValueFilter(possentiment, 1))
	file3.append( "With threshold 0.005 \n")
	comparator.setThreshold(0.005) // treat best value as true as long as it is nonzero
	
	groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
	 totalTestExamples = groundings1.size()
	println "printing totalTestExamples:Possentiment"+totalTestExamples
	DiscretePredictionStatistics stats = comparator.compare(possentiment, totalTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	file3.append("With threshold 0.5\n")
	comparator.setThreshold(0.5) // treat best value as true as long as it is nonzero
	
	groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
	 totalTestExamples = groundings1.size()
	println "printing totalTestExamples:Possentiment"+totalTestExamples
	stats = comparator.compare(possentiment, totalTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	println "Written pos scores to file3!! "
	
	file3.append("\n\n Writing Accuracy, F1, P and R scores for negsentiment"+"\n")
	comparator = new DiscretePredictionComparator(testDB)
	comparator.setBaseline(trueTestDB)
	comparator.setResultFilter(new MaxValueFilter(negsentiment, 1))
	file3.append("negsentiment with threshold 0.5 \n")
	comparator.setThreshold(0.5) // treat best value as true as long as it is nonzero
	
	 groundings1 = Queries.getAllAtoms(trueTestDB, negsentiment)
	totalTestExamples = groundings1.size()
	println "printing totalTestExamples: Negsentiment"+totalTestExamples
	stats = comparator.compare(negsentiment, totalTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy())
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	file3.append("negsentiment with threshold 0.005 \n")
	comparator.setThreshold(0.005) // treat best value as true as long as it is nonzero
	
	 groundings1 = Queries.getAllAtoms(trueTestDB, negsentiment)
	totalTestExamples = groundings1.size()
	println "printing totalTestExamples: Negsentiment"+totalTestExamples
	stats = comparator.compare(negsentiment, totalTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy() +"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	println "Written neg scores to file3!! "

	
	trueDataDB.close();
	trainDB.close();
	testDB.close();
	trueTestDB.close();

}
