package sunit.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Writes primitive types in little-endian and/or compact format to a stream and
 * supports writing strings in a specific encoding.
 * 
 * @author Tinus
 */
public class BinaryWriter implements Closeable {
	OutputStream output;
	Charset charset;
	boolean leaveOpen;
	
	// 9 bytes fits all types
	byte[] buffer = new byte[9];
	
	/**
	 * Initializes a new instance of the BinaryWriter class based on the specified
	 * stream and using UTF-8 encoding.
	 * 
	 * @param output
	 */
	public BinaryWriter(OutputStream output) {
		this(output, null, false);
	}
	
	/**
	 * Initializes a new instance of the BinaryWriter class based on the specified
	 * stream and character encoding.
	 * 
	 * @param output
	 * @param charset
	 */
	public BinaryWriter(OutputStream output, String charset) {
		this(output, charset, false);
	}
	
	/**
	 * Initializes a new instance of the BinaryWriter class based on the specified
	 * stream and character encoding, and optionally leaves the stream open.
	 * 
	 * @param output
	 * @param charset
	 * @param leaveOpen
	 */
	public BinaryWriter(OutputStream output, String charset, boolean leaveOpen) {
		this.output = output;
		this.charset = Charset.forName(charset == null ? "UTF-8" : charset);
		this.leaveOpen = leaveOpen;
	}
	
	/**
	 * Closes the stream if it is not initialized with the leaveOpen option
	 */
	@Override
	public void close() throws IOException {
		if (!leaveOpen) {
			output.close();
		}
	}
	
	public void write(byte[] value, int offset, int length) throws IOException {
		output.write(value, offset, length);
	}
	
	public void writeUint64(long value) throws IOException {
		writeUint(value, 8);
	}
	
	public void writeUint32(long value) throws IOException {
		writeUint(value, 4);
	}
	
	public void writeUint16(int value) throws IOException {
		writeUint(value, 2);
	}
	
	public void writeUint8(int value) throws IOException {
		writeUint(value, 1);
	}
	
	public void writeUint(long value, int length) throws IOException {
		// Bit layout of signed and unsigned are the same
		writeInt(value, length);
	}
	
	public void writeInt64(long value) throws IOException {
		writeInt(value, 8);
	}
	
	public void writeInt32(int value) throws IOException {
		writeInt(value, 4);
	}
	
	public void writeInt16(int value) throws IOException {
		writeInt(value, 2);
	}
	
	public void writeInt8(int value) throws IOException {
		writeInt(value, 1);
	}
	
	public void writeInt(long value, int length) throws IOException {
		for (int index = 0; index < length; index++) {
			buffer[index] = (byte) ((value >> (index * 8)) & 0xFF);
		}
		output.write(buffer, 0, length);
	}
	
	public void writeDouble(double value) throws IOException {
		ByteBuffer.wrap(buffer).putDouble(value);
		output.write(buffer, 0, 8);
	}
	
	public void writeFloat(float value) throws IOException {
		ByteBuffer.wrap(buffer).putFloat(value);
		output.write(buffer, 0, 4);
	}
	
	public void writeString(String value) throws IOException {
		output.write(value.getBytes(charset));
	}
	
	public void writeChars(char[] value) throws IOException {
		ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(value));
		output.write(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
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
	public void writeVaruint7(long value, int maxLength) throws IOException {
		if (maxLength <= 1) throw new IllegalArgumentException("Length need to larger then 1");
		if (maxLength > 9) throw new IllegalArgumentException("Length can't be larger then 9");
		
		int index = 0, length = 0, temp;
		while (index < maxLength) {
			temp = (int) (value & 0x7F);
			value >>= 7;
			length++;
			
			if (value < 0) {
				buffer[index] = (byte) temp;
				break;
			}
			
			temp |= 0x80;
			buffer[index++] = (byte) temp;
		}
		
		output.write(buffer, 0, length);
	}
	
	public void writeVaruint15(long value, int maxLength) throws IOException {
		
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
	public void writeVarsint7(long value, int maxLength) throws IOException {
		if (maxLength <= 1) throw new IllegalArgumentException("Length need to larger then 1");
		if (maxLength > 9) throw new IllegalArgumentException("Length can't be larger then 9");
		
		int index = 0, length = 0, temp;
		
		if (value >= 0) {
			temp = (int) (value & 0x3F);
			value >>= 6;
			length++;
			
			if (value > 0) {
				temp |= 0x40;
				buffer[index++] = (byte) temp;
				
				while (index < maxLength) {
					temp = (int) (value & 0x7F);
					value >>= 7;
					length++;
					
					if (value < 0) {
						buffer[index] = (byte) temp;
						break;
					}
					
					temp |= 0x80;
					buffer[index++] = (byte) temp;
				}
			}else {
				buffer[index++] = (byte) temp;
			}
		} else {
			temp = (int) (value & 0x3F);
			
			
			value >>= 6;
			length++;			
		}
		
		output.write(buffer, 0, length);
	}
	
	public void writeVarsint15(long value, int maxLength) throws IOException {
		
	}
}
