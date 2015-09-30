package measure;

public class MeasureFactory {
	public static Measure getMeasure(MeasureType type){
		if(type == MeasureType.PLUS_MEASURE){
			return new PlusMeasure();
		}
		else if(type == MeasureType.PERCENTANGE_MEASURE){
			return new PercentageMeasure();
		}
		else{
			return null;
		}
	}
}
