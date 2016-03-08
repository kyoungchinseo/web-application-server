package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class HttpRequest {
	
	public HttpRequest(BufferedReader br) {

	}

	public Map<String, String> getHTTPMap(BufferedReader br) {
		// TODO Auto-generated method stub
		String line;
		Map<String, String> http = new HashMap<String, String>();
		try {
			while(!((line = br.readLine()).equals(""))) {
				String[] splitLine = line.split(": ");
				if (splitLine.length == 1) {
					String []tk = line.split(" ");
					http.put("METHOD", tk[0]);
					http.put("url",tk[1]);
				} else if (splitLine.length >= 2){
					http.put(splitLine[0],splitLine[1]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return http;
	}
	
	public boolean isGETMethod(Map<String,String> http) {
		return http.get("METHOD").equals("GET");
	}
	
	public boolean isPOSTMethod(Map<String,String> http) {
		return http.get("METHOD").equals("POST");
	}
	
	public void respond(BufferedReader br, Map<String,String> http, HttpResponse httpResponse) {
		if (isFavicon(http)) {
			return;
		}
		if (isGETMethod(http)) {
			respondGET(http, httpResponse);
		}
		if (isPOSTMethod(http)) {
			respondPOST(br, http,httpResponse);
		}
	}

	private boolean isFavicon(Map<String, String> http) {
		return http.get("url").equals("/favicon.ico");
	}

	private void respondGET(Map<String, String> http, HttpResponse httpResponse) {
		String url = http.get("url");
		
		int index = url.indexOf("?"); 
		String requestPath = "";
		String params = "";
		if (index != -1) { // request from GET method with user data
			requestPath = url.substring(0,index);
			params = url.substring(index+1);
		}
		
		if (index > -1) {
			if (requestPath.equals("/user/create")) {
				Map<String,String> data = new HashMap<String,String>();
				data = HttpRequestUtils.parseQueryString(params);	
				User user = new User(data.get("userId"), data.get("password"),data.get("name"),data.get("email"));
				System.out.println("USER: " + user);
				
				// ADD USER to STATIC RAM MEMORY
				DataBase.addUser(user);
				
				// request
				// httpResponse.response202(url, user.toString()); // for test
				// redirection
				url = "/index.html";
				try {
					httpResponse.response302(url);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}
		
		if (url.equals("/user/list")) {
			String cookie = http.get("Cookie");
			boolean logined = cookie.contains("logined=true");
			System.out.println("LOGIN: " + logined);
			if (logined == true) {
				//url = "/user/list.html";
				//byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
				StringBuilder html = new StringBuilder();
				html.append("<!DOCTYPE html>");
				html.append("<html lang=&quotkr&quot>");
				html.append("<head>");
				html.append("<meta http-equiv=&quotcontent-type&quot content=&quottext/html; charset=UTF-8&quot>");
				html.append("<meta charset=&quotutf-8&quot>");
				html.append("<title>SLiPP Java Web Programming</title>");
				html.append("<meta name=&quotviewport&quot content=&quotwidth=device-width, initial-scale=1, maximum-scale=1&quot>");
				html.append("<link href=&quot../css/bootstrap.min.css&quot rel=&quotstylesheet&quot>");
				html.append("<link href=&quot../css/styles.css&quot rel=&quotstylesheet&quot>");
				html.append("</head>");
				html.append("<body>");
				
				html.append("<table>");
				html.append("<thead>");
				html.append("<tr>");
				html.append("<th>USER ID</th> <th>NAME</th> <th>EMAIL</th>");
				html.append("</tr>");
				html.append("</thead>");
				html.append("<tbody>");
				html.append("</tbody>");
				// table and with user data
				Collection<User> userList = DataBase.findAll();
				Iterator it = userList.iterator();
				while(it.hasNext()) {
					User user = (User)it.next();
					html.append("<tr>");
					html.append("<td>"+user.getUserId()+"</td> <td>"+user.getName()+"</td> <td>"+user.getEmail()+"</td>");
					html.append("</tr>");
				}
	            html.append("</table>");
				
				html.append("</body>");
				html.append("</html>");
				
				
				httpResponse.response202(url, html.toString());
				return;
			} 
			
			if (logined == false) {
				url = "/user/login.html";
				try {
					httpResponse.response302(url);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		} 
		
		if (url.equals("/")) {
			url = "/index.html";
		}
			
		try {
			httpResponse.response202(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	
	}	
	
	private void respondPOST(BufferedReader br, Map<String, String> http, HttpResponse httpResponse) {
		String url = http.get("url");
		if (url.equals("/")) {
			return;
		}
		
		if (url.equals("/user/create")) {
			int contentLength = Integer.parseInt(http.get("Content-Length"));
			if (contentLength < 0) {
				return;
			} 
			String userData = "";
			try {
				userData = util.IOUtils.readData(br, contentLength);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("User Data: " + userData);
			
			Map<String,String> data = new HashMap<String,String>();
			data = HttpRequestUtils.parseQueryString(userData);	
			User user = new User(data.get("userId"), data.get("password"),data.get("name"),data.get("email"));
			System.out.println("USER: " + user);
			
			// ADD USER to STATIC RAM MEMORY
			DataBase.addUser(user);
			
			// redirection
			url = "/index.html";
			try {
				httpResponse.response302(url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if (url.equals("/user/login")) {
			int contentLength = Integer.parseInt(http.get("Content-Length"));
			if (contentLength < 0) {
				return;
			} 
			String userData="";
			try {
				userData = util.IOUtils.readData(br, contentLength);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("User Data: " + userData);
			
			Map<String,String> data = new HashMap<String,String>();
			data = HttpRequestUtils.parseQueryString(userData);	

		
			// compare data
			User user = DataBase.findUserById(data.get("userId"));
			
			boolean setCookie = false;
			if (user != null && user.getPassword().equals(data.get("password"))) { // login succeeded.
				url = "/index.html";
				setCookie = true;
				System.out.println(user.toString());
			} else { // login failed.
				url = "/user/login_failed.html";
				setCookie = false;
				System.out.println("NO USER DATA");
			}
			
			// redirection
			try {
				httpResponse.response302(url,setCookie);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		try {
			httpResponse.response202(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
		
	}	
				
}
