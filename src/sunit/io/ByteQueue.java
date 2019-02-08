package sunit.io;

public class ByteQueue {
	int head;
	int tail;
	byte[] buffer;
	
	/**
	 * 
	 * @param capacity
	 */
	public ByteQueue(int capacity) {
		buffer = new byte[capacity];
	}
	
	/**
	 * Length of the data in this queue
	 * 
	 * @return
	 */
	public synchronized int length() {
		return (head > tail ? tail + buffer.length : tail) - head;
	}
	
	/**
	 * The total this buffer can contain
	 * 
	 * @return
	 */
	public int size() {
		return buffer.length;
	}
	
	/**
	 * Read to the max length of the destination
	 * 
	 * @param destination
	 * @return The amount of bytes read
	 */
	public synchronized int read(byte[] destination) {
		int read = 0;
		
		while (read < destination.length && head != tail) {
			// Copy the byte
			destination[read++] = buffer[head];
			
			// Increment the head
			head = (head + 1) % buffer.length;
		}
		
		return read;
	}

	/**
	 * Read to the max of length into destination at a given offset
	 * 
	 * @param destination
	 * @param offset
	 * @param length
	 * @return
	 */
	public synchronized int read(byte[] destination, int offset, int length) {
		int read = 0;
		
		while (read < length && head != tail) {
			// Copy the byte
			destination[offset + read++] = buffer[head];
			
			// Increment the head
			head = (head + 1) % buffer.length;
		}
		
		return read;
	}
	
	/**
	 * Writes the source bytes to max of length
	 * 
	 * @param source
	 * @param length
	 * @return The amount of bytes written
	 */
	public synchronized int write(byte[] source, int length) {
		if (source == null) throw new IllegalArgumentException("Source is null");
		if (Math.abs(length) > source.length) throw new IllegalArgumentException("Length is to long");
		
		if (length < 0) length = source.length - length;
		
		int written = 0;
		
		while (written < length && (tail + 1) % buffer.length != head) {
			// Move the tail to the next empty index
			tail = (tail + 1) % buffer.length;
			
			// Copy the byte
			buffer[tail] = source[written++];
		}
		
		return written;
	}
}
