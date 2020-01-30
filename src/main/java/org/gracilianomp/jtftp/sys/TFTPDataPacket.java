package org.gracilianomp.jtftp.sys;


import org.gracilianomp.jtftp.utils.UnsignedHelper;

import java.nio.charset.StandardCharsets;

public class TFTPDataPacket {

    public final static int HEADER_SIZE = 4  ;

    public final static int MAX_BLOCK_NR = 65535  ;
    public final static int MAX_BLOCK_SIE = 512  ;

    /**
     * Maximal number of blocks: 65535
     * Maximal size of block: 512
     * Maximal File data: 65535*512: 32MB
     */
    public final static int MAX_DATA_SIZE = MAX_BLOCK_NR * MAX_BLOCK_SIE ;

    //////////////////////////////////////////////////////

    static public TFTPDataPacket parse(byte[] bytes) {
        if (bytes.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Packet length < "+HEADER_SIZE+": "+ bytes.length);
        }

        //int opCode = bytes[0] + bytes[1];
        int opCode = UnsignedHelper.twoBytesToInt(bytes[0], bytes[1]) ;

        if (opCode != 3) throw new IllegalStateException("Packet opCode is not a data packet: " + opCode);

        int blockNr = UnsignedHelper.twoBytesToInt(new byte[]{bytes[2], bytes[3]});

        //Data packets start from 1:
        if (blockNr == 0) {
            //Shouldn't throw exception here, might be that the client sent packet 2 before 1.
            return null;
        }

        byte[] data = new byte[bytes.length - HEADER_SIZE];
        System.arraycopy(bytes, HEADER_SIZE, data, 0, data.length);

        return new TFTPDataPacket(data, blockNr);
    }

    /////////////////////////////////////////////////////

    static final private byte[] OP_CODE = new byte[] {0, 3} ;

    private int blockNr;
    private byte[] packet;

    public TFTPDataPacket(byte[] data, int blockNr) {
        if (data.length > MAX_BLOCK_SIE) throw new IllegalArgumentException("Packet data length maximum size is "+ MAX_BLOCK_SIE +", received: " + data.length);
        if (blockNr < 1 || blockNr > MAX_BLOCK_NR) throw new IllegalArgumentException("Block number out of range 1-"+MAX_BLOCK_NR+": "+ blockNr);

        this.blockNr = blockNr;
        packet = new byte[HEADER_SIZE + data.length];
        packet[0] = OP_CODE[0] ;
        packet[1] = OP_CODE[1] ;

        byte[] blockNrBs = UnsignedHelper.intTo2UnsignedBytes(blockNr);
        packet[2] = blockNrBs[0];
        packet[3] = blockNrBs[1];

        System.arraycopy(data, 0, packet, HEADER_SIZE, data.length);
    }

    public byte[] getContent() {
        int dataLength = getDataLength();
        byte[] out = new byte[dataLength];
        System.arraycopy(packet, HEADER_SIZE, out, 0, dataLength);
        return out;
    }

    public int getDataLength() {
        return packet.length - HEADER_SIZE;
    }

    public boolean isFullBlock() {
        int dataLength = getDataLength();
        assert( dataLength >= 0 && dataLength <= MAX_BLOCK_SIE ) ;
        return dataLength == MAX_BLOCK_SIE ;
    }

    public String getContentAsString() {
        return new String(packet, HEADER_SIZE, packet.length-HEADER_SIZE, StandardCharsets.ISO_8859_1) ;
    }

    public byte[] toSerial() {
        return packet;
    }

    public int getBlockNr() {
        return blockNr;
    }

    public String toString() {
        String out = "{";
        out += "Opcode: " + packet[0] + "," + packet[1];
        out += ", blockNr: " + blockNr;
        out += ", dataLength: " + getDataLength() ;
        out += "}" ;
        return out;
    }

}
