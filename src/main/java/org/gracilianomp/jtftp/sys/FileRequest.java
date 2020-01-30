package org.gracilianomp.jtftp.sys;

import java.io.File;

public abstract class FileRequest {

    private int opCode;
    private String fileName;

    public FileRequest(int opc, String fName) {
        opCode = opc;
        fileName = fName;
    }

    public int getOpCode() {
        return opCode;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile(File parentFile) {
        return new File(parentFile, fileName);
    }

    public boolean isReadRequest() {
        return opCode == 1;
    }

    public boolean isWriteRequest() {
        return opCode == 2;
    }

    @Override
    public String toString() {
        return "FileRequest{" +
                "opCode=" + opCode +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
