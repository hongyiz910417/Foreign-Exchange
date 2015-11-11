package mapreduce;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

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

        String IP = args[0];
        String KEYSPACE = args[1];
        String TABLE = args[2];
        int N = Integer.parseInt(args[3]);
        String output = args[4];

        Database db = new Database(IP,KEYSPACE);

        String url = "inputFile";
        convertDBtoFile(db, TABLE, url, N);

        String query = "select count(*) from " + TABLE + " ;";
        ResultSet result = db.executeWithResult(query);
        Row r = result.one();
        int count = r.getInt(0);
        int lineNum = count/N;

        conf.setInt("treeNumber", N);

        int featureNum = (int) Math.ceil(Math.sqrt((double)N));
        conf.set("featureSet", storeFeatureSet(N, featureNum));

        Job job = new Job(conf,url);

        NLineInputFormat.setNumLinesPerSplit(job,lineNum);

        job.setJarByClass(MapReduce.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(NLineInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileOutputFormat.setOutputPath(job, new Path(output));

        job.waitForCompletion(true);
    }

    public String storeFeatureSet(int N, int featureNum) {
        RandomForest rf = new RandomForest(N, "");
        HashSet<ArrayList<Integer>> featureSet = new HashSet<>();
        StringBuffer sb = new StringBuffer();

        ArrayList<Integer> list ;
        for(int i=0;i<N;){
            list = rf.getFeatureSet(featureSet, featureNum);
            if (list !=null){
                i++;
                for(int j : list) {
                    sb.append(j);
                    sb.append(",");
                }
                sb.append("\n");
            }
        }
      return sb.toString();
    }

    public void convertDBtoFile(Database db, String tablename, String url, int N) throws IOException {
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(url), "utf-8"));

        ResultSet results = db.selectAllFromTable(tablename);

        StringBuffer sb ;

        for(Row row : results){
            sb = new StringBuffer();
            for(int i=1;i<=7;i++){
                sb.append(row.getInt(i));
                sb.append("\t");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("\n");
            wr.write(sb.toString());
        }

        wr.close();
    }
}
