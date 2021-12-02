package common.netty.messages;

import java.io.IOException;
import java.util.HashMap;

/**
 * Rubiks Cube Message to the EV3
 * @version 1.0
 * @author James
 */
public class RubiksCubeMessage extends Message {
    private static HashMap<String, Integer> cubeMessageConvert = new HashMap<String, Integer>();

    private int rotationCode;
    private int movesToSolve;

    public RubiksCubeMessage(String rotation, int moves){
        super(ResponseCode.SEND_DATA);

        fillConversionHashmap();
        movesToSolve = moves;
        rotationCode = cubeMessageConvert.get(rotation);

    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CUBE_STATE_UPDATE;
    }

    @Override
    public byte[] getByteArray() {
        byte[] returnArray = new byte[3];
        returnArray[0] = super.getBaseHeader();
        returnArray[1] = (byte) rotationCode;
        returnArray[2] = (byte) movesToSolve;
        return returnArray;
    }

    private void fillConversionHashmap(){
        cubeMessageConvert.put("N", -1);
        cubeMessageConvert.put("R", 0);
        cubeMessageConvert.put("'R", 1);
        cubeMessageConvert.put("L", 2);
        cubeMessageConvert.put("'L", 3);
        cubeMessageConvert.put("F", 4);
        cubeMessageConvert.put("'F", 5);
        cubeMessageConvert.put("U", 6);
        cubeMessageConvert.put("'U", 7);
        cubeMessageConvert.put("B", 8);
        cubeMessageConvert.put("'B", 9);
        cubeMessageConvert.put("D", 10);
        cubeMessageConvert.put("'D", 11);
    }
}
