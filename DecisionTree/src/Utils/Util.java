package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import constants.Params;

public class Util {
	public static List<String> readfile(String filename){
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null){
				if(!line.endsWith(Params.NONE_STR)){
					lines.add(line);
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		return lines;
	}
	
	public static List<String> readDB(String db){
		List<String> lines = new ArrayList<String>();
		Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		Session session = cluster.connect(db);
		ResultSet results = session.execute("SELECT * FROM " + Params.TRAINING_TABLE_NAME);
		for (Row row : results) {
			String line = row.getString("currency") + "," + row.getString("datetime") + "," + 
							row.getString("bidMin") + "," + row.getString("bidMax") + "," + 
							row.getString("bidAvg") + "," + row.getString("spreadAvg") + "," + 
							row.getString("label");
			lines.add(line);
		}
		cluster.close();
		return lines;
	}
	
	public static void saveMetrics(double performance, String db){
		Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		Session session = cluster.connect(db);
		session.execute("INSERT INTO " + Params.METRICS_TABLE_NAME 
				+ " (performance) VALUES (" + performance + ")");
		session.close();
	}
}
