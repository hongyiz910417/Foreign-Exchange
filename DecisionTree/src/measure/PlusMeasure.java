package measure;

import java.util.List;

import constants.Params;

public class PlusMeasure implements Measure {

	@Override
	public double measureSplit(double splitPoint, List<String> lines, int col) {
	    int trueS1Count = 0, trueS2Count = 0, falseS1Count = 0, falseS2Count = 0;
	    for(String line : lines) {
	       String[] cols = line.split(",");
	       if(cols[Params.LABEL_INDEX].equals(Params.NONE_STR)){
	    	   continue;
	       }
	       double colVal = Double.parseDouble(cols[col]);
	       boolean labelVal = Boolean.parseBoolean(cols[Params.LABEL_INDEX]);
	       if(colVal < splitPoint && labelVal){
	    	   trueS1Count++;
	       }
	       else if(colVal < splitPoint && !labelVal){
	    	   falseS1Count++;
	       }
	       else if(labelVal){
	    	   trueS2Count++;
	       }
	       else{
	    	   falseS2Count++;
	       }
	    }
	    int count1 = 0, count2 = 0;
	    if(trueS1Count < falseS1Count){
	    	count1 = falseS1Count;
	    	count2 = trueS2Count;
	    }
	    else{
	    	count1 = trueS1Count;
	    	count2 = falseS2Count;
	    }
	    return count1 + count2;
	}

}
