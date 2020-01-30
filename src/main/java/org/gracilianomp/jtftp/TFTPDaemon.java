package org.gracilianomp.jtftp;

import org.gracilianomp.jtftp.sys.TFTPDataPacket;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.*;

import static org.slf4j.LoggerFactory.getLogger;

public class TFTPDaemon {

    private static final Logger LOGGER = getLogger(TFTPDaemon.class);
    private static final double VERSION = 1.0 ;

    final private int port;
    private final File directory;

    public TFTPDaemon(int port, File directory) throws IOException {
        this.port = port;
        this.directory = directory;

        open();
    }

    public int getPort() {
        return port;
    }

    public File getDirectory() {
        return directory;
    }

    private DatagramSocket datagramSocket;

    private void open() throws IOException {
        datagramSocket = new DatagramSocket(null);
        SocketAddress socketAddress = new InetSocketAddress("0.0.0.0", port);
        LOGGER.info("Binding to port: {}", socketAddress);

        datagramSocket.bind(socketAddress);
        datagramSocket.setReuseAddress(true);

        new Thread(this::acceptLoop).start();
    }

    private void acceptLoop() {

        LOGGER.info("Running TFTPDaemon at port {} and directory {}", port , directory);

        byte[] buffer = new byte[4 + TFTPDataPacket.MAX_BLOCK_SIE];

        while (true) {
            try {
                DatagramPacket requestPackage = new DatagramPacket(buffer, buffer.length);
                LOGGER.info("Accepting initial packet...");

                datagramSocket.receive(requestPackage);

                LOGGER.info("Received initial packet: {}", requestPackage);

                new TFTPClientHandler(this, requestPackage);
            }
            catch (IOException e) {
                LOGGER.error("Error receiving initial packet", e);
            }
        }
    }

    private static void showHelp() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("| TFTPDaemon: "+ VERSION );
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("USAGE:");
        System.out.println();
        System.out.println("  - JAVA command line: ");
        System.out.println("    $> java "+ TFTPDaemon.class.getName() +" %port %directory") ;
        System.out.println();
        System.out.println("  - Gradle command line: ");
        System.out.println("    $> ./gradlew run --args \"%port %directory\"");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println();
        System.out.println("    $> ./gradlew run --args \"69 /tmp/tftp\"");
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            showHelp();
            System.exit(0);
        }

        int port = Integer.parseInt( args[0] ) ;
        File dir = new File(args[1]);
        dir.mkdirs();

        new TFTPDaemon(port, dir);
    }

}
