package driver;


import java.util.List;

import randomForrest.RandomForest;
import Utils.Util;
import validation.Validator;

public class Driver {

	public static void main(String[] args) {
		List<String> test = Util.readDB("datadb");
		RandomForest randomForest = new RandomForest("datadb", 5);
		double correctness = Validator.validateForest(test, randomForest);
		System.out.println("forest correctness: " + correctness);
		Util.saveMetrics(correctness, "datadb");
	}
}
