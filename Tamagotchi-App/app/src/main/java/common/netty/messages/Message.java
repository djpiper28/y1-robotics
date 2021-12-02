package common.netty.messages;

import java.io.IOException;

import common.netty.messages.ResponseCode;
import common.netty.messages.MessageType;

/**
 * This is the base class for a message. All message subclasses should extend
 * this class and call the constructor that is here when they are made. They
 * should have a method that produces a byte array to represent their message.
 * Each class that extends this must have a desterilisation method
 * 
 * @author Danny
 * @version 1.0
 */
public abstract class Message {

	private byte baseHeader;
	public static final byte NIBBLE_BIT_MASK = 0xF;
	public static final byte BYTE_BIT_MASK = (byte) 0xFF;

	/**
	 * For internal use only. This should be called by the contructor of any class
	 * that extends this and should create the entire message on the call.
	 * 
	 * @param respCode -> this the response code nibble
	 * @param type     -> this is a nibble that represents the type of the message
	 * @since 1.0
	 */
	protected Message(ResponseCode respCode) {
		this.baseHeader = (byte) ((respCode.getReponseNibble() & NIBBLE_BIT_MASK << 4)
				+ (this.getMessageType().getTypeNibble() & NIBBLE_BIT_MASK));
	}

	/**
	 * @return MessageType -> this is the message type that this class if for
	 * @since 1.0
	 */
	public abstract MessageType getMessageType();

	/**
	 * @return byte -> the message header as a byte
	 * @since 1.0
	 */
	protected byte getBaseHeader() {
		return this.baseHeader;
	}

	/**
	 * @return byte[] -> this is the byte array of the message.
	 * @since 1.0
	 */
	public abstract byte[] getByteArray();

	/**
	 * @param data is the bytes array to get the message type from
	 * @return the message type
	 * @since 1.0
	 */
	public static MessageType getMessageType(byte[] data) throws IOException {
		if (data.length < 1) {
			throw new IOException("Invalid Length");
		}

		byte type = (byte) ((byte) data[0] & NIBBLE_BIT_MASK);

		for (MessageType m : MessageType.values())
			if (m.getTypeNibble() == type)
				return m;

		return null;
	}

}
