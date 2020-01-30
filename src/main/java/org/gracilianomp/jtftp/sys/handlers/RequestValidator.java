package org.gracilianomp.jtftp.sys.handlers;


import org.gracilianomp.jtftp.sys.error.ErrorFileAlreadyExist;
import org.gracilianomp.jtftp.sys.error.ErrorFileNotFound;

import java.io.File;

abstract public class RequestValidator {

    static public void validateReadFile(File file) throws ErrorFileNotFound {
        if (!file.exists()) throw new ErrorFileNotFound(file);
    }

    static public void validateWriteFile(File file) throws ErrorFileAlreadyExist {
        if (file.exists()) throw new ErrorFileAlreadyExist(file) ;
    }

}
