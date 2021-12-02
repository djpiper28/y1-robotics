package common.netty.messages;

public class SignalStrengthReqMessage extends Message {

    public SignalStrengthReqMessage() {
        super(ResponseCode.REQUEST);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.SIGNAL_STRENGTH_RESP;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[] {super.getBaseHeader()};
    }

}
