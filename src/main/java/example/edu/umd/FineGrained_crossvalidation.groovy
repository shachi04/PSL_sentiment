package example.edu.umd;
import edu.umd.cs.psl.application.inference.LazyMPEInference;
import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxPseudoLikelihood;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.random.GroundSliceRandOM;
import edu.umd.cs.psl.application.learning.weight.maxmargin.MaxMargin;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.maxmargin.PositiveMinNormProgram;
//import edu.umd.cs.psl.application.learning.weight.em.HardEM;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.VotedPerceptron;
import edu.umd.cs.psl.application.learning.weight.random.FirstOrderMetropolisRandOM
import edu.umd.cs.psl.application.learning.weight.random.HardEMRandOM
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
//
m.add rule : (possentiment(A) ) >> ~negsentiment(A), weight :5
m.add rule : (negsentiment(A) ) >> ~possentiment(A), weight :5
m.add rule : (priorpos(A) ) >> possentiment(A), weight :5
m.add rule : (priorneg(A) ) >> negsentiment(A), weight :5
/*
m.add rule : possentiment(A)>> (priorpos(A) ) , weight :1
m.add rule : negsentiment(A)>> (priorneg(A) ) , weight :1
/*
 * Without ^
 */

m.add rule : (prev(A,B) & possentiment(B)) >> possentiment(A), weight :10
m.add rule : (prev(A,B) & negsentiment(B)) >> negsentiment(A), weight :10

//
//m.add rule : (prev(A,B) & possentiment(B) & (A ^ B)) >> possentiment(A), weight :100
//m.add rule : (prev(A,B) & negsentiment(B) & (A ^ B)) >> negsentiment(A), weight :100

/*
m.add rule : (contrast(A,B) & possentiment(B) & ( A ^ B)) >> negsentiment(A)  , weight :50
m.add rule : (contrast(A,B) & negsentiment(B) & ( A ^ B)) >> possentiment(A)  , weight :50
*/
/*
m.add rule : (contrast(A,B) & possentiment(B) ) >> negsentiment(A)  , weight :50
m.add rule : (contrast(A,B) & negsentiment(B) ) >> possentiment(A)  , weight :50


/*
 * loading the predicates from the data files
 */
int folds = 10
List<Partition> trainPartition = new ArrayList<Partition>(folds)
List<Partition> trueDataPartition = new ArrayList<Partition>(folds)
List<Partition> testDataPartition = new ArrayList<Partition>(folds)
List<Partition> trueTestDataPartition = new ArrayList<Partition>(folds)
//List<Partition> trueTestNeg = new ArrayList<Partition>(folds)



for(cvSet =0 ;cvSet<10;++cvSet)
{
trainPartition.add(cvSet, new Partition(cvSet))
trueDataPartition.add(cvSet, new Partition(cvSet + folds))
testDataPartition.add(cvSet, new Partition(cvSet + 2*folds))
trueTestDataPartition.add(cvSet, new Partition(cvSet + 3*folds))
}

cvSet = 7
//for(cvSet =0 ;cvSet<10;++cvSet)
//{
	/*
	 * Train data partition, each partition has 9 folders, one kept aside for testing... 
	 */
	for (trainSet = 1 ; trainSet<=9;++trainSet)
	{
		dirToUse = 0;
		dirToUse = (cvSet+trainSet)%10
		if(dirToUse==0) dirToUse = 10;
		
		
		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold'+dirToUse+java.io.File.separator;
		//println "accessing directory "+filename+ "and train partition " +trainPartition.get(cvSet)
		
		//insert = data.getInserter(prev, trainPartition.get(i))
		//println "Train Partition === "+ data.getInserter(prev, trainPartition.get(cvSet));
		
		InserterUtils.loadDelimitedData(data.getInserter(prev, trainPartition.get(cvSet)), filename+"all_prev.csv");
		//insert = data.getInserter(priorpos, trainPartition.get(i))
		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorpos, trainPartition.get(cvSet)), filename+"softpos.csv","\t");
		//insert = data.getInserter(priorneg, trainPartition.get(i))
		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorneg, trainPartition.get(cvSet)),filename+"softneg.csv","\t");
		//insert = data.getInserter(all, trainPartition.get(i))
		InserterUtils.loadDelimitedData(data.getInserter(all, trainPartition.get(cvSet)), filename+"allID.csv");
		//insert = data.getInserter(contrast, trainPartition.get(i))
		InserterUtils.loadDelimitedData(data.getInserter(contrast, trainPartition.get(cvSet)), filename+"contrast_ids.csv");
		
