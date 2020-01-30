package org.gracilianomp.jtftp.utils;

public class UnsignedHelper {

    static final public int INT_TO_UNSIGNED_BYTES_UPPER_BOUND = (int) Math.round(Math.pow(2, 16) - 1);

    /**
     * This method receives an signed int and converts it to the equivalent
     * unsigned byte values of the two least significant bytes of the int.
     *
     * @param n an signed integer value 0 <= val <= 2^16 - 1
     * @return byte[] containing the unsigned equivalent of the entered
     * value of the integer to the method. The least significant bits in
     * index 1 and the most significant bits in index 0.
     */
    public synchronized static byte[] intTo2UnsignedBytes(int n) {
        if (n < 0 || n > INT_TO_UNSIGNED_BYTES_UPPER_BOUND) {
            throw new IllegalArgumentException("n out of range 0-"+INT_TO_UNSIGNED_BYTES_UPPER_BOUND+": "+ n);
        }

        int b0 = n / 256 ;
        int b1 = (n % 256) ;

        byte[] bs = new byte[2];
        bs[0] = (byte) b0 ;
        bs[1] = (byte) b1 ;
        return bs;
    }

    /**
     * Returns the signed integer value of an 16-bit binary number
     *
     * @param bs the 16-bit unsigned value
     * @return the same value expressed as an 32-bit signed integer
     */
    public synchronized static int twoBytesToInt(byte[] bs) {
        return twoBytesToInt(bs[0], bs[1]);
    }

    public synchronized static int twoBytesToInt(byte b0, byte b1) {
        int first = Byte.toUnsignedInt(b0);
        int second = Byte.toUnsignedInt(b1);
        int n = first * 256 + second ;
        return n ;
    }


}
