package driver;


import validation.Validator;
import decisionTree.DecisionTree;

public class Driver {

	public static void main(String[] args) {
		DecisionTree dt = new DecisionTree();
		Validator validator = new Validator();
		dt.buildTree("input");
		System.out.println(dt.toString());
		double performance = validator.validate("test", dt);
		System.out.println(performance);
	}

}
