package org.gracilianomp.jtftp.sys.error;

public class ErrorDiskFull extends TFTPError {

    public ErrorDiskFull() {
        super(new byte[]{0, 3}, "Disk is full or the allocation is exceeded");
    }

}
