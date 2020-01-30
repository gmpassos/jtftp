package org.gracilianomp.jtftp.sys.error;

public class ErrorNoSuchUser extends TFTPError {

    public ErrorNoSuchUser() {
        super(new byte[]{0, 7}, "No such user");
    }

}