//		println "directory :"+filename+"trueneg.csv, truedataPartition : " + trueDataPartition.get(i)
//		println "accessing directory "+filename+ "and truedata partition " +trueDataPartition.get(i)
		//insert = data.getInserter(negsentiment, trueDataPartition.get(i))
		InserterUtils.loadDelimitedData(data.getInserter(negsentiment, trueDataPartition.get(cvSet)), filename+"trueneg.csv");
		//insert = data.getInserter(possentiment, trueDataPartition.get(i))
		InserterUtils.loadDelimitedData(data.getInserter(possentiment, trueDataPartition.get(cvSet)), filename+"truepos.csv");
		
	}
	/*
	 * For test data partition - it needs only one fold in each partition.... Start with 10,1,2,3.... so on. 
	 */
	testSet = 0;
	testSet = (cvSet+10)%10
	if(testSet==0) testSet = 10;
	filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold'+testSet+java.io.File.separator;
	//println "accessing directory "+filename+ "and TestDatapartition " +testDataPartition.get(cvSet)
	
	//insert = data.getInserter(prev, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(data.getInserter(prev, testDataPartition.get(cvSet)), filename+"all_prev.csv");
	
	//insert = data.getInserter(priorpos, testDataPartition.get(i))
	InserterUtils.loadDelimitedDataTruth(data.getInserter(priorpos, testDataPartition.get(cvSet)), filename+"softpos.csv","\t");
	
	//insert = data.getInserter(priorneg, testDataPartition.get(i))
	InserterUtils.loadDelimitedDataTruth(data.getInserter(priorneg, testDataPartition.get(cvSet)),filename+"softneg.csv","\t");
	
	//insert = data.getInserter(all, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(data.getInserter(all, testDataPartition.get(cvSet)), filename+"allID.csv");
	
	//insert = data.getInserter(contrast, testDataPartition.get(i))
	InserterUtils.loadDelimitedData(data.getInserter(contrast, testDataPartition.get(cvSet)), filename+"contrast_ids.csv");

