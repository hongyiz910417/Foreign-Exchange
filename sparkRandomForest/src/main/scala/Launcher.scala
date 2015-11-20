
/**
 * @author hongyiz
 */

import java.util.UUID

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd._
import org.apache.spark.rdd.RDD

class Launcher {
   //function for parsing a boolean label into double
    def parseLabel(label: Boolean) : Double = {
      if(label) 1.0
      else -1.0
    }
    
    def main (args: Array[String]) {
    val conf = new SparkConf().set("spark.cassandra.connection.host", "localhost")
      .setAppName("RandomForest")
    val sc = new SparkContext(conf)
    val trainingTable = sc.cassandraTable("foreignStock", "trainingData")

    val trainingData  = trainingTable.map{
      row => {
        LabeledPoint(new Vector(row.getDouble("bidMin"), row.getDouble("bidMax")
            , row.getDouble("bidAvg"), row.getDouble("spreadAvg")), parseLabel(row.getBool("label")))
      }}

    val testTable = sc.cassandraTable("foreignStock", "testData")
    val testData = testTable.map{
      row => {
        LabeledPoint(new Vector(row.getDouble("bidMin"), row.getDouble("bidMax")
            , row.getDouble("bidAvg"), row.getDouble("spreadAvg")), parseLabel(row.getBool("label")))
      }}

    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]() //because all the features are numeric
    val numTrees = 6 // will be tuned with more experiements
    val featureSubsetStrategy = "auto"
    val impurity = "gini"
    val maxDepth = 4 //because there's only 4 features
    val maxBins = 64

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    //test on the testData, if the prediction value and the label value 
    //are both below 0 or above 0, which means that the production >0
    // it means that the prediction is right, we store a true.
    val results = testData.map { line =>
      (model.predict(line.features) * line.label > 0)
    }

    val correctness = results.filter(r => r == true).count.toDouble / results.count().toDouble
    println("Correctness = " + correctness)
    println("Model:\n" + model.toDebugString)

    val collection = sc.parallelize(Seq((UUID.randomUUID.toString(), correctness)))

    // save performance metrics to database
    collection.saveToCassandra("foreignStock","performance",SomeColumns("id","correctness"))
    println("performance saved")
  }
}