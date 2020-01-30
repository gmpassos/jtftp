package org.gracilianomp.jtftp.sys.error;

public class ErrorInvalidTransferID extends TFTPError {

    public ErrorInvalidTransferID() {
        super(new byte[]{0, 5}, "Invalid transfer id");
    }

}
