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
 */
/*
m.add rule : (prev(A,B) & possentiment(B) ) >> possentiment(A), weight :1
m.add rule : (prev(A,B) & negsentiment(B) ) >> negsentiment(A), weight :1
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

def dir1 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold1'+java.io.File.separator;
def dir2 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold2'+java.io.File.separator;
def dir3 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold3'+java.io.File.separator;
def dir4 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold4'+java.io.File.separator;
def dir5 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold5'+java.io.File.separator;
def dir6 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold6'+java.io.File.separator;
def dir7 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold7'+java.io.File.separator;
def dir8 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold8'+java.io.File.separator;
def dir9 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold9'+java.io.File.separator;
def dir10 = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator+'fold10'+java.io.File.separator;
	
/*
 * Constraints on predicates
 *//*
m.add PredicateConstraint.PartialFunctional , on : possentiment
m.add PredicateConstraint.PartialFunctional, on :negsentiment
//m.add PredicateConstraint.PartialInverseFunctional , on : samePerson
//m.add PredicateConstraint.Symmetric, on : samePerson

/*
 * loading the predicates from the data files
 */
def trainPartition = new Partition(0);
Partition trueDataPartition = new Partition(1);
Partition testData = new Partition(2);
Partition trueTestPos = new Partition(3);
Partition trueTestNeg = new Partition(4);

/*
def insert = data.getInserter(contrast, trainPartition);
InserterUtils.loadDelimitedData(insert, dir1+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir2+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir3+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir4+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir5+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir6+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir7+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir8+"contrast_ids.csv");
InserterUtils.loadDelimitedData(insert, dir9+"contrast_ids.csv");
*/

def insert = data.getInserter(prev, trainPartition)
InserterUtils.loadDelimitedData(insert, dir10+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir2+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir3+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir4+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir5+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir6+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir9+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir8+"all_prev.csv");
InserterUtils.loadDelimitedData(insert, dir1+"all_prev.csv");


insert = data.getInserter(priorpos, trainPartition)
InserterUtils.loadDelimitedData(insert, dir10+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir2+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir3+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir4+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir5+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir6+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir9+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir8+"positive.csv");
InserterUtils.loadDelimitedData(insert, dir1+"positive.csv");


insert = data.getInserter(priorneg, trainPartition)
InserterUtils.loadDelimitedData(insert, dir10+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir2+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir3+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir4+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir5+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir6+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir9+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir8+"negative.csv");
InserterUtils.loadDelimitedData(insert, dir1+"negative.csv");

insert = data.getInserter(all, trainPartition)
InserterUtils.loadDelimitedData(insert, dir10+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir2+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir3+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir4+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir5+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir6+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir9+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir8+"allID.csv");
InserterUtils.loadDelimitedData(insert, dir1+"allID.csv");

/*
 * Inference
 */

//Database trainDb = data.getDatabase(trainPartition, [Contrast, Prev,Priorpos, Priorneg, All] as Set);

// Without contrast
Database trainDb = data.getDatabase(trainPartition, [ Prev,Priorpos, Priorneg, All] as Set);
/*
 * Setting the predicates possentiment and negsentiment to an initial value for all groundings
 */

ResultList allGroundings = trainDb.executeQuery(Queries.getQueryForAllAtoms(all))
print allGroundings.size();
for (int i = 0; i < allGroundings.size(); i++) {
	GroundTerm [] grounding = allGroundings.get(i)
	RandomVariableAtom atom1 = trainDb.getAtom(possentiment, grounding);
	RandomVariableAtom atom2 = trainDb.getAtom(negsentiment, grounding);
	atom1.setValue(0.0);
	atom2.setValue(0.0);
	atom1.commitToDB();
	atom2.commitToDB();
}

/*MPEInference mpe = new MPEInference(m, trainDb,config)
FullInferenceResult result = mpe.mpeInference()
System.out.println("Objective: " + result.getTotalWeightedIncompatibility())
*/
MPEInference inferenceApp = new MPEInference(m,trainDb, config)
//LazyMPEInference inferenceApp = new LazyMPEInference(m, trainDb, config);
inferenceApp.mpeInference();
inferenceApp.close();

/*
Integer count = 0
println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(trainDb, possentiment)){
	println atom.toString() + "\t" + atom.getValue();
	count = count+1;}
println count

count = 0
println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(trainDb, negsentiment))
{
	println atom.toString() + "\t" + atom.getValue();
	count = count+1;
}
println count
	*/



