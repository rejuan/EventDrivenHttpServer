package com.shortandprecise.server.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public class SocketAcceptor implements Runnable {

	private int port;
	private ServerSocketChannel serverSocketChannel;
	private Queue<SocketChannel> connectionQueue;

	public SocketAcceptor(int port, Queue<SocketChannel> connectionQueue) {
		this.port = port;
		this.connectionQueue = connectionQueue;
	}

	@Override
	public void run() {

		try {
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				SocketChannel socketChannel = this.serverSocketChannel.accept();
				this.connectionQueue.add(socketChannel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
