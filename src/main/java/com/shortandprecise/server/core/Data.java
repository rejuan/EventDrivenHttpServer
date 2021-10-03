package com.shortandprecise.server.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Data {

	private StringBuilder stringBuilder;
	private SocketChannel socketChannel;
	private Selector selector;
	private SelectionKey selectionKey;
	private byte[] response = "HTTP/1.1 200 Ok\r\nContent-Type: text/html\r\nConnection: Closed\r\nContent-Length: 12S\r\n\r\nHello World!".getBytes();
	private ByteBuffer responseBuffer = ByteBuffer.wrap(response);

	public Data(SocketChannel socketChannel, Selector selector, SelectionKey selectionKey) {
		this.stringBuilder = new StringBuilder();
		this.socketChannel = socketChannel;
		this.selector = selector;
		this.selectionKey = selectionKey;
	}

	public void read() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
		try{
			int length = socketChannel.read(byteBuffer);
			if (length == -1) {
				closeConnection(socketChannel);
			} else {
				byteBuffer.flip();
				stringBuilder.append(StandardCharsets.UTF_8.decode(byteBuffer));
				int position = stringBuilder.indexOf("\r\n\r\n");
				if (position > 0) {
					selectionKey.interestOps(SelectionKey.OP_WRITE);
				}
			}
		} catch (IOException ex) {
			closeConnection(socketChannel);
			ex.printStackTrace();
		}
	}

	public void write() {
		try{
			socketChannel.write(responseBuffer);
			if (!responseBuffer.hasRemaining()) {
				closeConnection(socketChannel);
			}
		} catch (IOException ex) {
			closeConnection(socketChannel);
			ex.printStackTrace();
		}
	}

	private void closeConnection(SocketChannel socketChannel) {
		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
