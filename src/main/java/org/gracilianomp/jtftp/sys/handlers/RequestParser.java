package org.gracilianomp.jtftp.sys.handlers;

import org.gracilianomp.jtftp.sys.FileRequest;
import org.gracilianomp.jtftp.sys.ReadRequest;
import org.gracilianomp.jtftp.sys.WriteRequest;
import org.gracilianomp.jtftp.sys.error.ErrorFileNotFound;
import org.gracilianomp.jtftp.sys.error.ErrorIllegalOperation;
import org.gracilianomp.jtftp.utils.ArrayUtils;
import org.gracilianomp.jtftp.utils.UnsignedHelper;

import java.io.File;
import java.net.DatagramPacket;

public class RequestParser {

    static public FileRequest parse(DatagramPacket requestPackage, File directory) throws Exception {
        byte[] packetData = requestPackage.getData();

        //int opCode = packetData[0] + packetData[1];
        int opCode = UnsignedHelper.twoBytesToInt(packetData) ;

        System.out.println("Received opCode: " + opCode);

        String fileName = getFilenameAndVerifyMode(requestPackage.getData(), 2);

        System.out.println("Received fileName: " + fileName);

        if (opCode == 1) {
            File requestedFile = new File(directory, fileName);
            if (!requestedFile.exists()) throw new ErrorFileNotFound(requestedFile);

            System.out.println("Does the file exist? " + requestedFile.exists());
            System.out.println("Returning new read request");
            return new ReadRequest(fileName);
        }
        else if (opCode == 2) {
            return new WriteRequest(fileName);
        }
        else {
            throw new ErrorIllegalOperation("Invalid opCode: "+ opCode);
        }

    }

    private static String getFilenameAndVerifyMode(byte[] buffer, int offset) throws Exception {

        int msgEndIdx = ArrayUtils.indexOf(buffer, offset, (byte)0) ;
        if (msgEndIdx < 0) throw new IllegalStateException("Can't find message end: \\0") ;

        String filename = new String( buffer, offset, msgEndIdx - offset) ;

        if (filename.isEmpty()) {
            throw new ErrorIllegalOperation("Empty filename: "+ filename);
        }

        int modeEndIdx = ArrayUtils.indexOf(buffer, msgEndIdx +1, (byte)0) ;
        if (modeEndIdx < 0) throw new IllegalStateException("Can't find mode end: \\0") ;

        String mode = new String( buffer, msgEndIdx +1, modeEndIdx -(msgEndIdx +1) ) ;

        if (!mode.equalsIgnoreCase("octet")) {
            throw ErrorIllegalOperation.unsupportedMode(mode, filename) ;
        }

        return filename;
    }

}
