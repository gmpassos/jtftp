package org.gracilianomp.jtftp.sys;

import org.gracilianomp.jtftp.utils.UnsignedHelper;

public class ACK {
    static final protected byte[] OP_CODE = {0, 4};

    static public boolean isValidOpCode(byte[] bs, int offset) {
        return bs[offset] == OP_CODE[0] && bs[offset+1] == OP_CODE[1] ;
    }

    private byte[] blockNrBs;
    private int blockNr;

    public ACK(int blockNr) {
        this.blockNr = blockNr;
        this.blockNrBs = UnsignedHelper.intTo2UnsignedBytes(blockNr);
    }

    public ACK(byte b0, byte b1) {
        this.blockNrBs = new byte[] {b0, b1};
        this.blockNr = UnsignedHelper.twoBytesToInt(blockNrBs);
    }

    public int getBlockNr() {
        return blockNr;
    }

    public String toString() {
        return "ACK#" + blockNr;
    }

    public byte[] toSerial() {
        byte[] out = new byte[4];
        out[0] = OP_CODE[0];
        out[1] = OP_CODE[1];
        out[2] = blockNrBs[0];
        out[3] = blockNrBs[1];
        return out;
    }

}
