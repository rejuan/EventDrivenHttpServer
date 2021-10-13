package com.shortandprecise.server;

import com.shortandprecise.server.core.SocketAcceptor;
import com.shortandprecise.server.core.SocketProcessor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Application {

	public static void main(String[] args) throws IOException {

		Queue<SocketChannel> connectionQueue = new LinkedBlockingQueue<>();

		new Thread(new SocketAcceptor(9095, connectionQueue)).start();
		new Thread(new SocketProcessor(connectionQueue)).start();
	}
}
