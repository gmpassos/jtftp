package org.gracilianomp.jtftp.sys.error;

import java.io.File;

public class ErrorFileAlreadyExist extends TFTPError {

    public ErrorFileAlreadyExist(File file) {
        super(new byte[]{0, 6}, "File already exist:"+ file);
    }

}