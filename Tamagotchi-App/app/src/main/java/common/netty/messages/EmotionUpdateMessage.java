package common.netty.messages;

import java.io.IOException;

import common.EmotionPrimitiveID;
import common.EmotionsInterface;

/**
 * This is for sterlisation and desterlisation of emotion update messages
 * 
 * @author Danny
 * @version 1.1
 */
public final class EmotionUpdateMessage extends Message {

	private byte ID;
	private float newValue;
	private static final int LENGTH = 3;
	private static final float CONSTANT = 100f; //A workaround for Nan in my floats

	/**
	 * Only to be used to get an object for sterlisation
	 * 
	 * @param EmotionPrimitiveID -> this is the ID of the field to change in the
	 *                           EmotionsInterface class
	 * @param float              newValue -> the new value of the field to send to
	 *                           the user
	 * @since 1.0
	 * @see common.EmotionPrimitiveID
	 * @see common.EmotionsInterface
	 */
	public EmotionUpdateMessage(EmotionPrimitiveID fieldID, float newValue) {
		this();
		this.ID = fieldID.getID();
		this.newValue = newValue;
	}

	/**
	 * Only to be used to get an object for desterlisation
	 * 
	 * @since 1.0
	 */
	public EmotionUpdateMessage() {
		super(ResponseCode.SEND_DATA);
	}

	/**
	 * @return the byte array of the the field change request
	 * @since 1.1
	 */
	@Override
	public byte[] getByteArray() {
		byte[] bytes = new byte[LENGTH];
		bytes[0] = super.getBaseHeader();
		bytes[1] = ID;
		bytes[2] = (byte) Math.round(this.newValue * CONSTANT);

		return bytes;
	}

	/**
	 * This will desterlise the byte array given to it to the EmotionsInterface
	 * object sent to it
	 * 
	 * @param bytes[]  -> the raw data to desterlise
	 * @param emotions -> the object to write the data to after desterlisation
	 * @throws IOException if the bytes are invalid
	 * @since 1.1
	 */
	public void fromBytesArray(byte[] bytes, EmotionsInterface emotions) throws IOException {
		// Check for valid message
		if (bytes == null)
			throw new NullPointerException("Error: null input");

		if (bytes.length != LENGTH) {
			throw new IOException(String.format("Error this message is of the wrong length (%d instead" + " of %d)",
					bytes.length, LENGTH));
		}

		byte type = (byte) (bytes[0] & NIBBLE_BIT_MASK);

		if (type != getMessageType().getTypeNibble()) {
			throw new IOException(
					String.format("Error this message is of the wrong " + "type (type %d instead of expected type %d).",
							type, getMessageType().getTypeNibble()));
		}

		// Read message
		float newValue = ((float) bytes[2]) / CONSTANT;

		// Put new value in correct field
		if (bytes[1] == EmotionPrimitiveID.FEAR.getID()) {
			emotions.setFear(newValue);
		} else if (bytes[1] == EmotionPrimitiveID.HUNGER.getID()) {
			emotions.setHunger(newValue);
		} else if (bytes[1] == EmotionPrimitiveID.TIRED.getID()) {
			emotions.setTired(newValue);
		} else if (bytes[1] == EmotionPrimitiveID.BOREDOM.getID()) {
			emotions.setBoredem(newValue);
		} else {
			throw new IOException(String.format("The field changed byte is not recognised (type ID: %d).", bytes[1]));
		}
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.FIELD_CHANGE;
	}

}
