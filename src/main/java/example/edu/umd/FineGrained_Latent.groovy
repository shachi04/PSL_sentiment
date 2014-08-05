package example.edu.umd
import edu.umd.cs.psl.application.inference.LazyMPEInference;
import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxPseudoLikelihood;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.random.GroundSliceRandOM;
import edu.umd.cs.psl.application.learning.weight.maxmargin.MaxMargin;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.maxmargin.PositiveMinNormProgram;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.VotedPerceptron;
import edu.umd.cs.psl.application.learning.weight.random.FirstOrderMetropolisRandOM
import edu.umd.cs.psl.application.learning.weight.random.HardEMRandOM
import edu.umd.cs.psl.application.learning.weight.em.DualEM
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
import edu.umd.cs.psl.database.DatabasePopulator


class FineGrained_Latent {

	public static void main(String[] args)
	{
		for(int i = 0; i <10; ++i)
		{
			FineGrained_Latent a = new FineGrained_Latent()
			a.pslmodel(i);
		}
	}

	void pslmodel(int cvSet)
	{
		/*
		 * Config bundle changed to accept String as UniqueID
		 */
		ConfigManager cm = ConfigManager.getManager()
		ConfigBundle config = cm.getBundle("fine-grained")
		String writefolder = System.getProperty("user.home") + "/Documents/Shachi/CMPS209C/reviews/Results/prev_and_contrast/"
		File file3 = new File(writefolder+"results.csv");


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
		m.add predicate: "noncontrast", types:[ArgumentType.UniqueID, ArgumentType.UniqueID]
		m.add predicate: "priorpos", types: [ArgumentType.UniqueID]
		m.add predicate: "priorneg", types: [ArgumentType.UniqueID]
		m.add predicate: "possentiment", types: [ArgumentType.UniqueID]
		m.add predicate: "negsentiment", types: [ArgumentType.UniqueID]
		m.add predicate: "all", types: [ArgumentType.UniqueID]
		m.add predicate: "hiddenpriorpos", types: [ArgumentType.UniqueID]
		m.add predicate: "hiddenpriorneg", types: [ArgumentType.UniqueID] 


		/*
		 * Adding rules
		 */

		/*
		 * Rules for attribute features alone - sentiment lexicons as source
		 */
		m.add rule : (possentiment(A) ) >> ~negsentiment(A), constraint:true
		//m.add rule : (negsentiment(A) ) >> ~possentiment(A), weight :5
		m.add rule : (all(A) & ~negsentiment(A)) >> possentiment(A), constraint:true

		m.add rule : hiddenpriorpos(A) >> possentiment(A), weight:5, squared : false
		m.add rule : hiddenpriorneg(A) >> negsentiment(A), weight:5, squared : false
		
		m.add rule : possentiment(A) >> hiddenpriorpos(A), weight:5, squared : false
		m.add rule : negsentiment(A) >> hiddenpriorneg(A), weight:5, squared : false
		
		m.add rule : (priorpos(A) ) >> hiddenpriorpos(A), weight :5, squared : false
		m.add rule : (priorneg(A) ) >> hiddenpriorneg(A), weight :5, squared : false

		/*  
		 * Rules for Neighborhood relation
		 * 
		 * 
		 */
		m.add rule : (prev(A,B) &  possentiment(B)) >> possentiment(A), weight :5, squared : false
		m.add rule : (prev(A,B) &  negsentiment(B)) >> negsentiment(A), weight :5, squared : false

		/*
		 * Rules for contrast and non-contrast relation
		 */

		int folds = 10


		/*
		 * The results are shown for all threshold levels.
		 */

		/*
		 * initialize partitions
		 */
		Partition trainPartition = new Partition(0)
		Partition predict_tr = new Partition(1)
		Partition dummy_tr = new Partition(2)
		Partition trueDataPartition = new Partition(3)
		
		Partition testDataPartition = new Partition(4)
		Partition predict_te = new Partition(5);
		Partition dummy_test = new Partition(6)
		Partition trueTestDataPartition = new Partition(7)
		

		/*
		 * Set the cross validation fold set
		 */
		//cvSet = 9
		/*
		 * Set the folder to write into
		 */
		Integer folder = (cvSet+10)%10;
		if (folder ==0) folder = 10
		String filename1 = writefolder+"fold"+folder+"/possentiment.csv"
		String filename2 = writefolder+"fold"+folder+"/negsentiment.csv"
		File file1 = new File(filename1);
		File file2 = new File(filename2);
		File file4 = new File(writefolder+"auc.csv");
		/*
		 * Train data partition, each partition has 9 folders, one kept aside for testing... 
		 * 
		 * loading the predicates from the data files into the trainPartition
		 */
		String filename
		Integer trainSet
		for (trainSet = 1 ; trainSet<=9;++trainSet)
		{
			Integer dirToUse = 0;
			dirToUse = (cvSet+trainSet)%10
			if(dirToUse==0) dirToUse = 10;


			filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold'+dirToUse+java.io.File.separator;
			InserterUtils.loadDelimitedData(data.getInserter(prev, trainPartition), filename+"all_prev.csv");

			InserterUtils.loadDelimitedDataTruth(data.getInserter(priorpos, trainPartition),
					filename+"wordnet_negation_changedpos.csv","\t");
			InserterUtils.loadDelimitedDataTruth(data.getInserter(priorneg, trainPartition),
					filename+"wordnet_negation_changedneg.csv","\t");
			InserterUtils.loadDelimitedData(data.getInserter(all, trainPartition), filename+"allID.csv");

			/*
			 * Load in the ground truth positive and negative segments
			 */
			InserterUtils.loadDelimitedData(data.getInserter(negsentiment, trueDataPartition), 
				filename+"trueneg_other.csv");
			InserterUtils.loadDelimitedData(data.getInserter(possentiment, trueDataPartition), 
				filename+"truepos_other.csv");
			
			/*
			 * Populate dummy_tr for predicates
			 */
			InserterUtils.loadDelimitedData(data.getInserter(negsentiment, dummy_tr),
				filename+"allID.csv");
			InserterUtils.loadDelimitedData(data.getInserter(possentiment, dummy_tr),
				filename+"allID.csv");
			InserterUtils.loadDelimitedData(data.getInserter(hiddenpriorpos, dummy_tr),
				filename+"allID.csv");
			InserterUtils.loadDelimitedData(data.getInserter(hiddenpriorneg, dummy_tr),
				filename+"allID.csv");				
		}


		/*
		 * For test data partition - it needs only one fold in each partition.... Start with 10,1,2,3.... so on. 
		 */
		Integer testSet = 0;
		testSet = (cvSet+10)%10
		if(testSet==0) testSet = 10;
		filename = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold'+testSet+java.io.File.separator;

		InserterUtils.loadDelimitedData(data.getInserter(prev, testDataPartition), filename+"all_prev.csv");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorpos, testDataPartition),
				filename+"wordnet_negation_changedpos.csv","\t");

		InserterUtils.loadDelimitedDataTruth(data.getInserter(priorneg, testDataPartition),
				filename+"wordnet_negation_changedneg.csv","\t");

		InserterUtils.loadDelimitedData(data.getInserter(all, testDataPartition), filename+"allID.csv");

		/*
		 * Load in the ground truth positive and negative segments
		 */
		InserterUtils.loadDelimitedData(data.getInserter(possentiment, trueTestDataPartition), 
			filename+"truepos_other.csv");

		InserterUtils.loadDelimitedData(data.getInserter(negsentiment, trueTestDataPartition), 
			filename+"trueneg_other.csv");
		
		InserterUtils.loadDelimitedData(data.getInserter(negsentiment, dummy_test),
			filename+"allID.csv");
		InserterUtils.loadDelimitedData(data.getInserter(possentiment, dummy_test),
			filename+"allID.csv");
		InserterUtils.loadDelimitedData(data.getInserter(hiddenpriorpos, dummy_test),
			filename+"allID.csv");
		InserterUtils.loadDelimitedData(data.getInserter(hiddenpriorneg, dummy_test),
			filename+"allID.csv");



