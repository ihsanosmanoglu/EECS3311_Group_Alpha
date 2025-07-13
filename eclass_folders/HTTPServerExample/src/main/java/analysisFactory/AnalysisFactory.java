package analysisFactory;

import theHandlers.*;

public class AnalysisFactory {
	public Analysis create(String name) {
		
		Analysis result = null;
		if(name.equals("Analysis1"))
			result =  new Analysis1();
		
		if(name.contentEquals("Analysis2"))
			result = new Analysis2();
		
		if(name.contentEquals("Analysis3"))
			result = new Analysis3();
		
		return result;
		
	}

}
