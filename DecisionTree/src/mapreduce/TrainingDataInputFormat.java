package mapreduce;

import java.io.IOException;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

public class TrainingDataInputFormat extends FileInputFormat<LongWritable, Text> {
	private static String filepath = "inputfile";
	
	@Override
	public RecordReader<LongWritable, Text> createRecordReader(
			InputSplit arg0, TaskAttemptContext arg1)
			throws IOException, InterruptedException {
		arg1.setStatus(arg0.toString());
        return new LineRecordReader();
	}
	
	public List<InputSplit> getSplits(JobContext job) throws IOException {
        List<InputSplit> splits = new ArrayList<InputSplit>();
        for (FileStatus status : listStatus(job)) {
        	splits.addAll(getTrainingSplits(status, job.getConfiguration()));
	    }
	    return splits;
	}
	
	public List<InputSplit> getTrainingSplits(FileStatus status, Configuration conf){
		try{
            Path pt=new Path(filepath);
            FileSystem fs = FileSystem.get(new Configuration());
            BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
            List<String> strList = new ArrayList<String>();
            String line;
            line=br.readLine();
            while (line != null){
            	strList.add(line);
            	line=br.readLine();
            }
            List<InputSplit> training = new ArrayList<InputSplit>();
    		Random random = new Random();
    		for(int i = 0; i < strList.size(); i += 3){
    			int testline = random.nextInt() % 3;
    			for(int j = 0; j < 3; j++){
    				if(testline != j) training.add(new FileSplit(pt, (long)i + (long)j, 1l,
                            new String[] {}));
    			}
    		}
    		return training;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
