package common.netty.messages;

public class RobotDeathMessage extends Message {

    public RobotDeathMessage() {
        super(ResponseCode.SEND_DATA);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.DEAD_ROBOT;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[] {super.getBaseHeader()};
    }

}
