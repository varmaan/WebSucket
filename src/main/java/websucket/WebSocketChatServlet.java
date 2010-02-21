package websucket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class WebSocketChatServlet extends WebSocketServlet {
	final Set<ChatWebSocket> members = new CopyOnWriteArraySet<ChatWebSocket>();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getServletContext().getNamedDispatcher("default").forward(request, response);
	}

	@Override
	protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new ChatWebSocket();
	}

	class ChatWebSocket implements WebSocket {
		Outbound outbound;

		@Override
		public void onConnect(Outbound outbound) {
			this.outbound = outbound;
			members.add(this);
		}

		@Override
		public void onMessage(byte frame, String data) {
			for (ChatWebSocket member : members) {
				try {
					member.outbound.sendMessage(frame, data);
				} catch (IOException e) {
					Log.warn(e);
				}
			}
		}

		@Override
		public void onDisconnect() {
			members.remove(this);
		}

		@Override
		public void onMessage(byte frame, byte[] data, int offset, int length) {
			throw new UnsupportedOperationException();
		}
	}
}