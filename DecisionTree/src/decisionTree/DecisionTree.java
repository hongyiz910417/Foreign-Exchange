package decisionTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.datastax.driver.core.Session;

import Utils.Util;
import constants.Params;
import measure.Measure;
import measure.MeasureFactory;
import measure.MeasureType;
import treenode.FeatureNode;
import treenode.LabelNode;
import treenode.TreeNode;


/**
 * 
 * @author Hongyi Zhang (hongyiz)
 * the Decision Tree class
 */
public class DecisionTree {
	private TreeNode root;
	private static final int DIVIDE_COUNT = 10;
	
	public void buildTree(List<String> lines, int[] features){
		Measure measure = MeasureFactory.getMeasure(MeasureType.PERCENTANGE_MEASURE);
		Queue<List<String>> lineQueue = new LinkedList<List<String>>();
		Queue<TreeNode> nodeQueue = new LinkedList<TreeNode>();
		root = new FeatureNode(null, null, 0.0, features[0]);
		root.setLevel(0);
		nodeQueue.offer(root);
		lineQueue.offer(lines);
		/*
		 * do a BFS, and for each of the node, if the node is a feature node, 
		 * split it in the split point where measure.measureSplit has the largest value
		 */
		while(!nodeQueue.isEmpty()){
			TreeNode node = nodeQueue.poll();
			List<String> nodeLines = lineQueue.poll();
			FeatureNode featureNode = (FeatureNode)node;
			int colIndex = featureNode.getColIndex();
			int nodelevel = featureNode.getLevel();
			double[] maxMin = getMaxMin(nodeLines, colIndex);
			double delta = (maxMin[0] - maxMin[1]) / DIVIDE_COUNT;
			double maxPerformance = 0;
			double bestPoint = 0;
			/*
			 * for each split, try DIVIDE_COUNT -2 different values, 
			 * and select the point where performance is highest.
			 */
			for(int i = 2; i < DIVIDE_COUNT - 1; i++){
				double splitPoint = maxMin[1] + delta * i;
				double performance = measure.measureSplit(splitPoint, nodeLines
						, colIndex);
				if(performance > maxPerformance){
					maxPerformance = performance;
					bestPoint = splitPoint;
				}
			}
			/*
			 * split the node on split point
			 */
			featureNode.setSplitPoint(bestPoint);
			List[] lists = split(bestPoint, nodeLines, colIndex);
			if(nodelevel == features.length - 1){
				LabelNode left = new LabelNode(getLabel(lists[0]));
				LabelNode right = new LabelNode(getLabel(lists[1]));
				node.setLeft(left);
				node.setRight(right);
			}
			else{
				int nextLevel = nodelevel + 1;
				FeatureNode left = new FeatureNode(null, null, 0.0, features[nextLevel]);
				left.setLevel(nextLevel);
				FeatureNode right = new FeatureNode(null, null, 0.0, features[nextLevel]);
				right.setLevel(nextLevel);
				node.setLeft(left);
				node.setRight(right);
				lineQueue.offer(lists[0]);
				lineQueue.offer(lists[1]);
				nodeQueue.offer(left);
				nodeQueue.offer(right);
			}
		}
	}
	
	/*
	 * search the prediction for an instance
	 */
	public boolean searchInTree(String line){
		String[] cols = line.split(",");
		TreeNode node = root;
		for(int i = 2; i < Params.LABEL_INDEX; i++){
			double val = Double.parseDouble(cols[i]);
			FeatureNode featureNode = (FeatureNode)node;
			if(val < featureNode.getSplitPoint()){
				node = node.getLeft();
			}
			else{
				node = node.getRight();
			}
		}
		LabelNode labelNode = (LabelNode)node;
		return labelNode.isVal();
	}
	
	public String toString(){
		return getString(root, 0);
	}
	
	private String getString(TreeNode node, int level){
		String s = "";
		for(int i = 0; i < level; i++){
			s += "\t";
		}
		if(node instanceof LabelNode){
			return s + node.toString();
		}
		else{
			s += node.toString() + "\n";
			s += getString(node.getLeft(), level + 1) + "\n";
			s += getString(node.getRight(), level + 1);
			return s;
		}
	}
	
	/**
	 * split the instances into two sets based on the splitPoint
	 * @param splitPoint
	 * @param lines
	 * @param colIndex
	 * @return
	 */
	private List[] split(double splitPoint, List<String> lines, int colIndex){
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		for(String line : lines) {
			String[] cols = line.split(",");
		    double val = Double.parseDouble(cols[colIndex]);
		    if(val < splitPoint){
		    	list1.add(line);
		    }
		    else{
		    	list2.add(line);
		    }
		}
		List[] lists = new List[2];
		lists[0] = list1;
		lists[1] = list2;
		return lists;
	}
	
	/*
	 * take the majority as the node's label
	 */
	private boolean getLabel(List<String> lines){
		int trueCount = 0;
		for(String line : lines) {
			String[] cols = line.split(",");
			boolean label = Boolean.parseBoolean(cols[Params.LABEL_INDEX]);
			if(label){
				trueCount++;
			}
		}
		boolean flag = trueCount < lines.size() / 2;
		return flag;
	}
	
	/*
	 * get the range (max and min) for the data set
	 */
	private double[] getMaxMin(List<String> lines, int colIndex){
		double[] results = new double[2]; // 0: max, 1: min
		results[0] = 0;
		results[1] = Integer.MAX_VALUE;
	    for(String line : lines) {
	       String[] cols = line.split(",");
	       if(cols.length <= 4){
	    	   System.out.println(line);
	       }
	       double val = Double.parseDouble(cols[colIndex]);
	       results[0] = Math.max(results[0], val);
	       results[1] = Math.min(results[1], val);
	    }
		return results;
	}
}
