package sunit.io.net;

import java.io.IOException;
import java.io.InputStream;

public class NetworkInputStream extends InputStream {
	private Socket socket;
	boolean ownsSocket;
	
	public NetworkInputStream(Socket socket) {
		this(socket, false);
	}
	
	public NetworkInputStream(Socket socket, boolean ownsSocket) {
		this.socket = socket;
		this.ownsSocket = ownsSocket;
	}
	
	@Override
	public int available() throws IOException {
		return socket.available();
	}
	
	@Override
	@Deprecated
	public int read() throws IOException {
		byte[] buffer = new byte[1];
		if(read(buffer) == -1) return 0;
		return buffer[0];
	}
	
	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}
	
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if(socket.available(true) > 0) {
			return socket.receive(buffer, offset, length);
		}
		return -1;
	}
	
	@Override
	public void close() throws IOException {
		if(ownsSocket) {
			socket.close();
		}
	}
	
}
