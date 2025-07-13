package theServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.util.HashMap;

import java.util.Map;

import theHandlers.*;
import theData.*;
import analysisFactory.*;

public class Server {
    // The server with the analyses runs and gets requests with parameters from your program 
	// creates the appropriate analysis by examining the URL parameters 
	// (see the call to the analysis factory) and then calls the appropriate analysis 
	// by calling its perform method
	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		System.out.println("server started at ");
		server.createContext("/WBAnalysis", new WBServerHandler());
		server.createContext("/UNAnalysis", new UNServerHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	static class WBServerHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			
			System.out.println("WB Handler is called ");
			Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
			String param1 = params.get("p1");
			String param2 = params.get("p2");
			String param3 = params.get("p3");
			BusinessDataObject theBusinessDataObject = new BusinessDataObject(param1, param2, param3);
			AnalysisFactory aFactory = new AnalysisFactory();
			Analysis service = aFactory.create(param1);
			
			
			String response = service.perform(theBusinessDataObject);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	

		public Map<String, String> queryToMap(String query) {
			if (query == null) {
				return null;
			}
			Map<String, String> result = new HashMap<>();
			for (String param : query.split("&")) {
				String[] entry = param.split("=");
				if (entry.length > 1) {
					result.put(entry[0], entry[1]);
				} else {
					result.put(entry[0], "");
				}
			}
			return result;
		}
	}
	
	
	static class UNServerHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			
			Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
			String param1 = params.get("p1");
			String param2 = params.get("p2");
			String param3 = params.get("p3");
			BusinessDataObject theBusinessDataObject = new BusinessDataObject(param1, param2, param3);
			AnalysisFactory aFactory = new AnalysisFactory();
			Analysis service = aFactory.create(param1);
			
			String response = service.perform(theBusinessDataObject);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	

		public Map<String, String> queryToMap(String query) {
			if (query == null) {
				return null;
			}
			Map<String, String> result = new HashMap<>();
			for (String param : query.split("&")) {
				String[] entry = param.split("=");
				if (entry.length > 1) {
					result.put(entry[0], entry[1]);
				} else {
					result.put(entry[0], "");
				}
			}
			return result;
		}
	}
}
