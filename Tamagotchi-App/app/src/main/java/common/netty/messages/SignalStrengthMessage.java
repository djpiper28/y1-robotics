package common.netty.messages;

import java.io.IOException;

/**
 * Signal strength (RESP) message
 * @version 1.0
 * @author Danny
 */
public class SignalStrengthMessage extends Message {

    private double signalStrength;
    private static final int LENGTH = 1 + Double.BYTES;

    public SignalStrengthMessage() {
        super(ResponseCode.SEND_DATA);
    }

    public SignalStrengthMessage(double signalStrength) {
        this();
        this.signalStrength = signalStrength;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.SIGNAL_STRENGTH_RESP;
    }

    @Override
    public byte[] getByteArray() {
        byte[] bytes = new byte[LENGTH];
        bytes[0] = super.getBaseHeader();

        long bits = Double.doubleToRawLongBits(this.signalStrength);
        for (int i = 0; i < Double.BYTES; i++)
            bytes[1 + i] = (byte) ((bits >> Double.BYTES - 1 - i) & BYTE_BIT_MASK);

        return bytes;
    }

    /**
     * Gets the signal strength from the byte array
     * @param bytes -> raw data
     * @return double - signal strength read from the device
     * @throws IOException
     */
    public double fromBytes(byte[] bytes) throws IOException {
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

        long bits = 0x0;
        for (int i = 0; i < Double.BYTES; i++)
            bits |= (byte) ((bytes[i + 1] << Double.BYTES - 1 - i) & BYTE_BIT_MASK);

        return Double.longBitsToDouble(bits);
    }
}
