package example.edu.umd;
import edu.umd.cs.psl.application.inference.LazyMPEInference;
import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxPseudoLikelihood;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.random.GroundSliceRandOM;
import edu.umd.cs.psl.application.learning.weight.maxmargin.MaxMargin;
import edu.umd.cs.psl.application.learning.weight.em.DualEM
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.maxmargin.PositiveMinNormProgram;
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




class PSL_with_MLNdata
{

	public static void main(String[] args)
	{
		for(int i = 2; i <5; ++i)
		{
			PSL_with_MLNdata a = new PSL_with_MLNdata()
			a.pslmodel(i);
		}
	}

	void pslmodel(int folds)
	{
		/*
		 * Config bundle changed to accept String as UniqueID
		 */
		ConfigManager cm = ConfigManager.getManager()
		ConfigBundle config = cm.getBundle("psl_mln")
		String writefolder = System.getProperty("user.home") +
				"/Documents/Shachi/CMPS209C/reviews/Results/PSL_neighborhood_lazy/"
		File file3 = new File(writefolder+"results.csv");


		/* Uses H2 as a DataStore and stores it in a temp. directory by default */
		def defaultPath = System.getProperty("java.io.tmpdir")
		String dbpath = config.getString("dbpath", defaultPath + File.separator + "psl_mln")
		DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)

		/*
		 * Initialize PSL model
		 */
		PSLModel m = new PSLModel(this, data)


		/*
		 * Predicates
		 */
		m.add predicate: "prev" , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
		m.add predicate: "priorpos", types: [ArgumentType.UniqueID]
		m.add predicate: "priorneg", types: [ArgumentType.UniqueID]
		m.add predicate: "possentiment", types: [ArgumentType.UniqueID]
		m.add predicate: "negsentiment", types: [ArgumentType.UniqueID]
		m.add predicate: "all", types: [ArgumentType.UniqueID]
		m.add predicate: "unigrampos", types: [ArgumentType.UniqueID]
		m.add predicate: "unigramneg", types: [ArgumentType.UniqueID]
		m.add predicate: "tglpos", types: [ArgumentType.UniqueID]
		m.add predicate: "tglneg", types: [ArgumentType.UniqueID]


		/*
		 * Adding rules
		 */

		/*
		 * Rules for attribute features alone - sentiment lexicons as source
		 */
		m.add rule : (possentiment(A) ) >> ~negsentiment(A), weight:1
		m.add rule : (all(A) & ~negsentiment(A)) >> possentiment(A), weight:1
				
		m.add rule : (priorpos(A) ) >> possentiment(A), weight :1, squared : false
		m.add rule : (priorneg(A) ) >> negsentiment(A), weight :1, squared : false
	
		m.add rule : unigrampos(A) >> possentiment(A), weight : 1, squared : false
		m.add rule : unigramneg(A) >> negsentiment(A), weight : 1, squared : false

		m.add rule : tglpos(A) >> possentiment(A), weight : 1, squared : false
		m.add rule : tglneg(A) >> negsentiment(A), weight : 1, squared : false
//
		/*  
		 * Rules for Neighborhood relation
		 * 
		 * 
		 */
		m.add rule : (prev(A,B) & possentiment(A)) >> possentiment(B), weight :1, squared : false
		m.add rule : (prev(A,B) & negsentiment(A)) >> negsentiment(B), weight :1, squared : false
		int numfolds = 10
		
		Partition trainPartition = new Partition(0)
		Partition trueDataPartition = new Partition(1)
		
		Partition testDataPartition = new Partition(2)
		Partition trueTestDataPartition = new Partition(3)

		
		
//		List<Partition> trainPartition = new ArrayList<Partition>(numfolds)
//		List<Partition> trueDataPartition = new ArrayList<Partition>(numfolds)
//		List<Partition> testDataPartition = new ArrayList<Partition>(numfolds)
//		List<Partition> trueTestDataPartition = new ArrayList<Partition>(numfolds)


		/*
		 * Initialize partitions for all cross validation sets
		 */

		
//		for(int initset =0 ;initset<10;++initset)
//		{
//			trainPartition.add(initset, new Partition(initset))
//			trueDataPartition.add(initset, new Partition(initset + numfolds))
//			testDataPartition.add(initset, new Partition(initset + 2*numfolds))
//			trueTestDataPartition.add(initset, new Partition(initset + 3*numfolds))
//		}