//		Database trainDB = data.getDatabase(trainPartition.get(cvSet), [ Prev,Priorpos,Priorneg, All] as Set);


		Database distributionDB = data.getDatabase(predict_tr, [Prev, Priorpos, Priorneg] as Set, trainPartition);
		Database truthDB = data.getDatabase(trueDataPartition, [possentiment, negsentiment] as Set)
		Database dummy_DB = data.getDatabase(dummy_tr, [possentiment, negsentiment, hiddenpriorpos, hiddenpriorneg] as Set)
		
		
		/* Populate distribution DB. */
		DatabasePopulator dbPop = new DatabasePopulator(distributionDB);
		dbPop.populateFromDB(dummy_DB, possentiment);
		dbPop.populateFromDB(dummy_DB, negsentiment);
		
		/*
		 * Populate distribution DB with all possible interactions
		 */
		dbPop.populateFromDB(dummy_DB, hiddenpriorpos);
		dbPop.populateFromDB(dummy_DB, hiddenpriorneg);
		
		DualEM weightLearning = new DualEM(m, distributionDB, truthDB, config);
		weightLearning.learn();
		weightLearning.close();
		
		println "newly learned model"
		println m
		

		Database testDB = data.getDatabase(predict_te, [Prev, Priorpos, Priorneg, All] as Set, testDataPartition);
		Database trueTestDB = data.getDatabase(trueTestDataPartition, [possentiment, negsentiment] as Set)
		Database dummy_testDB = 
		data.getDatabase(dummy_test, [hiddenpriorpos, hiddenpriorneg, possentiment, negsentiment] as Set)
		
		DatabasePopulator test_populator = new DatabasePopulator(testDB);
		test_populator.populateFromDB(dummy_testDB, possentiment);
		test_populator.populateFromDB(dummy_testDB, negsentiment);
		test_populator.populateFromDB(dummy_testDB, hiddenpriorpos);
		test_populator.populateFromDB(dummy_testDB, hiddenpriorneg);
		
		
		MPEInference mpe = new MPEInference(m, testDB, config)
		FullInferenceResult result = mpe.mpeInference();
		int j
		List<Double> thresholdList = [0.5,0.45,0.4]
