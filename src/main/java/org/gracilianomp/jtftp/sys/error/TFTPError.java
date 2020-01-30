package org.gracilianomp.jtftp.sys.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

abstract public class TFTPError extends IOException {

    final private static byte[] OP_CODE = new byte[]{0, 5};

    private byte[] errorCode;
    private String errorMessage;

    public TFTPError(byte[] code, String message) {
        super(message);

        this.errorCode = code;
        this.errorMessage = message;
    }

    public byte[] toSerial() {
        byte[] msgBytes = this.errorMessage.getBytes( StandardCharsets.ISO_8859_1 );

        // +2: opCode
        // +2: errorCode
        // +1: end of string
        byte[] out = new byte[ 4 + msgBytes.length + 1 ];

        out[0] = OP_CODE[0];
        out[1] = OP_CODE[1];
        out[2] = errorCode[0];
        out[3] = errorCode[1];

        System.arraycopy(msgBytes, 0, out, 4, msgBytes.length);

        return out;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

}