		String filename1 = writefolder+"fold"+folds+"/possentiment.csv"
		String filename2 = writefolder+"fold"+folds+"/negsentiment.csv"
		File file1 = new File(filename1);
		File file2 = new File(filename2);
		File file4 = new File(writefolder+"auc.csv");
		File file5 = new File(writefolder+"model.csv");
		/*
		 * Train data partition, each partition has 9 folders, one kept aside for testing... 
		 * 
		 * loading the predicates from the data files into the trainPartition
		 */
		String filename
		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+
				'PSL_neighborhood/fold'+folds+java.io.File.separator+'train'+java.io.File.separator;
		
//		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+
//		'PSL_neighborhood/fold1_old'+java.io.File.separator+'test'+java.io.File.separator;


		InserterUtils.loadDelimitedDataTruth(data.getInserter(unigrampos, trainPartition),
				filename+"pos_UL.csv"," ");
		InserterUtils.loadDelimitedDataTruth(data.getInserter(unigramneg, trainPartition),
				filename+"neg_UL.csv"," ");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(tglpos, trainPartition),
				filename+"pos_TGL.csv"," ");
		InserterUtils.loadDelimitedDataTruth(data.getInserter(tglneg, trainPartition),
				filename+"neg_TGL.csv"," ");

		
		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorpos, trainPartition),
				filename+"pos_SWN.csv"," ");
		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorneg, trainPartition),
				filename+"neg_SWN.csv"," ");
//		filename2 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+
//			'PSL_neighborhood/fold'+folds+java.io.File.separator+'test'+java.io.File.separator;

		InserterUtils.loadDelimitedData(data.getInserter(all, trainPartition), filename+"all_segments.csv");
		InserterUtils.loadDelimitedData(data.getInserter(prev, trainPartition), filename+"relation_other.csv");
		

		/*
		 * Load in the ground truth positive and negative segments
		 */
		InserterUtils.loadDelimitedData(data.getInserter(negsentiment, trueDataPartition),
				filename+"true_neg.csv");
		InserterUtils.loadDelimitedData(data.getInserter(possentiment, trueDataPartition),
				filename+"true_pos.csv");



		/*
		 * For test data partition - it needs only one fold in each partition.... Start with 10,1,2,3.... so on. 
		 */
		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+
				'PSL_neighborhood/fold'+folds+java.io.File.separator+'test'+java.io.File.separator;

//		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+
//				'PSL_neighborhood/fold1_old'+java.io.File.separator+'test'+java.io.File.separator;
//
				
		println filename
		InserterUtils.loadDelimitedDataTruth(data.getInserter(unigrampos,
				testDataPartition), filename+"pos_UL.csv"," ");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(unigramneg,
				testDataPartition), filename+"neg_UL.csv"," ");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorpos, testDataPartition),
				filename+"pos_SWN.csv"," ");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorneg, testDataPartition),
				filename+"neg_SWN.csv"," ");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(tglpos, testDataPartition),
				filename+"pos_TGL.csv"," ");
		InserterUtils.loadDelimitedDataTruth(data.getInserter(tglneg, testDataPartition),
				filename+"neg_TGL.csv"," ");

		InserterUtils.loadDelimitedData(data.getInserter(all, testDataPartition), filename+"all_segments.csv");
		InserterUtils.loadDelimitedData(data.getInserter(prev, testDataPartition),
			(filename+"relation_other.csv"));
		
		/*
		 * Load in the ground truth positive and negative segments
		 */
		InserterUtils.loadDelimitedData(data.getInserter(possentiment, trueTestDataPartition),
				filename+"true_pos.csv");

		InserterUtils.loadDelimitedData(data.getInserter(negsentiment, trueTestDataPartition),
				filename+"true_neg.csv");


		Database trainDB = data.getDatabase(trainPartition, [ Prev,Tglpos, Tglneg, Priorpos,
			Priorneg, Unigramneg,Unigrampos,All] as Set);

