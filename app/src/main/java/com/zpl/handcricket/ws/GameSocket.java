package com.zpl.handcricket.ws;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zpl.handcricket.BuildConfig;
import com.zpl.handcricket.utils.AppState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Singleton WebSocket client speaking the envelope protocol { type, data }.
 * Activities attach/detach listeners.
 */
public class GameSocket {

    private static final String TAG = "GameSocket";
    private static GameSocket INSTANCE;
    public static synchronized GameSocket get() {
        if (INSTANCE == null) INSTANCE = new GameSocket();
        return INSTANCE;
    }

    public interface Listener {
        void onEvent(String type, JsonObject data);
        default void onOpenConn() {}
        default void onClosedConn() {}
    }

    private WebSocket ws;
    private Listener listener;
    private final Gson gson = new Gson();
    private boolean authed;
    private final Queue<String> pendingPayloads = new ArrayDeque<>();

    public void setListener(Listener l) { this.listener = l; }

    public boolean isConnected() { return ws != null; }
    public boolean isAuthed() { return authed; }

    public void connect() {
        if (ws != null) {
            if (listener != null) listener.onOpenConn();
            return;
        }
        authed = false;
        pendingPayloads.clear();
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
        Request req = new Request.Builder().url(BuildConfig.WS_URL).build();
        ws = client.newWebSocket(req, new WebSocketListener() {
            @Override public void onOpen(WebSocket w, Response r) {
                Log.d(TAG, "WS open");
                if (listener != null) listener.onOpenConn();
                // Send auth
                Map<String, Object> env = new HashMap<>();
                env.put("type", "auth");
                Map<String, String> d = new HashMap<>();
                d.put("token", AppState.get().getToken());
                env.put("data", d);
                w.send(gson.toJson(env));
            }
            @Override public void onMessage(WebSocket w, String text) {
                try {
                    JsonObject obj = gson.fromJson(text, JsonObject.class);
                    String type = obj.get("type").getAsString();
                    JsonObject data = obj.has("data") && obj.get("data").isJsonObject()
                            ? obj.getAsJsonObject("data") : new JsonObject();
                    if ("auth_ok".equals(type)) {
                        authed = true;
                        flushPending();
                    }
                    if (listener != null) listener.onEvent(type, data);
                } catch (Exception e) {
                    Log.e(TAG, "bad message", e);
                }
            }
            @Override public void onFailure(WebSocket w, Throwable t, Response r) {
                Log.e(TAG, "WS failure", t);
                ws = null; authed = false;
                pendingPayloads.clear();
                if (listener != null) listener.onClosedConn();
            }
            @Override public void onClosed(WebSocket w, int code, String reason) {
                Log.d(TAG, "WS closed " + reason);
                ws = null; authed = false;
                pendingPayloads.clear();
                if (listener != null) listener.onClosedConn();
            }
        });
    }

    public void send(String type, Map<String, Object> data) {
        if (ws == null) return;
        Map<String, Object> env = new HashMap<>();
        env.put("type", type);
        env.put("data", data == null ? new HashMap<>() : data);
        String payload = gson.toJson(env);
        if (!authed && !"auth".equals(type)) {
            pendingPayloads.offer(payload);
            return;
        }
        ws.send(payload);
    }

    private void flushPending() {
        if (ws == null) return;
        String payload;
        while ((payload = pendingPayloads.poll()) != null) {
            ws.send(payload);
        }
    }

    public void queueRanked() { send("queue_ranked", null); }
    public void cancelQueue() { send("cancel_queue", null); }
    public void hostFriendly(String roomId) {
        Map<String, Object> d = new HashMap<>();
        d.put("roomId", roomId);
        send("host_friendly", d);
    }
    public void joinFriendly(String roomId) {
        Map<String, Object> d = new HashMap<>();
        d.put("roomId", roomId);
        send("join_friendly", d);
    }
    public void pick(int n) {
        Map<String, Object> d = new HashMap<>();
        d.put("pick", n);
        send("pick", d);
    }
    public void leave() { send("leave", null); }

    public void close() {
        if (ws != null) {
            ws.close(1000, "bye");
            ws = null;
            authed = false;
            pendingPayloads.clear();
        }
    }
}
