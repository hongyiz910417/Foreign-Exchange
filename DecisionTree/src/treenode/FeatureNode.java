package treenode;

/**
 * feature node of the tree
 * @author Hongyi Zhang
 *
 */
public class FeatureNode extends TreeNode {
	private double splitPoint;
	
	private int colIndex;
	
	public FeatureNode(TreeNode left, TreeNode right, double splitPoint, int colIndex){
		this.splitPoint = splitPoint;
		this.colIndex = colIndex;
		this.left = left;
		this.right = right;
	}

	public int getColIndex() {
		return colIndex;
	}

	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}

	public double getSplitPoint() {
		return splitPoint;
	}

	public void setSplitPoint(double splitPoint) {
		this.splitPoint = splitPoint;
	}

	@Override
	public String toString() {
		String s = "FeatureNode: ";
		s += "split point: " + splitPoint;
		s += " colIndex: " + colIndex;
		return s;
	}
}
