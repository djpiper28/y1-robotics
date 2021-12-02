package common.netty.messages;

/**
 * Contains the message types
 * @author Danny
 * @version 1.1
 */
public enum MessageType {

	FIELD_CHANGE((byte) 0),
	NOTIFY_USER((byte) 1),
	SIGNAL_STRENGTH_RESP((byte) 2),
	SIGNAL_STRENGTH_REQ((byte) 3),
	DEAD_ROBOT((byte) 4),
	CUBE_STATE_UPDATE((byte) 5);

	private byte typeNibble;

	public byte getTypeNibble() {
		return typeNibble;
	}

	public void setTypeNibble(byte typeNibble) {
		this.typeNibble = typeNibble;
	}

	private MessageType(byte typeNibble) {
		this.typeNibble = typeNibble;
	}
}
