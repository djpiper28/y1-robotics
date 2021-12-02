package common.netty.messages;

import java.io.IOException;

/**
 * This is for sterlisation and desterlisation of user notify messages
 * 
 * @author danny
 * @version 1.0
 */
public final class NotifyUserMessage extends Message {

	private String notifTitle, notifMessage;
	private final int BASE_LENGTH = 1 + 2 * Integer.BYTES;;

	@Override
	public String toString() {
		return "NotifyUserMessage{" +
				"notifTitle='" + notifTitle + '\'' +
				", notifMessage='" + notifMessage + '\'' +
				", BASE_LENGTH=" + BASE_LENGTH +
				'}';
	}

	/**
	 * only to be used for sterlisation
	 * 
	 * @param notification -> the notification
	 * @since 1.0
	 * @see <a href=
	 *      "https://developer.android.com/guide/topics/ui/notifiers/notifications">API
	 *      Page</a>
	 */
	public NotifyUserMessage(UserNotification notification) {
		this();

		if (notification.getNotifTitle() == null || notification.getNotifMessage() == null)
			throw new NullPointerException("Cannot sterlise null strings.");

		this.notifTitle = notification.getNotifTitle();
		this.notifMessage = notification.getNotifMessage();
	}

	/**
	 * Only to be used to get an object for desterlisation
	 * 
	 * @since 1.0
	 */
	public NotifyUserMessage() {
		super(ResponseCode.SEND_DATA);
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.NOTIFY_USER;
	}

	@Override
	public byte[] getByteArray() {
		int lenTitle = this.notifTitle.length(), lenMessage = this.notifMessage.length();

		// 1 byte for the header, 2 length integers then the strings
		byte[] bytes = new byte[BASE_LENGTH + (lenTitle + lenMessage) * Character.BYTES];
		bytes[0] = super.getBaseHeader();

		// Add lengths
		for (int i = 0; i < Integer.BYTES; i++)
			bytes[1 + i] = (byte) ((lenTitle >> (i * 8)) & BYTE_BIT_MASK);

		for (int i = 0; i < Integer.BYTES; i++)
			bytes[1 + Integer.BYTES + i] = (byte) ((lenMessage >> (i * 8)) & BYTE_BIT_MASK);

		// Add strings
		char[] titleArr = this.notifTitle.toCharArray(), messageArr = this.notifMessage.toCharArray();

		for (int i = 0; i < lenTitle; i++) {
			int baseIndex = BASE_LENGTH + i * Character.BYTES;
			bytes[baseIndex] = (byte) ((byte) titleArr[i] & BYTE_BIT_MASK);
			bytes[baseIndex + 1] = (byte) ((byte) (titleArr[i] >> 8) & BYTE_BIT_MASK);
		}

		for (int i = 0; i < lenMessage; i++) {
			int baseIndex = BASE_LENGTH + lenTitle * Character.BYTES + i * Character.BYTES;
			bytes[baseIndex] = (byte) ((byte) messageArr[i] & BYTE_BIT_MASK);
			bytes[baseIndex + 1] = (byte) ((byte) (messageArr[i] >> 8) & BYTE_BIT_MASK);
		}

		return bytes;
	}

	/**
	 * This will desterlise the byte array given to it to the UserNotification
	 * object sent to it
	 * 
	 * @param bytes[]          -> the raw data to desterlise
	 * @param UserNotification -> the object to write the data to after
	 *                         desterlisation
	 * @throws IOException if the bytes are invalid
	 * @since 1.0
	 */
	public void fromBytesArray(byte[] bytes, UserNotification dest) throws IOException {
		// Check for valid message
		if (bytes == null)
			throw new NullPointerException("Error: null input");

		byte type = (byte) (bytes[0] & NIBBLE_BIT_MASK);

		if (bytes.length < BASE_LENGTH) {
			throw new IOException(
					String.format("Error this message is of the wrong length (%d instead" + " of at least length %d)",
							bytes.length, BASE_LENGTH));
		}

		if (type != getMessageType().getTypeNibble()) {
			throw new IOException(
					String.format("Error this message is of the wrong " + "type (type %d instead of expected type %d).",
							type, getMessageType().getTypeNibble()));
		}

		// Get lengths
		int lenTitle = 0, lenMessage = 0;

		for (int i = 0; i < Integer.BYTES; i++)
			lenTitle |= bytes[1 + i] << (8 * i);

		for (int i = 0; i < Integer.BYTES; i++)
			lenMessage |= bytes[1 + Integer.BYTES + i] << (8 * i);

		// Check lengths make sense
		final int len = BASE_LENGTH + (lenTitle + lenMessage) * Character.BYTES;
		if (bytes.length != len) {
			throw new IOException(String.format("Error this message is of the wrong length (%d instead" + " of %d)",
					bytes.length, len));
		}

		// Read chars
		char[] titleArr = new char[lenTitle], messageArr = new char[lenMessage];

		for (int i = 0; i < lenTitle; i++) {
			int baseIndex = BASE_LENGTH + i * Character.BYTES;
			titleArr[i] = (char) bytes[baseIndex];
			titleArr[i] |= (char) bytes[baseIndex + 1] << 8;
		}

		for (int i = 0; i < lenMessage; i++) {
			int baseIndex = BASE_LENGTH + lenTitle * Character.BYTES + i * Character.BYTES;
			messageArr[i] = (char) bytes[baseIndex];
			messageArr[i] |= (char) bytes[baseIndex + 1] << 8;
		}

		// Put strings in object
		dest.setNotifTitle(new String(titleArr));
		dest.setNotifMessage(new String(messageArr));
	}

}
