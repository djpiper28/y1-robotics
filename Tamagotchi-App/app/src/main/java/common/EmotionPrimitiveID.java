package common;

public enum EmotionPrimitiveID {

	FEAR((byte) 0), HUNGER((byte) 1), TIRED((byte) 2), BOREDOM((byte) 3);

	private final byte ID;

	public final byte getID() {
		return ID;
	}

	private EmotionPrimitiveID(byte ID) {
		this.ID = ID;
	}

}