/*
 * Weight Learning
 */
insert = data.getInserter(possentiment, trueDataPartition)
InserterUtils.loadDelimitedData(insert, dir10+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir2+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir3+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir4+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir5+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir6+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir9+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir8+"truepos.csv");
InserterUtils.loadDelimitedData(insert, dir1+"truepos.csv");


insert = data.getInserter(negsentiment, trueDataPartition)
InserterUtils.loadDelimitedData(insert, dir10+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir2+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir3+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir4+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir5+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir6+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir9+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir8+"trueneg.csv");
InserterUtils.loadDelimitedData(insert, dir1+"trueneg.csv");


	
Database trueDataDB = data.getDatabase(trueDataPartition, [possentiment,negsentiment] as Set);
MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(m, trainDb, trueDataDB, config);
//LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(m, db, trueDataDB, config);
weightLearning.learn();
weightLearning.close();
/*
 * Newly learned weights
 */

println "Learned model:"
println m

/*
 * Test data
 */

/*
insert = data.getInserter(contrast, testData);
InserterUtils.loadDelimitedData(insert, dir1+"contrast_ids.csv");
*/
insert = data.getInserter(prev, testData)
InserterUtils.loadDelimitedData(insert, dir7+"all_prev.csv");


insert = data.getInserter(priorpos, testData)
InserterUtils.loadDelimitedData(insert, dir7+"positive.csv");


insert = data.getInserter(priorneg, testData)
InserterUtils.loadDelimitedData(insert, dir7+"negative.csv");

insert = data.getInserter(all, testData)
InserterUtils.loadDelimitedData(insert, dir7+"allID.csv");



//Database testDB = data.getDatabase(testData, [Prev, Contrast, Priorpos,Priorneg, All] as Set);

/*
 * without contrast
 */
Database testDB = data.getDatabase(testData, [Prev, Priorpos, Priorneg, All] as Set);


ResultList groundings = testDB.executeQuery(Queries.getQueryForAllAtoms(all))
print groundings.size();
for (int i = 0; i < groundings.size(); i++) {
	GroundTerm [] grounding = groundings.get(i)
	RandomVariableAtom atom1 = testDB.getAtom(possentiment, grounding);
	RandomVariableAtom atom2 = testDB.getAtom(negsentiment, grounding);
	atom1.setValue(0.0);
	atom2.setValue(0.0);
	atom1.commitToDB();
	atom2.commitToDB();
}
/*
 * Try and run non-lazy, MPE inference.
 */
//inferenceApp = new LazyMPEInference(m, testDB, config);
inferenceApp = new MPEInference(m, testDB,config)
result = inferenceApp.mpeInference();
inferenceApp.close();

//inferenceApp = new 


println "test results";

count = 0
println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(testDB, possentiment)){
	println atom.toString() + "\t" + atom.getValue();
	if(atom.getValue()>=0.5){
	file1.append(atom.toString().substring(atom.toString().indexOf('(')+1 ,atom.toString().indexOf(')') )+"\n"	)// + "\t" + atom.getValue()+"\n");
	}
	count = count+1;
	}
println count

count = 0
println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(testDB, negsentiment))
{
	println atom.toString() + "\t" + atom.getValue();
	if(atom.getValue()>=0.5){
	file2.append(atom.toString().substring(atom.toString().indexOf('(')+1 ,atom.toString().indexOf(')') )+"\n")// + "\t" + atom.getValue()+"\n");
	}
	count = count + 1
}
println count



insert = data.getInserter(possentiment, trueTestPos)
InserterUtils.loadDelimitedData(insert, dir7+"truepos.csv");

insert = data.getInserter(negsentiment, trueTestNeg)
InserterUtils.loadDelimitedData(insert, dir7+"trueneg.csv");

Database trueTestPosDB = data.getDatabase(trueTestPos, [possentiment] as Set);
Database trueTestNegDB = data.getDatabase(trueTestNeg, [negsentiment] as Set);

/*
int count = 0
println "Truth values \n Positive sentiment"
for (GroundAtom atom : Queries.getAllAtoms(trueTestPosDB, possentiment)){
	println atom.toString() + "\t" + atom.getValue();
	
	count = count+1;
	}
println count
count = 0
println "Truth values \n Negative"
for (GroundAtom atom : Queries.getAllAtoms(trueTestNegDB, negsentiment)){
	println atom.toString() + "\t" + atom.getValue();
	count = count+1;
}
println count

*/

/*
 * True database - printing number of elements/atoms
 */
