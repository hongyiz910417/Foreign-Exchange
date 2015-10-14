package treenode;

public abstract class TreeNode {
	protected TreeNode left;
	protected TreeNode right;
	protected int level;
	
	public TreeNode getLeft() {
		return left;
	}
	public void setLeft(TreeNode left) {
		this.left = left;
	}
	public TreeNode getRight() {
		return right;
	}
	public void setRight(TreeNode right) {
		this.right = right;
	}
	public void setLevel(int level){
		this.level = level;
	}
	public int getLevel(){
		return level;
	}
	
	public abstract String toString();
}