//
		/*
		 * Setting the predicates possentiment and negsentiment to an initial value for all groundings
		 */
		List<Double> thresholdList = [0.5]
		ResultList allGroundings = trainDB.executeQuery(Queries.getQueryForAllAtoms(all))
		
		println "groundings for all  "+ allGroundings.size();
		
		ResultList allGroundingsprev = trainDB.executeQuery(Queries.getQueryForAllAtoms(prev))
		println "groundings for prev "+ allGroundingsprev.size();
		int j
		for (j = 0; j < allGroundings.size(); j++) {
			GroundTerm [] grounding = allGroundings.get(j)
			RandomVariableAtom atom1 = trainDB.getAtom(possentiment, grounding);
			RandomVariableAtom atom2 = trainDB.getAtom(negsentiment, grounding);
			atom1.setValue(0.001);
			println atom1
			atom2.setValue(0.001);
			atom1.commitToDB();
			atom2.commitToDB();
		}

		MPEInference inferenceApp = new MPEInference(m,trainDB, config)
		//LazyMPEInference inferenceApp = new LazyMPEInference(m, trainDB, config);
		inferenceApp.mpeInference();
		inferenceApp.close();
		println "trudatapartition : "+trueDataPartition
		Database trueDataDB = data.getDatabase(trueDataPartition, [possentiment,negsentiment] as Set);
//		MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(m, trainDB, trueDataDB, config);
		//MaxMargin weightLearning = new MaxMargin(m, trainDB, trueDataDB, config);
//		DualEM weightLearning = new DualEM(m, trainDB, trueDataDB, config)
//		LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(m, trainDB, trueDataDB, config);
		MaxPseudoLikelihood weightLearning = new MaxPseudoLikelihood(m, trainDB, trueDataDB, config);
		weightLearning.learn();
		weightLearning.close();
		
		/*
		 * Newly learned weights
		 */
		file5.append(folds)
		file5.append("\n")
		file5.append(m)
		file5.append("\n")

		/*Test database setup*/

		Database testDB = data.getDatabase(testDataPartition,
				[ Prev, Tglpos, Tglneg, Priorpos, Priorneg, Unigramneg,Unigrampos, All] as Set);
