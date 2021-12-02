package common.netty.messages;

/**
 * @author Danny
 * @version 1.1
 */
public enum ResponseCode {

	SEND_DATA((byte) 0), REQUEST((byte) 1);

	private byte reponseNibble;

	public byte getReponseNibble() {
		return reponseNibble;
	}

	public void setReponseNibble(byte reponseNibble) {
		this.reponseNibble = reponseNibble;
	}

	private ResponseCode(byte respNibble) {
		this.reponseNibble = respNibble;
	}

}
