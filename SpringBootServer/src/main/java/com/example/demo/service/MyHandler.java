package com.example.demo.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;


@Service
public class MyHandler implements WebSocketHandler {

	// 在线用户列表
	private static final Map<String, WebSocketSession> users;

	static {
		users = new HashMap<>();
	}

	// 新增socket
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("成功建立连接");
		String ID = session.getUri().toString().split("ID=")[1];
		// ClientList clientList = ClientList.GetClientList();
		// System.out.println(ID+"ID");
		if (ID != null) {
			users.put(ID, session);
			session.sendMessage(new TextMessage("1"));
			// clientList.AddClient(new Client(ID));
			// System.out.println(ID);
			// System.out.println(session);
		}
		// System.out.println("当前在线人数："+users.size());
	}

	// 接收socket信息
	@Override
	public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage)
			throws Exception {
		// TODO Auto-generated method stub

		if (webSocketMessage.getPayloadLength() > 0) {
			/*
			 * JSONObject jsonobject = JSONObject.fromObject(webSocketMessage.getPayload());
			 * int count =(int)jsonobject.get("number"); if(count%10!=0) {
			 * System.out.println(count); HttpClientUtil2 clientUtil2 =new
			 * HttpClientUtil2(); String resultPostUrl =
			 * "http://192.168.8.212:8333/alert_handle/invasion_alert/"; Map<String,String>
			 * mapParam =new HashMap<>(); mapParam.put("ip", (String)jsonobject.get("id"));
			 * mapParam.put("serialnumber", (String)jsonobject.get("seserialnumer"));
			 * mapParam.put("snapshot_base64", GuardAlarmService.getBase64Picture()); String
			 * result = clientUtil2.doPost(resultPostUrl, mapParam, "utf-8"); JSONObject
			 * jsonobjects = JSONObject.fromObject(result); int status = (int)
			 * jsonobjects.get("status"); if(status==200) { JSONObject dataParam =
			 * JSONObject.fromObject(jsonobjects.get("data"));
			 * 
			 * if(jsonobjects.get("data") == null) {
			 * sendMessageToUser(jsonobject.get("id")+"",new TextMessage("1")); }else {
			 * System.out.println(jsonobject.get("message")+":来自"+(String)webSocketSession.
			 * getAttributes().get("WEBSOCKET_USERID")+"的消息"); boolean isend =
			 * sendMessageToUser(jsonobject.get("id")+"",new TextMessage(dataParam+""));
			 * System.out.println(isend); }
			 * 
			 * }
			 * 
			 * }else { sendMessageToUser(jsonobject.get("id")+"",new TextMessage("1")); }
			 */

		}
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// TODO Auto-generated method stub
		if (session.isOpen()) {
			session.close();
		}
		System.out.println("连接出错");
		users.remove(getClientId(session));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("连接已关闭：" + closeStatus);
		users.remove(getClientId(session));
	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 发送信息给指定用户
	 * 
	 * @param clientId
	 * @param message
	 * @return
	 */

	@Async
	public static boolean sendMessageToUser(String clientId, TextMessage message) {
		if (users.get(clientId) == null)
			return false;
		WebSocketSession session = users.get(clientId);
		// System.out.println("sendMessage:" + session);
		if (!session.isOpen())
			return false;
		try {

			session.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 获取用户标识
	 * 
	 * @param session
	 * @return
	 */
	private Integer getClientId(WebSocketSession session) {
		try {
			Integer clientId = (Integer) session.getAttributes().get("WEBSOCKET_USERID");
			return clientId;
		} catch (Exception e) {
			return null;
		}
	}

}
