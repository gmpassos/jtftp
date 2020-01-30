package org.gracilianomp.jtftp.sys.error;

import java.io.File;

public class ErrorFileNotFound extends TFTPError {

    public ErrorFileNotFound(File file) {
        super(new byte[]{0, 1}, "File not found: "+ file);
    }

}