//		
//		
//		
//		
//		/*
//		 * Setting the predicates possentiment and negsentiment to an initial value for all groundings
//		 */
//		
//		ResultList allGroundings = trainDB.executeQuery(Queries.getQueryForAllAtoms(all))
//		println "groundings for all"+ allGroundings.size();
//		int j
//		for (j = 0; j < allGroundings.size(); j++) {
//			GroundTerm [] grounding = allGroundings.get(j)
//			RandomVariableAtom atom1 = trainDB.getAtom(possentiment, grounding);
//			RandomVariableAtom atom2 = trainDB.getAtom(negsentiment, grounding);
//			RandomVariableAtom atom3 = trainDB.getAtom(hiddenpriorpos, grounding);
//			RandomVariableAtom atom4 = trainDB.getAtom(hiddenpriorneg, grounding);
//			atom1.setValue(0.0);
//			atom2.setValue(0.0);
//			atom1.commitToDB();
//			atom2.commitToDB();
//			atom3.setValue(0.0);
//			atom3.commitToDB();
//			atom4.setValue(0.0);
//			atom4.commitToDB();
//		}
//
//		MPEInference inferenceApp = new MPEInference(m,trainDB, config)
//		//LazyMPEInference inferenceApp = new LazyMPEInference(m, trainDB, config);
//		inferenceApp.mpeInference();
//		inferenceApp.close();
//		println "trudatapartition : "+trueDataPartition.get(cvSet)
//		Database trueDataDB = data.getDatabase(trueDataPartition.get(cvSet), [possentiment,negsentiment] as Set);
////		MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(m, trainDB, trueDataDB, config);
//		//MaxMargin weightLearning = new MaxMargin(m, trainDB, trueDataDB, config);
//
//		//LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(m, db, trueDataDB, config);
//		//MaxPseudoLikelihood weightLearning = new MaxPseudoLikelihood(m, trainDB, trueDataDB, config);
//		weightLearning.learn();
//		weightLearning.close();
//		/*
//		 * Newly learned weights
//		 */
//		/*
//		 */
//
//		/*Test database setup*/
//
//		Database testDB = data.getDatabase(testDataPartition.get(cvSet),
//				[ Contrast, Noncontrast,Prev, Tglpos, Tglneg, Priorpos, Priorneg,Unigrampos, 
//					Unigramneg, Nrclexiconneg,Nrclexiconpos,
//					Subjectivityneg,Subjectivitypos, All] as Set);
//
//		ResultList groundings = testDB.executeQuery(Queries.getQueryForAllAtoms(all))
//		print groundings.size();
//		for (j = 0; j < groundings.size(); j++) {
//			GroundTerm [] grounding = groundings.get(j)
//			RandomVariableAtom atom1 = testDB.getAtom(possentiment, grounding);
//			RandomVariableAtom atom2 = testDB.getAtom(negsentiment, grounding);
//			atom1.setValue(0.0);
//			atom2.setValue(0.0);
//			atom1.commitToDB();
//			atom2.commitToDB();
//		}
//		inferenceApp = new MPEInference(m, testDB,config)
//		inferenceApp.mpeInference();
//		inferenceApp.close();
//
//
//
//
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
//		Database trueTestDB = data.getDatabase(trueTestDataPartition, [possentiment, negsentiment] as Set);



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
			file4.append(testSet +"\t"+ "possentiment" +"\t"+ score[0] +"\t"+ score[1]+"\t"+score[2]+"\n")
			file4.append(testSet +"\t"+ "negsentiment" +"\t"+ score2[0] +"\t"+ score2[1]+"\t"+score2[2]+"\n")
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No evaluation data! Terminating!");
		}

		Set<GroundAtom> groundings3 = Queries.getAllAtoms(trueTestDB, possentiment)
		int totalPosTestExamples3 = groundings3.size()

		groundings3 = Queries.getAllAtoms(trueTestDB, negsentiment)
		int totalNegTestExamples3 = groundings3.size()

		Set<GroundAtom> groundings2 = Queries.getAllAtoms(truthDB, possentiment)
		int totalPosTrainExamples = groundings2.size()

		groundings2 = Queries.getAllAtoms(truthDB, negsentiment)
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
			file3.append(cvSet+"\t"+"pos"+"\t"+threshold+"\t"+accuracy+"\t"+f1+"\t"+p+"\t"+r+"\n")
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
			file3.append(cvSet+"\t"+"neg"+"\t"+threshold+"\t"+accuracy+"\t"+f1+"\t"+p+"\t"+r+"\n")
		}
		/*all_tn = negcomparator.tn+ poscomparator.tn;
		 all_tp = negcomparator.tp+ poscomparator.tp;
		 all_fp = negcomparator.fp+ poscomparator.fp;
		 all_fn = negcomparator.fn+ poscomparator.fn;
		 all_accuracy = (all_tn+all_tp)/(all_tn+all_tp+all_fp+all_fn)
		 println "Overall accuracy = " +all_accuracy*/


		testDB.close()
		trueTestDB.close();
		dummy_testDB.close();
		distributionDB.close();
		truthDB.close();
		dummy_DB.close();
	}



}

