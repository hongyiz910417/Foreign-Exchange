package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
}
