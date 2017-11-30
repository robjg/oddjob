package org.oddjob.jmx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailableSocketFactory extends RMISocketFactory {

	private static final Logger logger = LoggerFactory.getLogger(FailableSocketFactory.class);
	
	private boolean fail;
	
	final Set<FailableSocket> sockets = new HashSet<FailableSocket>();
	
	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		logger.info("Creating Server Socket on port " + port);
		return new FailableServerSocket(port) {
			@Override
			public FailableSocket accept() throws IOException {
				FailableSocket socket = super.accept();
				synchronized (sockets) {
					socket.setFail(fail);
					sockets.add(socket);
				}
				return socket;
			}
		};
	}
	
	@Override
	public Socket createSocket(String host, int port) throws IOException {
		logger.info("Creating Socket to " + host + " on port " + port);
		throw new RuntimeException("Unexpected from server.");
//		FailableSocket socket = new FailableSocket(host, port) {
//			public synchronized void close() throws IOException {
//				super.close();
//				sockets.remove(this);
//			}
//		};
//		sockets.add(socket);
//		return socket;
	}
	
	public void setFail(boolean fail) {
		synchronized (sockets) {
			this.fail = fail;
			for (FailableSocket socket: sockets) {
				socket.setFail(fail);
			}
		}
	}
}
