package mapreduce;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import Utils.Util;

import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import randomForrest.RandomForest;
import treenode.TreeNode;
import decisionTree.DecisionTree;

public class MapReducer {
	public class Map extends Mapper<LongWritable, Text, Text, Text> {     
        /*
    	 * all the possible combinations of 2 features
    	 */
    	private int[][] featureCombinations = {{2, 3}, {2, 4}, {2, 5}, {3, 4}, {3, 5}, {4, 5}};

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] lines = value.toString().split("\n");
            List<String> strList = Arrays.asList(lines);
            
            //build decision tree
            DecisionTree dt = new DecisionTree();
            Random random = new Random();
            int[] features = featureCombinations[random.nextInt() / featureCombinations.length];
            dt.buildTree(strList, features);

            // get Json
            Gson gson = new GsonBuilder().create();
            String treeJson = gson.toJson(dt);

            Text text = new Text();
            text.set(treeJson);
            
            Text newKey = new Text();
            newKey.set(UUID.randomUUID().toString());
            context.write(newKey ,text);
        }
    }

    public class Reduce extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            RandomForest forest = new RandomForest();
            Gson gson = new GsonBuilder().create();

            for (Text val : values) {
                DecisionTree tree = gson.fromJson(val.toString(), DecisionTree.class);
                forest.addTree(tree);
            }

            context.write(key, new Text(gson.toJson(forest)));
        }
    }


    public void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        List<String> list = Util.readDB("datadb");

        String file = "inputFile";
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file)));
        for(String row : list){
            wr.write(row + "\n");
        }
        wr.close();
        
        int treeNum = 6;

        Job job = new Job(conf,file);

        job.setJarByClass(MapReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TrainingDataInputFormat.class);

        FileOutputFormat.setOutputPath(job, new Path("output"));

        job.waitForCompletion(true);
    }
}
