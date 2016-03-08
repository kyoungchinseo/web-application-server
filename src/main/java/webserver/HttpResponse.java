package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
	
	OutputStream out;
	
	public HttpResponse(OutputStream out) {
		this.out = out;
	}
	
	public void response202(String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
		DataOutputStream dos = new DataOutputStream(out);
		String contentType = "text/html";
		if (url.contains("css")) {
			contentType = "text/css";
		}
		response200Header(dos, contentType, body.length);
		responseBody(dos, body);
	}
	
	public void response202(String url, String data) {
		DataOutputStream dos = new DataOutputStream(out);
		byte[] body = data.getBytes();
		String contentType = "text/html";
		if (url.contains("css")) {
			contentType = "text/css";
		}
		response200Header(dos, contentType, body.length);
		responseBody(dos, body);
	}
	
	
	public void response302(String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
		DataOutputStream dos = new DataOutputStream(out);
		response302Header(dos, url, false, body.length);
		responseBody(dos, body);
	}
		
	public void response302(String url, boolean setCookie) throws IOException {
		byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
		DataOutputStream dos = new DataOutputStream(out);
		response302Header(dos, url, setCookie, body.length);
		responseBody(dos, body);
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
	
	private void response200Header(DataOutputStream dos,int lengthOfBodyContent) {
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
