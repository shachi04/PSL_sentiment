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
 * The first thing we need to do is initialize a ConfigBundle and a DataStore
 */

/*
 * A ConfigBundle is a set of key-value pairs containing configuration options. One place these
 * can be defined is in psl-example/src/main/resources/psl.properties
 */
ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("tryexample")

/* Uses H2 as a DataStore and stores it in a temp. directory by default */
def defaultPath = System.getProperty("java.io.tmpdir")
String dbpath = config.getString("dbpath", defaultPath + File.separator + "tryexample")
DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)

/*
 * Now we can initialize a PSLModel, which is the core component of PSL.
 * The first constructor argument is the context in which the PSLModel is defined.
 * The second argument is the DataStore we will be using.
 */
PSLModel m = new PSLModel(this, data)

/* 
 * We create three predicates in the model, giving their names and list of argument types
 */

m.add predicate: "text", types: [ArgumentType.UniqueID, ArgumentType.String]
m.add predicate: "containsgreat", types: [ArgumentType.UniqueID]
m.add predicate: "sentiment", types: [ArgumentType.UniqueID]
m.add PredicateConstraint.PartialFunctional , on : sentiment

m.add rule : ( text(I, A) & containsgreat(I))  >> sentiment(I),  weight : 8
m.add rule : ( text(I, A) & ~containsgreat(I))  >> ~sentiment(I),  weight : 5

println m;

/* 
 * We now insert data into our DataStore. All data is stored in a partition.
 * 
 * We can use insertion helpers for a specified predicate. Here we show how one can manually insert data
 * or use the insertion helpers to easily implement custom data loaders.
 */
 
def partition = new Partition(0);
def insert = data.getInserter(text, partition);

def dir1 = 'data'+java.io.File.separator+'textinput'+java.io.File.separator;
InserterUtils.loadDelimitedData(insert, dir1+"textinput.txt");

insert = data.getInserter(containsgreat, partition)
def dir = 'data'+java.io.File.separator+'containsgreat'+java.io.File.separator;
InserterUtils.loadDelimitedData(insert, dir+"containsgreat.txt");

Database db = data.getDatabase(partition, [Text, Containsgreat] as Set);
LazyMPEInference inferenceApp = new LazyMPEInference(m, db, config);
inferenceApp.mpeInference();
inferenceApp.close();


println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(db, Sentiment))
	println atom.toString() + "\t" + atom.getValue();
/*
 * After having loaded the data, we are ready to run some inference and see what kind of
 * alignment our model produces. Note that for now, we are using the predefined weights.
 * 
 * We first open up Partition 0 as a Database from the DataStore. We close the predicates
 * Name and Knows since we want to treat those atoms as observed, and leave the predicate
 * SamePerson open to infer its atoms' values.
 */
 /*
Database db1 = data.getDatabase(partition, [text, containsgreat] as Set);
LazyMPEInference inferenceApp = new LazyMPEInference(m, db1, config);
inferenceApp.mpeInference();
inferenceApp.close();

/*
 * Let's see the results
 */
 /*
println "Inference results with hand-defined weights:"
for (GroundAtom atom : Queries.getAllAtoms(db, SamePerson))
	println atom.toString() + "\t" + atom.getValue();

/* 
 * Next, we want to learn the weights from data. For that, we need to have some evidence
 * data from which we can learn. In our example, that means we need to specify the 'true'
 * alignment, which we now load into a second partition.
 */
 /*
Partition trueDataPartition = new Partition(1);
insert = data.getInserter(samePerson, trueDataPartition)
InserterUtils.loadDelimitedDataTruth(insert, dir + "sn_align.txt");

/* 
 * Now, we can learn the weight, by specifying where the respective data fragments are stored
 * in the database (see above). In addition, we need to specify, which predicate we would like to
 * infer, i.e. learn on, which in our case is 'samePerson'.
 */
 /*
Database trueDataDB = data.getDatabase(trueDataPartition, [samePerson] as Set);
LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(m, db, trueDataDB, config);
weightLearning.learn();
weightLearning.close();

/*
 * Let's have a look at the newly learned weights.
 */
println "Learned model:"
println m

/*
 * Now, we apply the learned model to a different social network alignment dataset. We load the 
 * dataset as before (this time into partition 2) and run inference. Finally we print the results.
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

/**
 * This class implements the ExternalFunction interface so that it can be used
 * as an attribute similarity function within PSL.
 *
 * This simple implementation checks whether two strings are identical, in which case it returns 1.0
 * or different (returning 0.0).
 *
 * The package edu.umd.cs.psl.ui.functions.textsimilarity contains additional and
 * more sophisticated string similarity functions.
 *//*
class MyStringSimilarity implements ExternalFunction {
	
	@Override
	public int getArity() {
		return 2;
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		return [ArgumentType.String, ArgumentType.String].toArray();
	}
	
	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		return args[0].toString().equals(args[1].toString()) ? 1.0 : 0.0;
	}
	
}*/
