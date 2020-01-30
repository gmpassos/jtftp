package org.gracilianomp.jtftp.sys.error;

public class ErrorUndefined extends TFTPError {

    public ErrorUndefined(String errorMSG) {
        super(new byte[]{0, 0}, errorMSG);
    }

}
