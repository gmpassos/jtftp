package org.gracilianomp.jtftp.utils;

public class ArrayUtils {

    static public int indexOf(byte[] bs, byte v ) {
        return indexOf(bs, 0, bs.length, v) ;
    }

    static public int indexOf(byte[] bs, int offset, byte v ) {
        return indexOf(bs, offset, bs.length-offset, v) ;
    }

    static public int indexOf(byte[] bs, int offset, int length, byte v ) {
        int limit = offset+length ;
        for (int i = offset; i < limit; i++) {
            if (bs[i] == v) return i ;
        }
        return -1 ;
    }

}
