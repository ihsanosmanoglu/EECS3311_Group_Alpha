package theHandlers;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

import theData.BusinessDataObject;
import theData.Result;

public class Analysis2 implements Analysis {

	public String perform(BusinessDataObject data) {
		// TODO Auto-generated method stub
		Result result = new Result();
		ArrayList<HashMap<Integer, Float>> myList = new ArrayList<HashMap<Integer, Float>>();
		HashMap<Integer, Float> series1 = new HashMap<Integer, Float>();
		series1.put(2000,new Float(1.0));
        series1.put(2001,new Float(2.0));
        series1.put(2002,new Float(1.1));
        series1.put(2003,new Float(2.1));
        
		HashMap<Integer, Float> series2 = new HashMap<Integer, Float>();
		series2.put(2000,new Float(5.0));
        series2.put(2001,new Float(6.0));
        series2.put(2002,new Float(5.1));
        series2.put(2003,new Float(6.1));
     
		myList.add(series1);
		myList.add(series2);
		
		result.setTheData(myList);
		
		Gson gson = new Gson();
        String json = gson.toJson(result); 
        System.out.println(json);
		
		return json;
	}

}
