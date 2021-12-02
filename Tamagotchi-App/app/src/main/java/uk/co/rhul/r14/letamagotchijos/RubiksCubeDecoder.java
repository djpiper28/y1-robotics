/**
 * This is the main class for decoding the cube
 * it calls the BLE interface and the CubeDecoder to connect to the cube and decode the messages recieved from it
 *
 * @author James
 * @version 1.0
 **/

package uk.co.rhul.r14.letamagotchijos;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import common.netty.messages.MessagesIOHandler;
import common.netty.messages.RubiksCubeMessage;
import uk.co.rhul.r14.letamagotchijos.min2phase.Search;

public class RubiksCubeDecoder {

    final private static String TAG = "RubiksCubeDecoder";
    private static final HashMap<String, int[][]> rotations = new HashMap<String, int[][]>();
    private static MessagesIOHandler[] btConn;
    private final RubiksCubeBLEInterface cubeBLEInterface;
    private CubeData previousCubeState;
    private int scrambledAmount;


    public RubiksCubeDecoder(BluetoothAdapter btAdapter, Context context, MessagesIOHandler[] conn) {
        cubeBLEInterface = new RubiksCubeBLEInterface(btAdapter, this, context);
        cubeBLEInterface.startBluetoothListener();
        previousCubeState = null; // used for getting the change that has been made to the cube
        fillRotationsMap();
        btConn = conn;
        Search.init();// apparently takes about 200ms to startup
    }

    private static int[] byteArrToIntArr(byte[] bytes) {
        int[] array = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            array[i] = (int) bytes[i] & 0xFF;
        }
        return array;
    }

    private static String intsToHex(int[] ints) {
        if (ints == null)
            return null;
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ints.length * 3];
        for (int j = 0; j < ints.length; j++) {
            int v = ints[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }// partly from stackoverflow

    private static String intsToString(int[] ints) {
        String outString = "";
        for (int i = 0; i < ints.length; i++) {
            outString = String.format("%s %d", outString, ints[i]);
            //outString.concat(Integer.toString(ints[i])+" ");
        }
        return outString;
    }

    private static void fillRotationsMap() {
        // this fills the rotations hashmap with the array values to determine the rotation notation from the cubelet array
        // there are 2 points of reference to determine the rotation that has been made
        rotations.put("R", new int[][]{{2, 20}, {8, 2}});
        rotations.put("'R", new int[][]{{20, 2}, {2, 8}});
        rotations.put("L", new int[][]{{0, 6}, {18, 0}});
        rotations.put("'L", new int[][]{{6, 0}, {0, 18}});
        rotations.put("F", new int[][]{{0, 2}, {2, 8}});
        rotations.put("'F", new int[][]{{2, 0}, {8, 2}});
        rotations.put("U", new int[][]{{2, 0}, {20, 2}});
        rotations.put("'U", new int[][]{{0, 2}, {2, 20}});
        rotations.put("B", new int[][]{{26, 20}, {20, 18}});
        rotations.put("'B", new int[][]{{20, 26}, {18, 20}});
        rotations.put("D", new int[][]{{6, 8}, {8, 26}});
        rotations.put("'D", new int[][]{{8, 6}, {26, 8}});
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    } // from stackoverflow

    private static int[] hexStringToIntArray(String s) {
        int len = s.length();
        int[] data = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (int) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
            data[i / 2] = data[i / 2] & 0xFF;
        }
        return data;
    }

    private static String getCubeMove(int[] before, int[] after) {// takes in the cubelet positions for the before and after states to determine the rotation
        for (String s : rotations.keySet()) {
            int[][] arrIndexes = rotations.get(s);
            if (before[arrIndexes[0][0]] == after[arrIndexes[0][1]] && before[arrIndexes[1][0]] == after[arrIndexes[1][1]]) {
                return s;
            }
        }
        return null;
    }

    private static void sendCubeDataToEv3(String rotation, int movesToSolve) {
        RubiksCubeMessage msg = new RubiksCubeMessage(rotation, movesToSolve);
        try {
            if (btConn.length > 0) {
                btConn[0].sendMessage(msg);
            }
        } catch (IOException e) {
            //Log.e(MainActivity.class.toString(), "Error sending cube data to the EV3");
            //e.printStackTrace();
        }
    }

    public void newCubeState() {
        int[] rawCubeState = byteArrToIntArr(cubeBLEInterface.getCubeState());
        CubeData cube = new CubeData(rawCubeState);
        int[] cubeArray = cube.getCubeletPositions();
        boolean solved = cube.getSolved();
        int[] cubeletPositions = cube.getCubeletPositions();


        //String min2phaseFormatScramble = cube.getMin2PhaseFormat();
        //String solveSequence = new Search().solution(min2phaseFormatScramble, 21, 100000000, 0, 0);
        //scrambledAmount = solveSequence.length() - solveSequence.replaceAll(" ", "").length();// the length of the total needed moves to solve, solveSequence is a string and the amount of spaces is the amount of moves that need to be made
        //Log.i(TAG, "MIN2PHASE FORMAT: "+min2phaseFormatScramble+" Solve sequence: "+solveSequence+" Scrambled amount: "+Integer.toString(scrambledAmount));
        //Log.i(TAG, "RAW HEX FROM MOVE: "+intsToHex(rawCubeState));
        String cubeMove;
        if (previousCubeState != null) {
            Log.i(TAG, "New cubelet array: " + intsToString(cube.getCubeArray()) + " Solved? " + (solved ? "True" : "False") + " Rotation: " + getCubeMove(previousCubeState.getCubeletPositions(), cubeletPositions));
            cubeMove = getCubeMove(previousCubeState.getCubeletPositions(), cubeletPositions);
        } else {
            cubeMove = "N";
        }
        int scrambledAmount = getScrambledAmount(cube);
        sendCubeDataToEv3(cubeMove, scrambledAmount);
        //DecimalFormat decimalFormat = new DecimalFormat("#.0000");
        Log.i(TAG, "Scrambled Amount: " + getScrambledAmount(cube));//Integer.toString(getScrambledAmount(cube)));


        previousCubeState = cube;
    }

    public int getScrambledAmount(CubeData cube) {
        if (cube.getSolved()) return 0;
        String result = new Search().solution(cube.getMin2PhaseFormat(), 21, 100000000, 0, 0);
        int movesRequired = result.length() - result.replaceAll(" ", "").length();
        return movesRequired;
        //return movesRequired;
    }

    public int getScrambledAmount() {
        if (previousCubeState.getSolved()) return 0;
        String result = new Search().solution(previousCubeState.getMin2PhaseFormat(), 21, 100000000, 0, 0);
        int movesRequired = result.length() - result.replaceAll(" ", "").length();
        return movesRequired;
    }
}
