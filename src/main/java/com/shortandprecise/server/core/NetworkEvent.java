package com.shortandprecise.server.core;

import java.nio.channels.SelectionKey;

public interface NetworkEvent {
	void read();
	void write();
	void connect(SelectionKey selectionKey);
}
