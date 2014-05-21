


/*
 * This file is part of the PSL software.
 * Copyright 2011-2013 University of Maryland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.example;
import edu.umd.cs.psl.application.inference.LazyMPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.random.GroundSliceRandOM;
import edu.umd.cs.psl.application.learning.weight.maxmargin.MaxMargin;
import edu.umd.cs.psl.application.learning.weight.maxmargin.PositiveMinNormProgram;
import edu.umd.cs.psl.config.*
import edu.umd.cs.psl.database.DataStore
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type
import edu.umd.cs.psl.groovy.PSLModel;
import edu.umd.cs.psl.groovy.PredicateConstraint;
import edu.umd.cs.psl.groovy.SetComparison;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.function.ExternalFunction;
import edu.umd.cs.psl.ui.functions.textsimilarity.*
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;

/*
 * Config bundle changed to accept String as UniqueID
 */
ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("fine-grained")

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
m.add predicate: "possentiment", types: [ArgumentType.UniqueID]
m.add predicate: "negsentiment", types: [ArgumentType.UniqueID]


/*
 * Adding rules
 */
m.add rule : (possentiment(A) ) >> ~negsentiment(A), weight :1
m.add rule : (negsentiment(A) ) >> ~possentiment(A), weight :1
m.add rule : (possentiment(A) ) >> possentiment(A), weight :1
m.add rule : (negsentiment(A) ) >> negsentiment(A), weight :1
m.add rule : (prev(A,B) & possentiment(B) & (A ^ B)) >> possentiment(A), weight :15
m.add rule : (prev(A,B) & negsentiment(B) & (A ^ B)) >> negsentiment(A), weight :10
m.add rule : (contrast(A,B) & possentiment(A) & (A ^ B)) >> negsentiment(B)  , weight :8
m.add rule : (contrast(A,B) & negsentiment(B) & (A ^ B)) >> possentiment(B)  , weight :8

/*
 * Printing model
 */
println m;


def partition = new Partition(0);
def insert = data.getInserter(contrast, partition);
def dir = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator;
InserterUtils.loadDelimitedData(insert, dir+"contrast.txt");

insert = data.getInserter(prev, partition)
 dir = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator;
InserterUtils.loadDelimitedData(insert, dir+"previous.txt");


insert = data.getInserter(possentiment, partition)
dir = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator;
InserterUtils.loadDelimitedData(insert, dir+"positive.txt");


insert = data.getInserter(negsentiment, partition)
dir = 'data'+java.io.File.separator+'sentiment'+java.io.File.separator;
InserterUtils.loadDelimitedData(insert, dir+"negative.txt");

/*
 * Inference
 */

Database db = data.getDatabase(partition, [Contrast, Prev] as Set);
LazyMPEInference inferenceApp = new LazyMPEInference(m, db, config);
inferenceApp.mpeInference();
inferenceApp.close();


Integer count = 0
println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(db, possentiment)){
	println atom.toString() + "\t" + atom.getValue();
	count = count+1;}
println count

println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(db, negsentiment))
	println atom.toString() + "\t" + atom.getValue();
	
/*
 * Weight Learning
 */
	

Partition trueDataPartition = new Partition(1);

insert = data.getInserter(possentiment, trueDataPartition)
InserterUtils.loadDelimitedData(insert, dir+"truepos.txt");

insert = data.getInserter(negsentiment, trueDataPartition)
InserterUtils.loadDelimitedData(insert, dir+"trueneg.txt");


	
Database trueDataDB = data.getDatabase(trueDataPartition, [possentiment,negsentiment] as Set);
MaxMargin weightLearning = new MaxMargin(m, db, trueDataDB, config);
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
Partition sn2 = new Partition(2);
insert = data.getInserter(name, sn2);
InserterUtils.loadDelimitedData(insert, dir+"sn2_names.txt");
insert = data.getInserter(knows, sn2);
InserterUtils.loadDelimitedData(insert, dir+"sn2_knows.txt");

Database db2 = data.getDatabase(sn2, [Name, Knows] as Set);
inferenceApp = new LazyMPEInference(m, db2, config);
result = inferenceApp.mpeInference();
inferenceApp.close();

println "Inference results on second social network with learned weights:"
for (GroundAtom atom : Queries.getAllAtoms(db2, SamePerson))
	println atom.toString() + "\t" + atom.getValue();
	
/* We close the Databases to flush writes */
	
	/*
db.close();
trueDataDB.close();
db2.close();
*/