//Unigrampos,Unigramneg,
			
		ResultList groundings = testDB.executeQuery(Queries.getQueryForAllAtoms(all))
		print groundings.size();
		for (j = 0; j < groundings.size(); j++) {
			GroundTerm [] grounding = groundings.get(j)
			RandomVariableAtom atom1 = testDB.getAtom(possentiment, grounding);
			RandomVariableAtom atom2 = testDB.getAtom(negsentiment, grounding);
			atom1.setValue(0.001);
			atom2.setValue(0.001);
			atom1.commitToDB();
			atom2.commitToDB();
		}
		inferenceApp = new MPEInference(m, testDB,config)
		inferenceApp.mpeInference();
		inferenceApp.close();




		println "test results";
		//file1.append("Partition:" + testDataPartition.get(cvSet)+"\n")
		int count = 0
		println "Inference results with hand-defined weights:"
		for (GroundAtom atom : Queries.getAllAtoms(testDB, possentiment)){
			//		println atom.toString() + "\t" + atom.getValue();
			file1.append(atom.toString().substring(atom.toString().indexOf('(')+1
					,atom.toString().indexOf(')')) + "\t" + atom.getValue()+"\n");
			count = count+1;
		}
		println count

		count = 0
		file2.append("Partition:" + testDataPartition+"\n")
		println "Inference results with hand-defined weights:"
		for (GroundAtom atom : Queries.getAllAtoms(testDB, negsentiment))
		{
			//		println atom.toString() + "\t" + atom.getValue();
			file2.append(atom.toString().substring(atom.toString().indexOf('(')+1
					,atom.toString().indexOf(')') ) + "\t" + atom.getValue()+"\n");
			count = count + 1
		}
		println count
		println "Truetestdatapartition "+trueTestDataPartition
		Database trueTestDB = data.getDatabase(trueTestDataPartition, [possentiment, negsentiment] as Set);



		Set<GroundAtom> groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
		int totalPosTestExamples = groundings1.size()
		println "possentiment total: "+totalPosTestExamples

		groundings1 = Queries.getAllAtoms(trueTestDB, negsentiment)
		int totalNegTestExamples = groundings1.size()
		println "negsentiment total: "+totalNegTestExamples

		//file4.append("Testfold" +"\t"+ "sentiment" +"\t"+ "AUPRC" +"\t"+ "NEGAUPRC"+"\t"+"AreaROC \n")

		def comparator = new SimpleRankingComparator(testDB)
		comparator.setBaseline(trueTestDB)

		// Choosing what metrics to report
		def metrics = [ RankingScore.AUPRC, RankingScore.NegAUPRC,  RankingScore.AreaROC]
		double [] score = new double[metrics.size()]
		double [] score2 = new double[metrics.size()]
		try {
			for (j = 0; j < metrics.size(); j++) {
				comparator.setRankingScore(metrics.get(j))
				score[j] = comparator.compare(possentiment)
				score2[j] = comparator.compare(negsentiment)
			}
			file4.append(folds +"\t"+ "possentiment" +"\t"+ score[0] +"\t"+ score[1]+"\t"+score[2]+"\n")
			file4.append(folds +"\t"+ "negsentiment" +"\t"+ score2[0] +"\t"+ score2[1]+"\t"+score2[2]+"\n")
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No evaluation data! Terminating!");
		}

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

		println ( "Learned model:\n")
		println m

		/*
		 * Accuracy
		 */

		groundings1 = Queries.getAllAtoms(trueTestDB, possentiment)
		totalPosTestExamples = groundings1.size()
		println "printing totalTestExamples:Possentiment"+totalPosTestExamples
		groundings2 = Queries.getAllAtoms(trueTestDB, negsentiment)
		totalNegTestExamples = groundings2.size()
		println "printing totalTestExamples: Negsentiment"+totalNegTestExamples


		//file3.append("\n scores for" +"\t"+"possentiment"+"\n")
		DiscretePredictionComparator poscomparator = new DiscretePredictionComparator(testDB)
		poscomparator.setBaseline(trueTestDB)
		poscomparator.setResultFilter(new MaxValueFilter(possentiment, 1))

		DiscretePredictionStatistics stats;
		Double accuracy = 0
		Double f1 = 0
		Double p = 0
		Double r = 0
		//file3.append("CVSet"+"\t"+"Pol"+"\t"+"Th"+"\t"+"Accuracy"+"\t"+"F1"+"\t"+"Precision"+"\t"+"Recall"+"\n")


		for(threshold in thresholdList)
		{

			//	file3.append("\n With threshold " +"\t"+threshold+"\n")
			poscomparator.setThreshold(threshold) // treat best value as true as long as it is nonzero

			stats = poscomparator.compare(possentiment, totalNegTestExamples+totalPosTestExamples)
			accuracy = stats.getAccuracy()
			f1 = stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)
			p = stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)
			r = stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)
			file3.append(folds+"\t"+"pos"+"\t"+threshold+"\t"+accuracy+"\t"+f1+"\t"+p+"\t"+r+"\n")
		}
		DiscretePredictionComparator negcomparator = new DiscretePredictionComparator(testDB)
		negcomparator.setBaseline(trueTestDB)
		negcomparator.setResultFilter(new MaxValueFilter(negsentiment, 1))

		for (threshold in thresholdList)
		{
			//	file3.append("\n negsentiment with threshold =====" + threshold+"\n")

			negcomparator.setThreshold(threshold) // treat best value as true as long as it is nonzero
			stats = negcomparator.compare(negsentiment, totalNegTestExamples+totalPosTestExamples)
			accuracy = stats.getAccuracy()
			f1 = stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)
			p = stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE)
			r = stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE)
			file3.append(folds+"\t"+"neg"+"\t"+threshold+"\t"+accuracy+"\t"+f1+"\t"+p+"\t"+r+"\n")
		}
		/*all_tn = negcomparator.tn+ poscomparator.tn;
		 all_tp = negcomparator.tp+ poscomparator.tp;
		 all_fp = negcomparator.fp+ poscomparator.fp;
		 all_fn = negcomparator.fn+ poscomparator.fn;
		 all_accuracy = (all_tn+all_tp)/(all_tn+all_tp+all_fp+all_fn)
		 println "Overall accuracy = " +all_accuracy*/



		trueDataDB.close();
		trainDB.close();
		testDB.close();
		trueTestDB.close();
	}



}

