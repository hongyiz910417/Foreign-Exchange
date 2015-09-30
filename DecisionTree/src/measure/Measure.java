package measure;

import java.util.List;

public interface Measure {
	public double measureSplit(double splitPoint, List<String> filename, int col);
}