//	println "accessing directory "+filename+ "and trueTestData partition " +trueTestDataPartition.get(i)
	//insert = data.getInserter(possentiment, trueTestDataPartition.get(i))
	InserterUtils.loadDelimitedData(data.getInserter(possentiment, trueTestDataPartition.get(cvSet)), filename+"truepos.csv");
	
	//insert = data.getInserter(negsentiment, trueTestDataPartition.get(i))
	InserterUtils.loadDelimitedData(data.getInserter(negsentiment, trueTestDataPartition.get(cvSet)), filename+"trueneg.csv");

	
	Database trainDB = data.getDatabase(trainPartition.get(cvSet), [Contrast, Prev,Priorpos, Priorneg, All] as Set);
	
	ResultList allGroundings1 = trainDB.executeQuery(Queries.getQueryForAllAtoms(contrast))
	println "groundings for contrast" +allGroundings1.size();
	allGroundings1 = trainDB.executeQuery(Queries.getQueryForAllAtoms(prev))
	println "groundings for prev" +allGroundings1.size();


	/*
	 * Setting the predicates possentiment and negsentiment to an initial value for all groundings
	 */
	
	ResultList allGroundings = trainDB.executeQuery(Queries.getQueryForAllAtoms(all))
	println "groundings for all"+ allGroundings.size();
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
	println "trudatapartition : "+trueDataPartition.get(cvSet)
	Database trueDataDB = data.getDatabase(trueDataPartition.get(cvSet), [possentiment,negsentiment] as Set);
	MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(m, trainDB, trueDataDB, config);
	//MaxMargin weightLearning = new MaxMargin(m, trainDB, trueDataDB, config);
	
	//LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(m, db, trueDataDB, config);
	//MaxPseudoLikelihood weightLearning = new MaxPseudoLikelihood(m, trainDB, trueDataDB, config);
	weightLearning.learn();
	weightLearning.close();
	/*
	 * Newly learned weights
	 */
	
	file3.append( "Learned model:\n")
	file3.append(m)
	
	
	/*Test database setup*/

	Database testDB = data.getDatabase(testDataPartition.get(cvSet), [Contrast, Prev, Priorpos, Priorneg, All] as Set);
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
	file1.append("Partition:" + testDataPartition.get(cvSet)+"\n")
	count = 0
	println "Inference results with hand-defined weights:"
	for (GroundAtom atom : Queries.getAllAtoms(testDB, possentiment)){
//		println atom.toString() + "\t" + atom.getValue();
		file1.append(atom.toString().substring(atom.toString().indexOf('(')+1 ,atom.toString().indexOf(')')) + "\t" + atom.getValue()+"\n");
		count = count+1;
		}
	println count
	
	count = 0
	file2.append("Partition:" + testDataPartition.get(cvSet)+"\n")
	println "Inference results with hand-defined weights:"
	for (GroundAtom atom : Queries.getAllAtoms(testDB, negsentiment))
	{
//		println atom.toString() + "\t" + atom.getValue();
		file2.append(atom.toString().substring(atom.toString().indexOf('(')+1 ,atom.toString().indexOf(')') ) + "\t" + atom.getValue()+"\n");
		count = count + 1
	}
	println count
	println "Truetestdatapartition "+trueTestDataPartition.get(cvSet)
	Database trueTestDB = data.getDatabase(trueTestDataPartition.get(cvSet), [possentiment, negsentiment] as Set);
	
	
	
	Set<GroundAtom> groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
	int totalPosTestExamples = groundings1.size()
	println "possentiment total: "+totalPosTestExamples
	
	groundings1 = Queries.getAllAtoms(trueTestDB, negsentiment)
	int totalNegTestExamples = groundings1.size()
	println "negsentiment total: "+totalNegTestExamples
	
/*	def comparator = new SimpleRankingComparator(testDB)
	comparator.setBaseline(trueTestDB)
	file3.append("\n\n TEST RESULTS FOR TESTING ON FOLD "+(cvSet+1) +"\n")
	// Choosing what metrics to report
	def metrics = [ RankingScore.AUPRC, RankingScore.NegAUPRC,  RankingScore.AreaROC]
	double [] score = new double[metrics.size()]
	double [] score2 = new double[metrics.size()]
	try {
		for (j = 0; j < metrics.size(); j++) {
				comparator.setRankingScore(metrics.get(j))
				score[j] = comparator.compare(possentiment)
		}
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
		
		file3.append("\nArea under positive-class PR curve: " + score2[0]+"\n")
		file3.append("Area under negetive-class PR curve: " + score2[1]+"\n")
		file3.append("Area under ROC curve: " + score2[2]+"\n")
		println "Written neg AUC to file3!! "
	}
	catch (ArrayIndexOutOfBoundsException e) {
		System.out.println("No evaluation data! Terminating!");
	}
	*/
	Set<GroundAtom> groundings3 = Queries.getAllAtoms(trueTestDB, possentiment)
	int totalPosTestExamples3 = groundings3.size()
	
	groundings3 = Queries.getAllAtoms(trueTestDB, negsentiment)
	int totalNegTestExamples3 = groundings3.size()
	
	Set<GroundAtom> groundings2 = Queries.getAllAtoms(trueDataDB, possentiment)
	int totalPosTrainExamples = groundings2.size()
	
	groundings2 = Queries.getAllAtoms(trueDataDB, negsentiment)
	int totalNegTrainExamples = groundings2.size()
	
	int total =  totalNegTrainExamples+totalPosTestExamples3+totalNegTestExamples3+totalPosTrainExamples
	println "Total ###"+total
	println "Pos ###"+totalPosTrainExamples
	println "Ned ###"+totalNegTrainExamples
	
	
	
	/*
	 * Accuracy
	 */
	
	groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
	totalPosTestExamples = groundings1.size()
	println "printing totalTestExamples:Possentiment"+totalPosTestExamples
	groundings2 = Queries.getAllAtoms(trueTestDB, negsentiment)
	totalNegTestExamples = groundings2.size()
	println "printing totalTestExamples: Negsentiment"+totalNegTestExamples
	
	
	file3.append("\n \n Writing Accuracy, F1, P and R scores for possentiment"+"\n")
	poscomparator = new DiscretePredictionComparator(testDB)
	poscomparator.setBaseline(trueTestDB)
	poscomparator.setResultFilter(new MaxValueFilter(possentiment, 1))

