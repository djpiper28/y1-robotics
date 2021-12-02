/**
 * This is the CubeData class, it's used for decoding the messages from the cube and putting them into a readable format
 * The output formats are the "Cubelets" which are what colours are on what part of the cube
 * The "CubeArray" format (called by getCubeArray()) returns the paper format (shown here https://miro.medium.com/max/654/1*qipWhJUMnafaJY2u4Ix9RA.jpeg)
 * The "min2phaseFormat" format (called by getMin2PhaseFormat()) is used for the min2phase library so it can solve the cube
 *
 * @author James
 * @version 1.0
 **/

package uk.co.rhul.r14.letamagotchijos;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CubeData {

    final private static String TAG = "CubeData";
    final private static int[] solvedState = {6, 6, 6, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4};
    final private static int[][] standardCubeFormat = new int[][]{
            // this is the standard format for the cubelets, you can work out which cubelet is what from this
            // the numbers represent colours and the 0's represent a non coloured side
            // it's used to determine the cubelet ID
            // Front slice
            {4, 5, 0, 0, 1, 0}, {4, 5, 0, 0, 0, 0}, {4, 5, 6, 0, 0, 0},//  0, 1, 2
            {4, 0, 0, 0, 1, 0}, {4, 0, 0, 0, 0, 0}, {4, 0, 6, 0, 0, 0},//  3, 4, 5
            {4, 0, 0, 3, 1, 0}, {4, 0, 0, 3, 0, 0}, {4, 0, 6, 3, 0, 0},//  6, 7, 8
            // Standing slice
            {0, 5, 0, 0, 1, 0}, {0, 5, 0, 0, 0, 0}, {0, 5, 6, 0, 0, 0},    //  9, 10, 11
            {0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 0}, {0, 0, 6, 0, 0, 0},    // 12, XX, 14
            {0, 0, 0, 3, 1, 0}, {0, 0, 0, 3, 0, 0}, {0, 0, 6, 3, 0, 0},    // 15, 16, 17
            // Back slice
            {0, 5, 0, 0, 1, 2}, {0, 5, 0, 0, 0, 2}, {0, 5, 6, 0, 0, 2},// 18, 19, 20
            {0, 0, 0, 0, 1, 2}, {0, 0, 0, 0, 0, 2}, {0, 0, 6, 0, 0, 2},// 21, 22, 23
            {0, 0, 0, 3, 1, 2}, {0, 0, 0, 3, 0, 2}, {0, 0, 6, 3, 0, 2} // 24, 25, 26
    };
    // we cannot just use the cubelet positions as that does not show if the cubelets have been rotated!
    private static final HashMap<Integer, Character> letterConversion = new HashMap<Integer, Character>(); // for the cube solver, this links the numbers that we use to letters that the solver uses
    private final int[] rawData; // this is the array where the raw data from the cube is put into

    private int errCheck; // if this is more than 1, the raw data is invalid, mainly used for debugging

    private final int[] cubeData; // cubeData is the data after it has been processed though "cubeRawDataDecode"

    private final int[] array; // the final output array

    private final int[] array2; // arrays to calculate the final array
    private final int[] array3;
    private final int[] array4;
    private final int[] array5;

    /**
     *
     * @param rawDataIn takes the raw data in from the cube in an integer array format
     */
    public CubeData(int[] rawDataIn) {
        rawData = rawDataIn;
        errCheck = 0;
        cubeData = new int[20];
        array = new int[55];
        array2 = new int[8];
        array3 = new int[8];
        array4 = new int[12];
        array5 = new int[12];
        cubeRawDataDecode(); // converts the raw hex into a form that can be processed further
        converseToPaperType(); // converts the processed hex into a "paper type" format (https://miro.medium.com/max/654/1*qipWhJUMnafaJY2u4Ix9RA.jpeg)
        fillConversionHashMap(); // this just fills the conversion map to convert from the colour number to letter, used for the solver
    }

    private static int[] hexStringToIntArray(String s) {
        int len = s.length();
        int[] data = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (int) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            data[i / 2] = data[i / 2] & 0xFF;
        }
        return data;
    }

    private static boolean checkArrColours(int[] arr1, int[] arr2) {// checks whether arr1 and arr2 contain the same values (order doesn't matter)
        List<Integer> list1Colours = new ArrayList<Integer>();
        List<Integer> list2Colours = new ArrayList<Integer>();

        for (int i = 0; i < 6; i++) { // adds the colours to the lists if they're not invalid (0)
            if (arr1[i] != 0) list1Colours.add(arr1[i]);
            if (arr2[i] != 0) list2Colours.add(arr2[i]);
        }
        if (list1Colours.size() != list2Colours.size())
            return false; // if they contain a different number of colours, return 0

        for (Integer c : list1Colours) {// loops through list 1, if c is not in list2, return false
            if (!list2Colours.contains(c)) return false;
        }
        return true; // otherwise it must be true
    }

    /**
     *
     * @return returns the cubelet array
     */
    public int[][] stateToCubelets() {
        // a cube is made of cubelets, these define the cubelet positions with the colours
        // cubelets have 6 sides and only a maximum of 3 are visible at a time
        // this array sets the cubelets based off of their colour
        // it's not needed but it makes life easier for me
        int[] d = getCubeArray();
        int[][] cubeletsArr = {
                {d[51], d[17], 0, 0, d[35], 0}, {d[48], d[14], 0, 0, 0, 0}, {d[45], d[11], d[2], 0, 0, 0},
                {d[52], 0, 0, 0, d[34], 0}, {d[49], 0, 0, 0, 0, 0}, {d[46], 0, d[1], 0, 0, 0},
                {d[53], 0, 0, d[42], d[33], 0}, {d[50], 0, 0, d[39], 0, 0}, {d[47], 0, d[0], d[36], 0, 0},

                {0, d[16], 0, 0, d[32], 0}, {0, d[13], 0, 0, 0, 0}, {0, d[10], d[5], 0, 0, 0},
                {0, 0, 0, 0, d[31], 0}, {0, 0, 0, 0, 0, 0}, {0, 0, d[4], 0, 0, 0},
                {0, 0, 0, d[43], d[30], 0}, {0, 0, 0, d[40], 0, 0}, {0, 0, d[3], d[37], 0, 0},

                {0, d[15], 0, 0, d[29], d[26]}, {0, d[12], 0, 0, 0, d[23]}, {0, d[9], d[8], 0, 0, d[20]},
                {0, 0, 0, 0, d[28], d[25]}, {0, 0, 0, 0, 0, d[22]}, {0, 0, d[7], 0, 0, d[19]},
                {0, 0, 0, d[44], d[27], d[24]}, {0, 0, 0, d[41], 0, d[21]}, {0, 0, d[6], d[38], 0, d[18]}
        };

        return cubeletsArr;
    }

    /**
     *
     * @return returns a string array
     */
    public int[] getCubeArray() {
        int[] outArr = new int[array.length - 1];
        for (int i = 1; i < array.length; i++) {
            outArr[i - 1] = array[i];
        }
        return outArr;
    }// returns the cube array in the papertype format, it has to chop off the 0 at the start (https://miro.medium.com/max/654/1*qipWhJUMnafaJY2u4Ix9RA.jpeg)

    public String getMin2PhaseFormat() {
        // fairly sure this works

        // Solved Input Format:  666666666555555555222222222111111111333333333444444444
        // Solved Output Format: UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB

        // U = 1 ASC, B = 2 DESC, L = 3 DESC, F = 4 DESC, R = 5 DESC, D = 6 ASC

        // U = 1 ASC, R = 5 DESC, F = 4 DESC, D = 6 ASC, L = 3 DESC, B = 2 DESC
        int[] cubeArray = getCubeArray();
        char[] outChar = new char[54];
        for (int i = 0; i < 9; i++) {
            outChar[i] = convertColourNumToLetter(cubeArray[27 + i]);
            outChar[i + 9] = convertColourNumToLetter(cubeArray[17 - i]);
            outChar[i + 18] = convertColourNumToLetter(cubeArray[53 - i]);
            outChar[i + 27] = convertColourNumToLetter(cubeArray[i]);
            outChar[i + 36] = convertColourNumToLetter(cubeArray[44 - i]);
            outChar[i + 45] = convertColourNumToLetter(cubeArray[26 - i]);
        }
        return new String(outChar);
        //return Tools.fromScramble("R R'");
    }

    public boolean getSolved() {
        int[] cubeArray = getCubeArray();
        for (int i = 0; i < solvedState.length; i++) {
            if (solvedState[i] != cubeArray[i]) return false;
        }
        return true;
    }

    private void converseChangeFaceAgain(int a1, int a2, int a3, int a4) {
        int temp = array[a4];
        array[a4] = array[a3];
        array[a3] = array[a2];
        array[a2] = array[a1];
        array[a1] = temp;
    }

    private void converseLineSetSingle(int lineFace, int p1, int p2, int c1, int c2) {
        switch (lineFace) {
            case 1:
                array[p1] = c1;
                array[p2] = c2;
                break;
            case 2:
                array[p1] = c2;
                array[p2] = c1;
                break;
            default:
                errCheck |= 1;
                Log.i(TAG, "Error, result == 3 because lineFace != 2 || lineFace != 1");
        }
    }

    private void converseLineSet(int line, int lineFace, int p1, int p2) {
        int[] vals = new int[2];
        switch (line) {
            case 1:
                vals[0] = 1;
                vals[1] = 2;
                break;
            case 2:
                vals[0] = 1;
                vals[1] = 3;
                break;
            case 3:
                vals[0] = 1;
                vals[1] = 4;
                break;
            case 4:
                vals[0] = 1;
                vals[1] = 5;
                break;
            case 5:
                vals[0] = 2;
                vals[1] = 3;
                break;
            case 6:
                vals[0] = 4;
                vals[1] = 3;
                break;
            case 7:
                vals[0] = 4;
                vals[1] = 5;
                break;
            case 8:
                vals[0] = 2;
                vals[1] = 5;
                break;
            case 9:
                vals[0] = 6;
                vals[1] = 2;
                break;
            case 10:
                vals[0] = 6;
                vals[1] = 3;
                break;
            case 11:
                vals[0] = 6;
                vals[1] = 4;
                break;
            case 12:
                vals[0] = 6;
                vals[1] = 5;
                break;
            default:
                errCheck |= 4;
        }

        if (errCheck != 4) {
            converseLineSetSingle(lineFace, p1, p2, vals[0], vals[1]);
        } else {
            Log.i(TAG, "Error in converseLineSet");
        }
    }

    private void converseAngleSetFirst(int angle, int angleFace, int f1, int f2, int f3, boolean xFirst) {
        int[] vals = new int[3];
        switch (angle) {
            case 1:
                vals[0] = 1;
                vals[1] = 2;
                vals[2] = 3;
                break;
            case 2:
                vals[0] = 1;
                vals[1] = 3;
                vals[2] = 4;
                break;
            case 3:
                vals[0] = 1;
                vals[1] = 4;
                vals[2] = 5;
                break;
            case 4:
                vals[0] = 1;
                vals[1] = 5;
                vals[2] = 2;
                break;
            case 5:
                vals[0] = 6;
                vals[1] = 3;
                vals[2] = 2;
                break;
            case 6:
                vals[0] = 6;
                vals[1] = 4;
                vals[2] = 3;
                break;
            case 7:
                vals[0] = 6;
                vals[1] = 5;
                vals[2] = 4;
                break;
            case 8:
                vals[0] = 6;
                vals[1] = 2;
                vals[2] = 5;
                break;
            default:
                errCheck |= 2;
        }
        if (errCheck != 2) {
            converseAngleSetSingleFirst(angleFace, f1, f2, f3, vals[0], vals[1], vals[2], xFirst);
        } else {
            Log.i(TAG, "Error in converseAngleSetFirst");
        }
    }

    private void converseAngleSetSingleFirst(int angleFace, int p1, int p2, int p3, int c1, int c2, int c3, boolean xFirst) {
        if (angleFace == 1 && xFirst || angleFace == 2 && !xFirst) {
            array[p1] = c3;
            array[p2] = c1;
            array[p3] = c2;
        } else if (angleFace == 2 && xFirst || angleFace == 1) {
            array[p1] = c2;
            array[p2] = c3;
            array[p3] = c1;
        } else if (angleFace == 3) {
            array[p1] = c1;
            array[p2] = c2;
            array[p3] = c3;
        } else {
            errCheck |= 1;
        }
    }

    private char convertColourNumToLetter(int num) {
        return letterConversion.get(num);
    }

    private void fillConversionHashMap() {
        letterConversion.put(1, 'U');
        letterConversion.put(2, 'B');
        letterConversion.put(3, 'L');
        letterConversion.put(4, 'F');
        letterConversion.put(5, 'R');
        letterConversion.put(6, 'D');

    }
    // https://github.com/wachino/xiaomi-mi-smart-rubik-cube/blob/4af6b16184c6a45e601e30f6c3f847ec7798d49b/src/helpers/cubeParser.js#L294

    private void cubeRawDataDecode() {
        int[] tmpArray = hexStringToIntArray("50af9820aa771389dae63f5f2e826aafa3f31407a715a8e88faf2a7d7e39fe57d95b55d7");
        if (rawData.length != 20) {
            Log.i(TAG, "Recieved Bad mixdata, length not equal to 20!");
            return;
        }
        if (rawData[18] != 167) {
            Log.i(TAG, "Recieved Bad mixdata");
            return;
        }
        int b = rawData[19];
        b &= 15;
        int b2 = rawData[19];
        b2 = (int) (b2 >> 4 & 15);
        for (int b3 = 0; b3 < 19; b3++) {
            cubeData[b3] = rawData[b3];
            int[] tmpArray3 = cubeData;
            int b4 = b3;
            tmpArray3[b4] = (int) (tmpArray3[b4] - tmpArray[b + b3]);
            int[] tmpArray4 = cubeData;
            int b5 = (int) b3;
            tmpArray4[b5] = (int) (tmpArray4[b5] - tmpArray[b2 + b3]);
        }
    } // partially taken from github, processing the cube's data from the hex input

    private void converseToPaperType() {

        if (cubeData.length != 20) {
            for (int i = 0; i < 55; i++) {
                array[i] = 0;
            }
            Log.i(TAG, "Error with cube data, returning nothing");
            return;
        }
        array2[0] = cubeData[0] >> 4 & 15;
        array2[1] = cubeData[0] & 15;
        array2[2] = cubeData[1] >> 4 & 15;
        array2[3] = cubeData[1] & 15;
        array2[4] = cubeData[2] >> 4 & 15;
        array2[5] = cubeData[2] & 15;
        array2[6] = cubeData[3] >> 4 & 15;
        array2[7] = cubeData[3] & 15;
        array3[0] = cubeData[4] >> 4 & 15;
        array3[1] = cubeData[4] & 15;
        array3[2] = cubeData[5] >> 4 & 15;
        array3[3] = cubeData[5] & 15;
        array3[4] = cubeData[6] >> 4 & 15;
        array3[5] = cubeData[6] & 15;
        array3[6] = cubeData[7] >> 4 & 15;
        array3[7] = cubeData[7] & 15;
        array4[0] = cubeData[8] >> 4 & 15;
        array4[1] = cubeData[8] & 15;
        array4[2] = cubeData[9] >> 4 & 15;
        array4[3] = cubeData[9] & 15;
        array4[4] = cubeData[10] >> 4 & 15;
        array4[5] = cubeData[10] & 15;
        array4[6] = cubeData[11] >> 4 & 15;
        array4[7] = cubeData[11] & 15;
        array4[8] = cubeData[12] >> 4 & 15;
        array4[9] = cubeData[12] & 15;
        array4[10] = cubeData[13] >> 4 & 15;
        array4[11] = cubeData[13] & 15;
        for (int b = 0; b < 12; b += 1) {
            array5[b] = 0;
        }
        if ((cubeData[14] & 128) != 0) {
            array5[0] = 2;
        } else {
            array5[0] = 1;
        }
        if ((cubeData[14] & 64) != 0) {
            array5[1] = 2;
        } else {
            array5[1] = 1;
        }
        if ((cubeData[14] & 32) != 0) {
            array5[2] = 2;
        } else {
            array5[2] = 1;
        }
        if ((cubeData[14] & 16) != 0) {
            array5[3] = 2;
        } else {
            array5[3] = 1;
        }
        if ((cubeData[14] & 8) != 0) {
            array5[4] = 2;
        } else {
            array5[4] = 1;
        }
        if ((cubeData[14] & 4) != 0) {
            array5[5] = 2;
        } else {
            array5[5] = 1;
        }
        if ((cubeData[14] & 2) != 0) {
            array5[6] = 2;
        } else {
            array5[6] = 1;
        }
        if ((cubeData[14] & 1) != 0) {
            array5[7] = 2;
        } else {
            array5[7] = 1;
        }
        if ((cubeData[15] & 128) != 0) {
            array5[8] = 2;
        } else {
            array5[8] = 1;
        }
        if ((cubeData[15] & 64) != 0) {
            array5[9] = 2;
        } else {
            array5[9] = 1;
        }
        if ((cubeData[15] & 32) != 0) {
            array5[10] = 2;
        } else {
            array5[10] = 1;
        }
        if ((cubeData[15] & 16) != 0) {
            array5[11] = 2;
        } else {
            array5[11] = 1;
        }

        // center pieces
        array[32] = 1;
        array[41] = 2;
        array[50] = 3;
        array[14] = 4;
        array[23] = 5;
        array[5] = 6;

        converseAngleSetFirst(array2[0], array3[0], 34, 43, 54, true);
        converseAngleSetFirst(array2[1], array3[1], 36, 52, 18, false);
        converseAngleSetFirst(array2[2], array3[2], 30, 16, 27, true);
        converseAngleSetFirst(array2[3], array3[3], 28, 25, 45, false);
        converseAngleSetFirst(array2[4], array3[4], 1, 48, 37, false);
        converseAngleSetFirst(array2[5], array3[5], 3, 12, 46, true);
        converseAngleSetFirst(array2[6], array3[6], 9, 21, 10, false);
        converseAngleSetFirst(array2[7], array3[7], 7, 39, 19, true);
        converseLineSet(array4[0], array5[0], 31, 44);
        converseLineSet(array4[1], array5[1], 35, 53);
        converseLineSet(array4[2], array5[2], 33, 17);
        converseLineSet(array4[3], array5[3], 29, 26);
        converseLineSet(array4[4], array5[4], 40, 51);
        converseLineSet(array4[5], array5[5], 15, 49);
        converseLineSet(array4[6], array5[6], 13, 24);
        converseLineSet(array4[7], array5[7], 42, 22);
        converseLineSet(array4[8], array5[8], 4, 38);
        converseLineSet(array4[9], array5[9], 2, 47);
        converseLineSet(array4[10], array5[10], 6, 11);
        converseLineSet(array4[11], array5[11], 8, 20);
        converseChangeFaceAgain(1, 7, 9, 3);
        converseChangeFaceAgain(4, 8, 6, 2);
        converseChangeFaceAgain(37, 19, 10, 46);
        converseChangeFaceAgain(38, 20, 11, 47);
        converseChangeFaceAgain(39, 21, 12, 48);
        converseChangeFaceAgain(40, 22, 13, 49);
        converseChangeFaceAgain(41, 23, 14, 50);
        converseChangeFaceAgain(42, 24, 15, 51);
        converseChangeFaceAgain(43, 25, 16, 52);
        converseChangeFaceAgain(44, 26, 17, 53);
        converseChangeFaceAgain(45, 27, 18, 54);
        converseChangeFaceAgain(34, 28, 30, 36);
        converseChangeFaceAgain(31, 29, 33, 35);
        if (errCheck != 0) {
            for (int j = 0; j < 55; j++) {
                array[j] = 0;
            }
            Log.i(TAG, "Error: errCheck == %s");
        }
    } /*

    this code has been converted (and improved a little bit) from javascript, it's spaghetti but it was worse
    look here for the original: https://github.com/wachino/xiaomi-mi-smart-rubik-cube/blob/master/src/helpers/cubeParser.js
    I'd much rather use some library for this but none exists :(
    */

    public int[] getCubeletPositions() {
        int[] cubeletPositionsOut = new int[27];
        int[][] currentCubeletPositions = stateToCubelets();
        for (int i = 0; i < 27; i++) {
            cubeletPositionsOut[i] = getCubeIDFromColours(currentCubeletPositions[i]);
        }
        return cubeletPositionsOut;
    }

    private int getCubeIDFromColours(int[] cubelet) { // returns the cube ID from the colours on it
        //if(checkCenter(cubelet)) return 13;// the center must be 13, it breaks the checkArr colours
        for (int i = 0; i < 27; i++) {
            if (checkArrColours(standardCubeFormat[i], cubelet)) return i;
        }
        return -1;// only runs if the colours are invalid
    }

    private boolean checkCenter(int[] arr) { // checks whether or not the cubelet is in the center, used to get the cube ID
        for (int i = 0; i < 6; i++) {
            if (arr[i] != 0) return false;
        }
        return true;
    }
}