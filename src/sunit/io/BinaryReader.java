package sunit.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class BinaryReader implements Closeable {
	InputStream input;
	Charset charset;
	boolean leaveOpen;
	
	// 9 bytes fits all types
	byte[] buffer = new byte[9];
	
	/**
	 * Initializes a new instance of the BinaryWriter class based on the specified
	 * stream and using UTF-8 encoding.
	 * 
	 * @param input
	 */
	public BinaryReader(InputStream input) {
		this(input, null, false);
	}
	
	/**
	 * Initializes a new instance of the BinaryWriter class based on the specified
	 * stream and character encoding.
	 * 
	 * @param input
	 * @param charset
	 */
	public BinaryReader(InputStream input, String charset) {
		this(input, charset, false);
	}
	
	/**
	 * Initializes a new instance of the BinaryWriter class based on the specified
	 * stream and character encoding, and optionally leaves the stream open.
	 * 
	 * @param input
	 * @param charset
	 * @param leaveOpen
	 */
	public BinaryReader(InputStream input, String charset, boolean leaveOpen) {
		this.input = input;
		this.charset = Charset.forName(charset == null ? "UTF-8" : charset);
		this.leaveOpen = leaveOpen;
	}
	
	/**
	 * Closes the stream if it is not initialized with the leaveOpen option
	 */
	@Override
	public void close() throws IOException {
		if (!leaveOpen) {
			input.close();
		}
	}
	
	public void read(byte[] value, int offset, int length) throws IOException {
		int total = 0;
		do {
			int read = input.read(value, total, length - total);
			if (read <= 0) throw new IOException("Failed to read needed amount of bytes");
			total += read;
		} while (total < length);
	}
	
	public long readUint64() throws IOException {
		return readUint(8);
	}
	
	public long readUint32() throws IOException {
		return readUint(4);
	}
	
	public int readUint16() throws IOException {
		return (int) readUint(2);
	}
	
	public int readUint8() throws IOException {
		return (int) readUint(1);
	}
	
	public long readUint(int length) throws IOException {
		// Bit layout of signed and unsigned are the same
		return readInt(length);
	}
	
	public long readInt64() throws IOException {
		return readInt(8);
	}
	
	public int readInt32() throws IOException {
		return (int) readInt(4);
	}
	
	public int readInt16() throws IOException {
		return (int) readInt(2);
	}
	
	public int readInt8() throws IOException {
		return (int) readInt(1);
	}
	
	public long readInt(int length) throws IOException {
		read(buffer, 0, length);
		long value = 0;
		for (int index = 0; index < length; index++) {
			value |= (buffer[index] & 0xffl) << (index * 8);
		}
		return value;
	}
	
	public double readDouble() throws IOException {
		read(buffer, 0, 8);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getDouble();
		
	}
	
	public float readFloat() throws IOException {
		read(buffer, 0, 8);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}
	
	public String readString(int length) throws IOException {
		byte[] buffer = new byte[length];
		read(buffer, 0, length);
		
		return new String(buffer, charset);
	}
	
	public char[] readChars(int length) throws IOException {
		byte[] buffer = new byte[length];
		read(buffer, 0, length);
		
		return ByteBuffer.wrap(buffer).asCharBuffer().array().clone();
	}
	
	/**
	 * Writes a variable length unsigned integer with chunks of 7 bits each in every
	 * byte. When the value fits within 7 bits of data (max 127) only one byte will
	 * be written. And that byte will be the same as if it was a regular unsigned
	 * byte
	 * 
	 * @param value
	 * @param maxLength
	 * @throws IOException
	 */
	public long readVaruint7(int maxLength) throws IOException {
		if (maxLength <= 1) throw new IllegalArgumentException("Length need to larger then 1");
		if (maxLength > 9) throw new IllegalArgumentException("Length can't be larger then 9");
		return 0;
	}
	
	public long readVaruint15(long value, int maxLength) throws IOException {
		return 0;
	}
	
	/**
	 * Writes a variable length integer where the first byte is compatible with a
	 * signed byte where the value is from -64 to 64. Any extending byte only
	 * contains 7 bits of extra data and an extend bit
	 * 
	 * @param value
	 * @param maxLength
	 * @throws IOException
	 */
	public long readVarsint7(int maxLength) throws IOException {
		if (maxLength <= 1) throw new IllegalArgumentException("Length need to larger then 1");
		if (maxLength > 9) throw new IllegalArgumentException("Length can't be larger then 9");
		return 0;
	}
	
	public long readVarsint15(int maxLength) throws IOException {
		return 0;
	}
	
}
