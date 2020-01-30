package org.gracilianomp.jtftp.sys.handlers;

import org.gracilianomp.jtftp.sys.ACK;

import java.net.DatagramPacket;

public class ACKParser {

    static public ACK parse(DatagramPacket recvDatagramPacket) throws IllegalStateException {
        byte[] data = recvDatagramPacket.getData();

        if ( !ACK.isValidOpCode(data,0) ) {
            throw new IllegalStateException("Wrong ACK opCode: " + data[0] + ", " + data[1]);
        }

        return new ACK(data[2], data[3]);
    }

}
