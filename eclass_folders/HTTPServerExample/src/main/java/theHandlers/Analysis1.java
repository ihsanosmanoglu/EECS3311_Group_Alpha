package theHandlers;

import java.util.*;

import theData.*;

import com.fasterxml.jackson.core.*;
//import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;

public class Analysis1 implements Analysis {

	public String perform(BusinessDataObject data) {
		// Here is a mock-up of an analysis
		// Prepares the URL to the WorldBank (say) by using the fields in the data parameter
		// does the necessary calls to the the World bank, 
		// does the necessary  the computations, and create a result json string
		// Here, it just puts dummy values to an array of hashmaps as a dummy result 
		// and returns the json 
		Result result = new Result();
		ArrayList<HashMap<Integer, Float>> myList = new ArrayList<HashMap<Integer, Float>>();
		HashMap<Integer, Float> series1 = new HashMap<Integer, Float>();
		series1.put(2000,new Float(1.0));
        series1.put(2001,new Float(2.0));
        series1.put(2002,new Float(1.1));
        series1.put(2003,new Float(2.1));
        
		HashMap<Integer, Float> series2 = new HashMap<Integer, Float>();
		series2.put(2000,new Float(8.0));
        series2.put(2001,new Float(9.0));
        series2.put(2002,new Float(8.1));
        series2.put(2003,new Float(9.1));
     
		myList.add(series1);
		myList.add(series2);
		
		result.setTheData(myList);
		
		Gson gson = new Gson();
        String json = gson.toJson(result); 
        System.out.println("Here is the result of the perform method of Analysis1 " + json);
		
		return json;
	}

}
