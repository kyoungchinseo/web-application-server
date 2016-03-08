package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			
			// read stream data
			Map<String,String> http = new HashMap<String,String>();
			http = generateHTTPMap(br);
			System.out.println("HASHMAP: " + http.toString());
				
			if (http.get("METHOD").equals("GET")) {
				String url = http.get("url");
				if (url.equals("/favicon.ico")) {
					return;
				}
				if (url.equals("/")) {
					url = "/index.html";
				}
				int index = url.indexOf("?");
				System.out.println("index:" + index);
				if (index > -1) { // request from GET method with data
					String requestPath = url.substring(0,index);
					String params = url.substring(index+1);
					
					Map<String,String> data = new HashMap<String,String>();
					data = HttpRequestUtils.parseQueryString(params);	
					User user = new User(data.get("userId"), data.get("password"),data.get("name"),data.get("email"));
					System.out.println("USER: " + user);
					
					DataOutputStream dos = new DataOutputStream(out);
					byte[] body = user.toString().getBytes();
					String contentType = "text/html";
					if (url.contains("css")) {
						contentType = "text/css";
					}
					response200Header(dos, contentType, body.length);
					responseBody(dos, body);
				} else { // no additional data (only html)
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
							
							byte[] body = html.toString().getBytes();
							DataOutputStream dos = new DataOutputStream(out);
							response200Header(dos, body.length);
							responseBody(dos, body);
							return;
						} 
						if (logined == false) {
							url = "/user/login.html";
							byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
							DataOutputStream dos = new DataOutputStream(out);
							response302Header(dos, url, false, body.length);
							responseBody(dos, body);
							return;
						}
					} else {
						byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
						DataOutputStream dos = new DataOutputStream(out);
						String contentType = "text/html";
						if (url.contains("css")) {
							contentType = "text/css";
						}
						response200Header(dos, contentType, body.length);
						responseBody(dos, body);
						return;
					}
				}	
				
				
				return;
			}
			
			if (http.get("METHOD").equals("POST")) {
				String url = http.get("url");
				if (url.equals("/")) {
					return;
				}
				if (url.equals("/user/create")) {
					int contentLength = Integer.parseInt(http.get("Content-Length"));
					if (contentLength < 0) {
						return;
					} 
					String userData =util.IOUtils.readData(br, contentLength);
					System.out.println("User Data: " + userData);
					
					Map<String,String> data = new HashMap<String,String>();
					data = HttpRequestUtils.parseQueryString(userData);	
					User user = new User(data.get("userId"), data.get("password"),data.get("name"),data.get("email"));
					System.out.println("USER: " + user);
					
					// ADD USER to STATIC RAM MEMORY
					DataBase.addUser(user);
					
					// redirection
					url = "/index.html";
					byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
					DataOutputStream dos = new DataOutputStream(out);
					response302Header(dos, url, false, body.length);
					responseBody(dos, body);
					return;
				}
				
				if (url.equals("/user/login")) {
					int contentLength = Integer.parseInt(http.get("Content-Length"));
					if (contentLength < 0) {
						return;
					} 
					String userData =util.IOUtils.readData(br, contentLength);
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
					byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
					DataOutputStream dos = new DataOutputStream(out);
					response302Header(dos, url, setCookie, body.length);
					responseBody(dos, body);
					return;
				}
				
				byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
				DataOutputStream dos = new DataOutputStream(out);
				String contentType = "text/html";
				if (url.contains("css")) {
					contentType = "text/css";
				}
				response200Header(dos, contentType, body.length);
				responseBody(dos, body);
				return;
				
			}	
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}


	private Map<String,String> generateHTTPMap(BufferedReader br) throws IOException {
		String line;
		Map<String, String> http = new HashMap<String, String>();
		while(!((line = br.readLine()).equals(""))) {
			//lines.add(line);
			String[] splitLine = line.split(": ");
			if (splitLine.length == 1) {
				String []tk = line.split(" ");
				http.put("METHOD", tk[0]);
				http.put("url",tk[1]);
			} else if (splitLine.length >= 2){
				http.put(splitLine[0],splitLine[1]);
			}
		}
		return http;
	}

	private void response302Header(DataOutputStream dos, String url, boolean setCookie, int lengthOfBodyContent) {
		// TODO Auto-generated method stub
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("Location: "+url+ "\r\n");
			if (setCookie) {
				dos.writeBytes("Set-Cookie: logined=true\r\n");
			} else {
				dos.writeBytes("Set-Cookie: logined=false\r\n");
			}
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200Header(DataOutputStream dos,String contentType, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: "+contentType+";charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
