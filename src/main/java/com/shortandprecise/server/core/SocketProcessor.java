package com.shortandprecise.server.core;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class SocketProcessor implements Runnable {

	private Selector selector;
	private Queue<SocketChannel> connectionQueue;

	public SocketProcessor(Queue<SocketChannel> connectionQueue) throws IOException {
		this.selector = Selector.open();
		this.connectionQueue = connectionQueue;
	}

	@Override
	public void run() {

		while (true) {
			try {
				SocketChannel socketChannel = connectionQueue.poll();
				if(Objects.nonNull(socketChannel)) {
					socketChannel.configureBlocking(false);
					SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
					Data data = new Data(socketChannel, selector, selectionKey);
					selectionKey.attach(data);
				}

				int availableChannelForProcess = selector.select(100);
				if(availableChannelForProcess == 0) {
					continue;
				}

				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				selectionKeys.forEach(selectionKey -> {
					if (selectionKey.isValid()) {
						int interest = selectionKey.interestOps();
						Data data = (Data) selectionKey.attachment();
						switch (interest) {
							case SelectionKey.OP_READ:
								data.read();
								break;
							case SelectionKey.OP_WRITE:
								data.write();
								break;
							default:
								break;

						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
