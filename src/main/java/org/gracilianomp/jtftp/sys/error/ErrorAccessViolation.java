package org.gracilianomp.jtftp.sys.error;

public class ErrorAccessViolation extends TFTPError {

    public ErrorAccessViolation() {
        super(new byte[]{0, 2}, "Access violation.");
    }

}
