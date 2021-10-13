package com.shortandprecise.server.core;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class RequestProcessor implements NetworkEvent {

	private StringBuilder stringBuilder;
	private SocketChannel socketChannel;
	private Selector selector;
	private SelectionKey selectionKey;
	private ByteBuffer responseBuffer;
	private static final int FIRST_LINE_POSITION = 0;
	private static final int URI_POSITION = 1;
	private StringBuilder responseMessage = new StringBuilder();
	private int numberOfUrl = 0;

	public RequestProcessor(SocketChannel socketChannel, Selector selector, SelectionKey selectionKey) {
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
			} else if(length > 0) {
				byteBuffer.flip();
				stringBuilder.append(StandardCharsets.UTF_8.decode(byteBuffer));
				int position = stringBuilder.indexOf("\r\n\r\n");
				if (position > 0) {
					String[] request = stringBuilder
							.substring(0, position)
							.split("\r\n");

					String firstLine = request[FIRST_LINE_POSITION];
					String[] firstLineArray = firstLine.split(" ");

					String url = firstLineArray[URI_POSITION];
					URI uri = URI.create(url);
					uri.getRawQuery();

					numberOfUrl = 2;
					// Need to replace the following URL
					new WebClient("http://localhost:8089/mock/abc", selector, this);
					new WebClient("http://localhost:8089/mock/xyz", selector, this);
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

	@Override
	public void connect(SelectionKey selectionKey) {
	}

	public void changeStateToWritable() {
		String response = "HTTP/1.1 200 Ok\r\nContent-Type: text/html\r\n" +
				"Connection: Closed\r\nContent-Length: " + responseMessage.length() +
				"\r\n\r\n" + responseMessage;
		responseBuffer = ByteBuffer.wrap(response.getBytes());
		selectionKey.interestOps(SelectionKey.OP_WRITE);
	}

	public void updateResponse(String response) {
		numberOfUrl--;
		responseMessage.append(response);
		responseMessage.append("<br/>");
		if(numberOfUrl == 0) {
			changeStateToWritable();
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
