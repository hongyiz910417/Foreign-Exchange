package measure;

import java.util.List;

import constants.Params;

/**
 * Measure that I finally chose to use
 * @author Hongyi Zhang (hongyiz)
 *
 */
public class PercentageMeasure implements Measure {

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
	    int s1Total = trueS1Count + falseS1Count;
	    int s2Total = trueS2Count + falseS2Count;
	    if(trueS1Count < falseS1Count){
	    	count1 = falseS1Count;
	    	count2 = trueS2Count;
	    }
	    else{
	    	count1 = trueS1Count;
	    	count2 = falseS2Count;
	    }
	    return (double)count1/(double)s1Total + (double)count2/(double)s2Total;
	}

}
