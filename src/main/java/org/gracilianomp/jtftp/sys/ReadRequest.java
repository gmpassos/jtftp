package org.gracilianomp.jtftp.sys;


public class ReadRequest extends FileRequest {
    public final static int OP_CODE_READ = 1;

    public ReadRequest(String fileName) {
        super(OP_CODE_READ, fileName);
    }

}
