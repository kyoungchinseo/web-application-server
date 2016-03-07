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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			
			String line = br.readLine();
			if (line == null) {
				return;
			}
			System.out.println(line);
			// split first line including file
			String [] tokens = line.split(" ");
			String url = tokens[1];
			if (url.equals("/")) {
				return;
			}
			
			int index = url.indexOf("?");
			System.out.println("index:" + index);
			if (index > -1) { 
			
				String requestPath = url.substring(0,index);
				String params = url.substring(index+1);
				
				//HttpRequestUtils hru = new HttpRequestUtils();
				Map<String,String> data = new HashMap<String,String>();
				data = HttpRequestUtils.parseQueryString(params);
				
				User user = new User(data.get("userId"), data.get("password"),data.get("name"),data.get("email"));
				
				System.out.println(user);
				
				String ID = data.get("userId");
				System.out.println("ID= " + ID);
				DataOutputStream dos = new DataOutputStream(out);
				byte[] body = user.toString().getBytes();
				response200Header(dos, body.length);
				responseBody(dos, body);
			} else if (url.equals("/favicon.ico")) {
				return;
			} else {
				byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
				DataOutputStream dos = new DataOutputStream(out);
				response200Header(dos, body.length);
				responseBody(dos, body);
			}	
			
			
			
			
			
			
		
			
			
			
			
			
			
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
