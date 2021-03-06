package validation;

import java.util.List;

import randomForrest.RandomForest;
import constants.Params;
import decisionTree.DecisionTree;

/**
 * calculate the correctness of the decision tree
 * @author Hongyi Zhang(hongyiz)
 *
 */
public class Validator {
	public static double validateTree(List<String> lines, DecisionTree dt){
		int count = 0;
		for(String line : lines){
			if(line.endsWith(Params.NONE_STR)){
				continue;
			}
			boolean prediction = dt.searchInTree(line);
			boolean fact = Boolean.parseBoolean(line.split(",")[Params.LABEL_INDEX]);
			if(prediction == fact){
				count++;
			}
		}
		return count / (double)(lines.size() - 1);
	}
	
	public static double validateForest(List<String> lines, RandomForest forest){
		int count = 0;
		for(String line : lines){
			if(line.endsWith(Params.NONE_STR)){
				continue;
			}
			boolean prediction = forest.predict(line);
			boolean fact = Boolean.parseBoolean(line.split(",")[Params.LABEL_INDEX]);
			if(prediction == fact){
				count++;
			}
		}
		return count / (double)(lines.size() - 1);
	}
}
