package com.lhjz.portal.component;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.lhjz.portal.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

		String path = request.getURI().getPath();
		String query = request.getURI().getQuery();

		attributes.put("path", path);
		attributes.put("query", query != null ? query : StringUtil.EMPTY);

		log.info("beforeHandshake attributes:{}", attributes);

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		log.info("afterHandshake...");
	}

}