//	
	DiscretePredictionStatistics stats = poscomparator.compare(possentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("\n With threshold ====" +0.5+"\n")
	poscomparator.setThreshold(0.5) // treat best value as true as long as it is nonzero

	stats = poscomparator.compare(possentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	
	file3.append("\n With threshold ====" +0.4+"\n")
	poscomparator.setThreshold(0.4) // treat best value as true as long as it is nonzero

	stats = poscomparator.compare(possentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	
	file3.append("\n With threshold ====" +0.3+"\n")
	poscomparator.setThreshold(0.3) // treat best value as true as long as it is nonzero
	stats = poscomparator.compare(possentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	file3.append( "\n With threshold 0.005 \n")
	poscomparator.setThreshold(0.005) // treat best value as true as long as it is nonzero	
	stats = poscomparator.compare(possentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	
	file3.append("\n\n Writing Accuracy, F1, P and R scores for negsentiment"+"\n")
	negcomparator = new DiscretePredictionComparator(testDB)
	negcomparator.setBaseline(trueTestDB)
	negcomparator.setResultFilter(new MaxValueFilter(negsentiment, 1))
	file3.append("\n negsentiment with threshold =====" + 0.5+"\n")
	
	negcomparator.setThreshold(0.5) // treat best value as true as long as it is nonzero
	stats = negcomparator.compare(negsentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	all_tn = negcomparator.tn+ poscomparator.tn;
	all_tp = negcomparator.tp+ poscomparator.tp;
	all_fp = negcomparator.fp+ poscomparator.fp;
	all_fn = negcomparator.fn+ poscomparator.fn;
	all_accuracy = (all_tn+all_tp)/(all_tn+all_tp+all_fp+all_fn)
	println "Overall accuracy = " +all_accuracy
	
	file3.append("\n negsentiment with threshold 0.4 \n")
	negcomparator.setThreshold(0.4) // treat best value as true as long as it is nonzero
	stats = negcomparator.compare(negsentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy() +"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	
	
	
	file3.append("\n negsentiment with threshold ===== " + 0.3+"\n")
	
	negcomparator.setThreshold(0.3) // treat best value as true as long as it is nonzero
	stats = negcomparator.compare(negsentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy()+"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")

	file3.append("\n negsentiment with threshold 0.005 \n")
	negcomparator.setThreshold(0.005) // treat best value as true as long as it is nonzero
	stats = negcomparator.compare(negsentiment, totalNegTestExamples+totalPosTestExamples)
	file3.append("Accuracy: " + stats.getAccuracy() +"\n")
	file3.append("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")
	file3.append("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)+"\n")

	
	
	
	trueDataDB.close();
	trainDB.close();
	testDB.close();
	trueTestDB.close();

//}
