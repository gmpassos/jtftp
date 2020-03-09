package org.gracilianomp.jtftp;

import org.gracilianomp.jtftp.sys.*;
import org.gracilianomp.jtftp.sys.error.ErrorUndefined;
import org.gracilianomp.jtftp.sys.error.TFTPError;
import org.gracilianomp.jtftp.sys.handlers.ACKParser;
import org.gracilianomp.jtftp.sys.handlers.RequestParser;
import org.gracilianomp.jtftp.sys.handlers.RequestValidator;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.slf4j.LoggerFactory.getLogger;

class TFTPClientHandler extends Thread {

    private static final Logger LOGGER = getLogger(TFTPClientHandler.class);

    static final private Random random = new Random();

    private static int randomPort() {
        int min = 65000;
        int max = 65534;
        int bound = max - min + 1;

        synchronized (random) {
            return random.nextInt(bound) + min;
        }
    }

    //////////////////////////////////////////////////

    private final TFTPDaemon tftpDaemon;
    private final DatagramPacket requestPacket;
    private final String requestHost;

    private final SocketAddress remoteAddress;

    public TFTPClientHandler(TFTPDaemon tftpDaemon, DatagramPacket requestPacket) {
        this.tftpDaemon = tftpDaemon;
        this.requestPacket = requestPacket;
        this.remoteAddress = requestPacket.getSocketAddress();
        this.requestHost = remoteAddress.toString();

        start();
    }

    @Override
    public void run() {
        try {
            FileRequest clientRequest = RequestParser.parse(requestPacket, tftpDaemon.getDirectory());

            DatagramSocket datagramSocket = new DatagramSocket(null);
            SocketAddress boundAddress = bindPort(datagramSocket, 1000);

            LOGGER.info("{}> Client  bound to: {}", clientRequest, boundAddress);

            if (clientRequest.isReadRequest()) {
                processRequestRead(datagramSocket, clientRequest);
            }
            else if (clientRequest.isWriteRequest()) {
                processRequestWrite(datagramSocket, clientRequest);
            }
        }
        catch (TFTPError e) {
            LOGGER.error("TFTPError processing request. Sending error to client...", e);
            this.sendErrorToClient(e);
        }
        catch (Exception e) {
            LOGGER.error("Unknown error processing request. Sending error to client...", e);
            ErrorUndefined undefinedError = new ErrorUndefined(e.getMessage());
            this.sendErrorToClient(undefinedError);
        }
    }

