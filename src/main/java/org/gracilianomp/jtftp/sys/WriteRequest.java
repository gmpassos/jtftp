package org.gracilianomp.jtftp.sys;

public class WriteRequest extends FileRequest {
    public final static int OP_CODE_WRITE = 2;

    public WriteRequest(String fileName) {
        super(OP_CODE_WRITE, fileName);
    }

}