Set<GroundAtom> groundings1 = Queries.getAllAtoms(trueTestPosDB, possentiment)
int totalTestExamples = groundings1.size()
println totalTestExamples

groundings1 = Queries.getAllAtoms(trueTestNegDB, negsentiment)
totalTestExamples = groundings1.size()
println totalTestExamples

/*
 * Test database - printing number of elements
 */

println "TestDb"
println "possentiment"
groundings1 = Queries.getAllAtoms(testDB, possentiment)
totalTestExamples = groundings1.size()
println totalTestExamples
println "negsentiment"
groundings1 = Queries.getAllAtoms(testDB, negsentiment)
totalTestExamples = groundings1.size()
println totalTestExamples



def comparator = new SimpleRankingComparator(testDB)
comparator.setBaseline(trueTestPosDB)

// Choosing what metrics to report
def metrics = [ RankingScore.AUPRC, RankingScore.NegAUPRC,  RankingScore.AreaROC]
double [] score = new double[metrics.size()]
double [] score2 = new double[metrics.size()]
try {
	for (int i = 0; i < metrics.size(); i++) {
			comparator.setRankingScore(metrics.get(i))
			score[i] = comparator.compare(possentiment)
	}
	System.out.println("\nArea under positive-class PR curve: " + score[0])
	System.out.println("Area under negetive-class PR curve: " + score[1])
	System.out.println("Area under ROC curve: " + score[2])

}
catch (ArrayIndexOutOfBoundsException e) {
		System.out.println("No evaluation data! Terminating!");
	}

comparator.setBaseline(trueTestNegDB)
// Choosing what metrics to report

try {
	for (int i = 0; i < metrics.size(); i++) {
			comparator.setRankingScore(metrics.get(i))
			score2[i] = comparator.compare(negsentiment)
	}
	System.out.println("\nArea under positive-class PR curve: " + score2[0])
	System.out.println("Area under negetive-class PR curve: " + score2[1])
	System.out.println("Area under ROC curve: " + score2[2])
}
catch (ArrayIndexOutOfBoundsException e) {
	System.out.println("No evaluation data! Terminating!");
}


/*
 * Accuracy
 */


comparator = new DiscretePredictionComparator(testDB)
comparator.setBaseline(trueTestPosDB)
comparator.setResultFilter(new MaxValueFilter(possentiment, 1))
println "With threshold 0.005"
comparator.setThreshold(0.005) // treat best value as true as long as it is nonzero

groundings1 = Queries.getAllAtoms(trueTestPosDB, possentiment)
 totalTestExamples = groundings1.size()
println "printing totalTestExamples:Possentiment"+totalTestExamples
DiscretePredictionStatistics stats = comparator.compare(possentiment, totalTestExamples)
System.out.println("Accuracy: " + stats.getAccuracy())
System.out.println("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE))

println "With threshold 0.5"
comparator.setThreshold(0.5) // treat best value as true as long as it is nonzero

groundings1 = Queries.getAllAtoms(trueTestPosDB, possentiment)
 totalTestExamples = groundings1.size()
println "printing totalTestExamples:Possentiment"+totalTestExamples
stats = comparator.compare(possentiment, totalTestExamples)
System.out.println("Accuracy: " + stats.getAccuracy())
System.out.println("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE))



comparator = new DiscretePredictionComparator(testDB)
comparator.setBaseline(trueTestNegDB)
comparator.setResultFilter(new MaxValueFilter(negsentiment, 1))
println "negsentiment with threshold 0.5"
comparator.setThreshold(0.5) // treat best value as true as long as it is nonzero

 groundings1 = Queries.getAllAtoms(trueTestNegDB, negsentiment)
totalTestExamples = groundings1.size()
println "printing totalTestExamples: Negsentiment"+totalTestExamples
stats = comparator.compare(negsentiment, totalTestExamples)
System.out.println("Accuracy: " + stats.getAccuracy())
System.out.println("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE))

println "negsentiment with threshold 0.005"
comparator.setThreshold(0.005) // treat best value as true as long as it is nonzero

 groundings1 = Queries.getAllAtoms(trueTestNegDB, negsentiment)
totalTestExamples = groundings1.size()
println "printing totalTestExamples: Negsentiment"+totalTestExamples
stats = comparator.compare(negsentiment, totalTestExamples)
System.out.println("Accuracy: " + stats.getAccuracy())
System.out.println("F1: " + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Precision: " + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE))
System.out.println("Recall: " + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE))



/* We close the Databases to flush writes */
trainDb.close();
trueDataDB.close();
testDB.close();
trueTestNegDB.close();
trueTestPosDB.close();