    /**
     * Assign a port that is not occupied
     *
     * @param datagramSocket The DatagramSocket
     * @return Bound SocketAddress
     */
    private SocketAddress bindPort(DatagramSocket datagramSocket, int maxRetries) {
        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                int randomPort = randomPort();
                SocketAddress socketAddress = new InetSocketAddress("0.0.0.0", randomPort);
                datagramSocket.bind(socketAddress);
                return socketAddress;
            } catch (SocketException ignore) { }
        }

        throw new IllegalStateException("Can't bind port to DatagramSocket: " + datagramSocket);
    }

    private void processRequestRead(DatagramSocket datagramSocket, FileRequest fileRequest) throws Exception {
        LOGGER.info("** Read Request from host: {}", requestHost);

        File file;
        if(fileRequest.getFileName() == tftpDaemon.getFile().getName()){
            file = tftpDaemon.getFile();
        }else{
            file = new File(tftpDaemon.getDirectory(), fileRequest.getFileName());
        }


        LOGGER.info("-- Reading file: {}", file);

        RequestValidator.validateReadFile(file);

        LOGGER.info("** Sending file: {}", file);

        datagramSocket.setSoTimeout(2000);

        ArrayList<TFTPDataPacket> packets = ReadRequestHandler.generatePacketsFromFile(file);

        boolean sentAllPackets = true;
        for (TFTPDataPacket packet : packets) {
            boolean sent = sendPacket(datagramSocket, packet);
            if (!sent) {
                LOGGER.info("!! Can't send packet  #{}. Aborting!", packet.getBlockNr());
                sentAllPackets = false;
                break;
            }
        }

        LOGGER.info("** Sent file> sentAllPackets: {} ; {}", sentAllPackets, file);
        if(tftpDaemon.getKillAfterSendFile() == true){
            System.exit(0);
        }

    }

    private boolean sendPacket(DatagramSocket datagramSocket, TFTPDataPacket packet) throws IOException {
        LOGGER.info("Sending packet #{}", packet.getBlockNr());

        int maxRetries = 5;

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                byte[] serial = packet.toSerial();
                DatagramPacket datagramPacket = new DatagramPacket(serial, serial.length, remoteAddress);

                datagramSocket.send(datagramPacket);
                // Wait for ACK from client
                datagramSocket.receive(requestPacket);

                ACK ack = ACKParser.parse(requestPacket);

                if (ack.getBlockNr() == packet.getBlockNr()) {
                    LOGGER.info("Received packet ACK #{}", ack.getBlockNr());
                    return true;
                }
                else {
                    throw new ErrorUndefined("Received ACK with wrong blockNr: "+ ack.getBlockNr()) ;
                }
            }
            catch (SocketTimeoutException e) {
                LOGGER.error("Timeout occurred> retry: "+ retry, e);
            }
        }

        LOGGER.error("Max retries reached. Stop sending!") ;

        return false ;
    }

    private void processRequestWrite(DatagramSocket datagramSocket, FileRequest fileRequest) throws Exception {
        LOGGER.info("Requested Write from host: {}", requestHost);

        File file = new File(tftpDaemon.getDirectory(), fileRequest.getFileName());

        LOGGER.info("Writing file: {}", file);

        RequestValidator.validateWriteFile(file);

        byte[] firstACK = new byte[4];
        firstACK[0] = 0;
        firstACK[1] = 4;
        firstACK[2] = 0;
        firstACK[3] = 0;

        DatagramPacket firstACKPacket = new DatagramPacket(firstACK, firstACK.length, remoteAddress);
        datagramSocket.send(firstACKPacket);

        datagramSocket.setSoTimeout(2000);  // Set timeout to 2 sec

        WriteRequestHandler dataPacketHandler = new WriteRequestHandler();

        while ( !dataPacketHandler.isLastPacketReceived() ) {
            boolean ok = receiveFilePackage(datagramSocket, dataPacketHandler, 5);
            if (!ok) {
                throw new ErrorUndefined("Error receiving file: "+ file) ;
            }
        }

        if ( dataPacketHandler.isLastPacketReceived() ) {
            dataPacketHandler.writePacketsToFile(file);
        }
        else {
            LOGGER.error("Final file block not received: "+ file);
        }

    }

    private boolean receiveFilePackage(DatagramSocket datagramSocket, WriteRequestHandler dataPacketHandler, int maxRetries) {

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                datagramSocket.receive(requestPacket);

                // If last packet, copy it to a new array.
                byte[] receivedData;
                if (requestPacket.getLength() < 516) {
                    receivedData = Arrays.copyOf(requestPacket.getData(), requestPacket.getLength());
                } else {
                    receivedData = requestPacket.getData();
                }

                // Get ACK
                ACK ackToClient = dataPacketHandler.receivePacketAndACK(receivedData);

                // Send ACK back to client
                byte[] ackBytes = ackToClient.toSerial();
                DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, remoteAddress);

                LOGGER.info("Sending block ACK #{}", ackToClient.getBlockNr());
                datagramSocket.send(ackPacket);

                return true ;
            }
            catch (IOException e) {
                LOGGER.error("Error receiving file package", e);
            }
        }

        return false ;
    }

    private boolean sendErrorToClient(TFTPError errorPacket) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(null);
            byte[] errorSerial = errorPacket.toSerial();

            DatagramPacket sendErrorPacket = new DatagramPacket(errorSerial, errorSerial.length, remoteAddress);
            datagramSocket.send(sendErrorPacket);

            return true;
        } catch (IOException e) {
            LOGGER.error("Error sending TFTPError to client: "+ errorPacket, e);
            return false;
        }
    }

}