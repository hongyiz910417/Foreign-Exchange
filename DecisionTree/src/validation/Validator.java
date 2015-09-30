package validation;

import java.util.List;

import constants.Params;
import Utils.Util;
import decisionTree.DecisionTree;

/**
 * calculate the correctness of the decision tree
 * @author Hongyi Zhang(hongyiz)
 *
 */
public class Validator {
	public double validate(String filename, DecisionTree dt){
		List<String> lines = Util.readfile(filename);
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
}
