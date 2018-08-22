package org.oddjob.jmx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketImpl;

public class FailableServerSocket extends ServerSocket {

	public FailableServerSocket(int port) throws IOException {
		super(port);
	}
	
    public FailableSocket accept() throws IOException {
    	if (isClosed())
    	    throw new SocketException("Socket is closed");
    	if (!isBound())
    	    throw new SocketException("Socket is not bound yet");
    	FailableSocket s = new FailableSocket((SocketImpl) null);
    	implAccept(s);
    	return s;
    }
}
