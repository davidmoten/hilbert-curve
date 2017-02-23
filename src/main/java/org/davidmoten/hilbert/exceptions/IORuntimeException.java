package org.davidmoten.hilbert.exceptions;

import java.io.IOException;

public class IORuntimeException extends RuntimeException {

    private static final long serialVersionUID = -2505330669950461931L;
    
    public IORuntimeException(IOException e) {
        super(e);
    }

}
