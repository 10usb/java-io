package sunit.io.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import sunit.io.ByteQueue;

/**
 * Wrapper class around the java socket class to be able to write none blocking
 * reading code
 * 
 * @author Tinus
 */
public class Socket implements Closeable {
	private Thread thread;
	private java.net.Socket socket;
	private InputStream input;
	private OutputStream output;
	private ByteQueue receiveBuffer;
	private IOException readError;
	
	/**
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public Socket(java.net.Socket socket) throws IOException {
		this.socket = socket;
		socket.setSoTimeout(1000);
		input = socket.getInputStream();
		output = socket.getOutputStream();
		receiveBuffer = new ByteQueue(8192);
		
		thread = new SocketThread();
		thread.start();
	}
	
	/**
	 * The amount available to read
	 * 
	 * @return
	 * @throws IOException
	 */
	public int available() throws IOException {
		if (readError != null) {
			IOException tmp = readError;
			readError = null;
			throw tmp;
		}
		return receiveBuffer.length();
	}
	
	/**
	 * The amount available to read with the option to block until at least 1 byte
	 * is available
	 * 
	 * @param block
	 * @return
	 * @throws IOException
	 */
	public int available(boolean block) throws IOException {
		// Do I need to change the if to next uncommented line....?
		// if(!block) throw new StupidException("Use the none blocking version!!");
		if (block) {
			// TODO implement an wait() / notify() construction
			// also known as "consumer/producer pattern", for nerds trying to understand the
			// comment
			while (receiveBuffer.length() <= 0) try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
		}
		
		if (readError != null) {
			IOException tmp = readError;
			readError = null;
			throw tmp;
		}
		
		return receiveBuffer.length();
	}
	
	/**
	 * To tell is this socket is still connected or not
	 * 
	 * @return true when connected
	 */
	public boolean isConnected() {
		return !socket.isClosed();
	}
	
	/**
	 * Read to the max size of the destination from the received buffer
	 * 
	 * @param destination
	 * @return the length read
	 * @throws IOException
	 */
	public int receive(byte[] destination) throws IOException {
		if (readError != null) {
			IOException tmp = readError;
			readError = null;
			throw tmp;
		}
		return receiveBuffer.read(destination);
	}
	
	/**
	 * Read to the max size of length from the received buffer
	 * 
	 * @param destination
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException 
	 */
	public int receive(byte[] destination, int offset, int length) throws IOException {
		if (readError != null) {
			IOException tmp = readError;
			readError = null;
			throw tmp;
		}
		return receiveBuffer.read(destination, offset, length);
	}
	
	/**
	 * 
	 * @param destination
	 * @param block
	 * @return
	 * @throws IOException
	 */
	public int receive(byte[] destination, boolean block) throws IOException {
		if (!block) return receive(destination);
		
		// TODO Construct a while loop, that constantly reads from the buffer until a
		// timeout
		if (receive(destination) != destination.length) throw new SocketTimeoutException();
		return destination.length;
	}
	
	/**
	 * Send the bytes to the output
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public int send(byte[] source) throws IOException {
		output.write(source);
		return source.length;
	}
	
	/**
	 * Closes the socket
	 */
	@Override
	public void close() {
		try {
			if (!socket.isClosed()) socket.close();
		} catch (IOException ex) {
		}
	}
	
	/**
	 * Internal thread that constantly tries to read data when local buffer it not
	 * full
	 * 
	 * @author Tinus
	 */
	public class SocketThread extends Thread {
		byte[] buffer;
		
		/**
		 * While the socket is open it will try to read something from the connection
		 */
		@Override
		public void run() {
			buffer = new byte[receiveBuffer.size()];
			
			while (socket.isConnected() && !socket.isInputShutdown() && !socket.isClosed()) {
				read();
			}
		}
		
		/**
		 * Tests how much it can read until the buffer is full, tries to read it and on
		 * success add's it to the buffer
		 * 
		 * @return
		 */
		private boolean read() {
			int canRead = receiveBuffer.size() - receiveBuffer.length();
			if (canRead > 0) {
				int read = read(canRead);
				if (read > 0) {
					receiveBuffer.write(buffer, read);
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Tries to read length amount from the input stream. If a error occur other
		 * then a read timeout It sets that error into the read error and closes the
		 * socket
		 * 
		 * @param length
		 * @return
		 */
		private int read(int length) {
			try {
				return input.read(buffer, 0, length);
			} catch (SocketTimeoutException e) {
				return 0;
			} catch (IOException ex) {
				readError = ex;
				close();
				return -1;
			}
		}
	}
}
