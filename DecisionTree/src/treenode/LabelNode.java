package treenode;

public class LabelNode extends TreeNode {
	private boolean val;
	
	public LabelNode(boolean val){
		this.val = val;
	}

	public boolean isVal() {
		return val;
	}

	public void setVal(boolean val) {
		this.val = val;
	}

	@Override
	public String toString() {
		String s = "LabelNode: ";
		s += val;
		return s;
	}
}
