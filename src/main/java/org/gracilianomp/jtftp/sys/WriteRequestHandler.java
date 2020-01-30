package org.gracilianomp.jtftp.sys;


import org.gracilianomp.jtftp.sys.error.ErrorAccessViolation;
import org.gracilianomp.jtftp.sys.error.ErrorDiskFull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.slf4j.LoggerFactory.getLogger;

public class WriteRequestHandler {

    private static final Logger LOGGER = getLogger(WriteRequestHandler.class);

    final private ArrayList<TFTPDataPacket> receivedPackets = new ArrayList<>() ;

    /**
     * This method will handle more or less everything that has to do with incoming
     * data packets from the client. It takes the entire packet from the client as
     * an argument and parse it to an TFTPDataPacket object and stores all incoming
     * packets from the client in an array list. It will always return an ack of the
     * last packet that arrived in order, the only way it will not is if it is not
     * an data packet passed as an argument or
     *
     * @param filePackageBlock the entire data packet received from the client.
     * @return ACK from last received packed in order if nothing that was to faulty
     * was passed as argument
     * @throws IllegalStateException and IllegalArgumentException in case there was
     *                               too little data (less than 4 bytes) or the wrong opcode.
     */
    public ACK receivePacketAndACK(byte[] filePackageBlock) {
        TFTPDataPacket packet = TFTPDataPacket.parse(filePackageBlock);

        LOGGER.info("Received block: {} > receivedPackets: {}", packet, receivedPackets.size());

        // The block nr of the data packet was zero:
        if (packet == null) {
            // Return ACK for last packet received:
            return getACKForLastPacketReceived() ;
        }
        // An actual data packet:
        else {
            if ( receivedPackets.isEmpty() ) {
                if (packet.getBlockNr() == 1) {
                    LOGGER.info("1st block appended: #{}", packet.getBlockNr());
                    receivedPackets.add(packet);
                    ACK ack = getACKForLastPacketReceived();
                    LOGGER.info("Generated ACK: #{}", ack.getBlockNr());
                    return ack;
                }
                // Else return null (There is no packet we can acknowledge):
                else {
                    return null;
                }
            }
            /* If the array is empty, we should receive the first packet.*/
            else {
                if (getLastReceivedPacketBlockNr()+1 == packet.getBlockNr()) { // Make sure packets are successive
                    LOGGER.info("Block appended: #{}", packet.getBlockNr());
                    receivedPackets.add(packet) ;
                }
                ACK ack = getACKForLastPacketReceived();
                LOGGER.info("Generated ACK: #{}", ack.getBlockNr());
                return ack;
            }
        }
    }

    private ACK getACKForLastPacketReceived() {
        TFTPDataPacket lastReceived = getLastReceivedPacket();
        return lastReceived != null ? new ACK( lastReceived.getBlockNr() ) : null ;
    }

    private TFTPDataPacket getLastReceivedPacket() {
        if (receivedPackets.isEmpty()) return null ;
        return receivedPackets.get(receivedPackets.size() - 1);
    }

    private int getLastReceivedPacketBlockNr() {
        TFTPDataPacket lastReceived = getLastReceivedPacket();
        return lastReceived != null ? lastReceived.getBlockNr() : -1 ;
    }

    public boolean isLastPacketReceived() {
        TFTPDataPacket lastReceived = getLastReceivedPacket();
        if (lastReceived == null) return false ;
        // The last packet is always less than 512, not full:
        boolean fullBlock = lastReceived.isFullBlock();
        return !fullBlock;
    }

    /**
     * This method writes the content of the file to the specified path and the content of the data.
     *
     * @param file File to write data.
     * @throws IOException
     */

    public void writePacketsToFile(File file) throws Exception {
        if ( !isLastPacketReceived() ) throw new ErrorAccessViolation() ;

        byte[] fullData = getRawDataFromPackets();

        File parentFile = file.getParentFile();
        if (parentFile == null) parentFile = new File("/");

        Long diskspace = parentFile.getFreeSpace();

        if (diskspace < fullData.length) {
            throw new ErrorDiskFull();
        }

        System.out.println("-- Writing data["+ fullData.length +"] to file: "+ file) ;

        writeToFile(file, fullData);

    }

    private void writeToFile(File file, byte[] rawData) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(rawData);
        outputStream.close();
    }

    /**
     * Help method that retrieves only the data section of all packets in order and
     * assembly them in to an single array.
     *
     * @return an byte array of all received data packets.
     */
    private byte[] getRawDataFromPackets() {
        ArrayList<byte[]> dataOnlyArrs = new ArrayList<byte[]>();
        int arrLength = 0;
        for (TFTPDataPacket packet : receivedPackets) {
            dataOnlyArrs.add(packet.getContent());
            arrLength += packet.getContent().length;
            //System.out.println(arrLength);
        }
        byte[] out = new byte[arrLength];
        for (int i = 0; i < dataOnlyArrs.size(); i++) {
            byte[] temp = dataOnlyArrs.get(i);
            //System.out.println(new String(temp) + "   " + temp.length);
            for (int k = 0; k < temp.length; k++) {
                int outIndex = TFTPDataPacket.MAX_BLOCK_SIE * i + k;
                //System.out.println(outIndex);
                out[outIndex] = temp[k];
            }
        }
        return out;
    }


}
