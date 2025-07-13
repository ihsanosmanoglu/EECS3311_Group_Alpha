package client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class anHttpClient {
	public static void main(String[] args) {
		// Here is a mock-up of your program doing a call to the server containing the analyses
		// Your program prepares the request and calls the remote server with the analyses module
		String urlString = String.format("http://localhost:8000/WBAnalysis/?p1=%s&p2=%s&p3=%s", "Analysis1",
				"SP.POP.TOTL", "2000:2001");

		// api.worldbank.org/v2/country/%s/indicator/SP.POP.TOTL?date=2000:2001&format=json", "can");
		System.out.println(urlString);
		int populationForYear = 0;
		int cummulativePopulation = 0;

		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.connect();
			int responsecode = conn.getResponseCode();
			System.out.println("Response Code " + responsecode);
// IF THE RESPONSE IS 200 OK GET THE LINE WITH THE RESULTS
			if (responsecode == 200) {
				String inline = "";
				Scanner sc = new Scanner(url.openStream());
				while (sc.hasNext()) {
					inline += sc.nextLine();
				}
				System.out.println("-------- Here is the response to the client: " + inline);
				sc.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block e.printStackTrace();
		}

		return;
	}

}
