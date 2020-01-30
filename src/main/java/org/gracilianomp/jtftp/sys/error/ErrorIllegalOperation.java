package org.gracilianomp.jtftp.sys.error;

public class ErrorIllegalOperation extends TFTPError {

    static public ErrorIllegalOperation unsupportedMode(String mode, String filename) {
        return new ErrorIllegalOperation("Only supporting octet mode! Requested mode: "+ mode +" ; filename: "+ filename) ;
    }

    public ErrorIllegalOperation(String message) {
        super(new byte[]{0, 4}, "Operation not allowed: "+ message);
    }

}
