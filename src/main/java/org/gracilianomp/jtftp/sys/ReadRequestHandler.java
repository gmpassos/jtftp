package org.gracilianomp.jtftp.sys;


import org.gracilianomp.jtftp.sys.TFTPDataPacket;
import org.gracilianomp.jtftp.sys.error.ErrorUndefined;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class ReadRequestHandler {

    static public ArrayList<TFTPDataPacket> generatePacketsFromFile(File file) throws Exception {
        byte[] fileData = Files.readAllBytes( file.toPath() ) ;

        if ( fileData.length > TFTPDataPacket.MAX_DATA_SIZE ) {
            throw new ErrorUndefined("Requested file too large: "+ file +" > "+ fileData.length +" > "+ TFTPDataPacket.MAX_DATA_SIZE);
        }

        return generatePackets(fileData);
    }

    static private ArrayList<TFTPDataPacket> generatePackets(byte[] data) {
        // Total number of packets to encapsulate data:
        int noPackets = data.length / TFTPDataPacket.MAX_BLOCK_SIE + 1;

        ArrayList<TFTPDataPacket> packets = new ArrayList<>(noPackets);

        // Generate all packets that are always full (512 bytes): [Not generating last packet]
        for (int i = 0; i < noPackets-1; i++) {
            int blockNr = i + 1;

            byte[] blockData = new byte[TFTPDataPacket.MAX_BLOCK_SIE];
            int blockOffset = i * TFTPDataPacket.MAX_BLOCK_SIE;

            System.arraycopy(data, blockOffset, blockData, 0, blockData.length);

            TFTPDataPacket toAdd = new TFTPDataPacket(blockData, blockNr);
            packets.add(toAdd);
        }

        // Generate last packet, since it can have less than 512 bytes:
        {
            int blockOffset = TFTPDataPacket.MAX_BLOCK_SIE * (noPackets - 1);
            int blockLength = data.length - blockOffset;

            byte[] blockData = new byte[blockLength];

            System.arraycopy(data, blockOffset, blockData, 0, blockData.length);

            TFTPDataPacket lastBlock = new TFTPDataPacket(blockData, noPackets);
            packets.add(lastBlock);
        }

        return packets;
    }


}
