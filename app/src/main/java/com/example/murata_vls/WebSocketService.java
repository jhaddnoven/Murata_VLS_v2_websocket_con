package com.example.murata_vls;


import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService extends WebSocketListener {
    private WebSocket webSocket;

    private boolean waitingForResponse = false;
    public WebSocketService() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.1.88:6001/laravel-websockets").build();
        webSocket = client.newWebSocket(request, this);
        subscribeToChannel("button-click");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JSONObject json = new JSONObject(text);
            String event = json.optString("event");

            if (event.equals("App\\Events\\cameraComandEvent")) {
                JSONObject eventData = json.optJSONObject("data");
                String buttonValue = eventData.optString("buttonValue");

                handleReceivedValue(buttonValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedValue(String value) {
        // Handle the received value here (e.g., show a Toast)
        System.out.println(value);
    }

    private void subscribeToChannel(String channelName) {
        // Create a JSON object for subscription
        JSONObject subscribeObject = new JSONObject();
        try {
            JSONObject data = new JSONObject();
            data.put("channel", channelName);
            subscribeObject.put("event", "pusher:subscribe");
            subscribeObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send the subscription request
        webSocket.send(subscribeObject.toString());
    }

}



