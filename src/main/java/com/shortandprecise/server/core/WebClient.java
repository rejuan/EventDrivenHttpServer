package com.shortandprecise.server.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class WebClient implements NetworkEvent {

	private Selector selector;
	private String url;
	private ByteBuffer upstreamRequestBuffer;
	private SocketChannel socketChannel;
	private SelectionKey selectionKey;
	private RequestProcessor requestProcessor;

	public WebClient(String url, Selector selector, RequestProcessor requestProcessor) {
		this.url = url;
		this.selector = selector;
		this.requestProcessor = requestProcessor;
		prepareRequest(url);
	}

	@Override
	public void read() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
		try{
			int length = socketChannel.read(byteBuffer);
			if (length == -1) {
				closeConnection(socketChannel);
			} else if(length > 0) {
				byteBuffer.flip();
				String response = StandardCharsets.UTF_8.decode(byteBuffer).toString();
				requestProcessor.updateResponse(response);
				closeConnection(socketChannel);
			}
		} catch (IOException ex) {
			closeConnection(socketChannel);
			ex.printStackTrace();
		}
	}

	@Override
	public void write() {
		try {
			socketChannel.write(upstreamRequestBuffer);
			if (!upstreamRequestBuffer.hasRemaining()) {
				selectionKey.interestOps(SelectionKey.OP_READ);
			}
		} catch (IOException ex) {
			closeConnection(socketChannel);
			ex.printStackTrace();
		}
	}

	@Override
	public void connect(SelectionKey selectionKey) {
		selectionKey.interestOps(SelectionKey.OP_WRITE);
		socketChannel = (SocketChannel) selectionKey.channel();
		this.selectionKey = selectionKey;
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			closeConnection(socketChannel);
			e.printStackTrace();
		}
	}

	private void prepareRequest(String url) {
		try {
			URL urlObj = new URL(url);
			String host = urlObj.getHost();
			int port = urlObj.getPort() == -1 ? urlObj.getDefaultPort() : urlObj.getPort();
			String path = urlObj.getPath();

			String requestStr = "GET " + path + " HTTP/1.1\r\nHost: "+ urlObj.getAuthority() + "\r\n\r\n";
			upstreamRequestBuffer = ByteBuffer.wrap(requestStr.getBytes());

			SocketAddress socketAddress =
					new InetSocketAddress(host, port);
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(socketAddress);
			SelectionKey sKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
			sKey.attach(this);
		} catch (Exception ex) {
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
