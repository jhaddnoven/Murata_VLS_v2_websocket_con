package com.example.murata_vls;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class websocketcleintlistener extends WebSocketClient {

    public websocketcleintlistener(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // Connection opened
    }

    @Override
    public void onMessage(String message) {
        // Handle incoming messages here
        if (message.contains("\"event\":\"capture\"")) {
            // Trigger the capture action on the Android app45
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // Connection closed
    }

    @Override
    public void onError(Exception ex) {
        // Handle errors
    }

}
