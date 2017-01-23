package ru.tersoft.streamchat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * Project streamchat.
 * Created by ivyanni on 21.01.2017.
 */
public class TwitchApi {
    private static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    public static final String AUTH_URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
                                        "?response_type=token" +
                                        "&client_id=" + CLIENT_ID +
                                        "&redirect_uri=http://localhost/twitch_oauth" +
                                        "&scope=chat_login+channel_read+user_read";

    private static PrintWriter socketWriter;
    private static PrintWriter logWriter;
    private final String SERVER = "irc.chat.twitch.tv";
    private final int PORT = 6667;
    private final String USERINFO_URL = "https://api.twitch.tv/kraken/channel";
    private String token;
    private String username;

    public TwitchApi() {
    }

    private void connect() {
        try {
            Socket socket=new Socket(SERVER, PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            logWriter = new PrintWriter(new OutputStreamWriter(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    Logger.printSymbol((char)b);
                }
            }), true);
            Consumer c = new Consumer(reader, socketWriter, logWriter);

            sendCommand("PASS oauth:" + token);
            sendCommand("NICK " + username);
            sendCommand("JOIN #" + username);
            sendCommand("CAP REQ :twitch.tv/commands");
            sendCommand("CAP REQ :twitch.tv/membership");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendCommand(String str) {
        socketWriter.println(str);
        logWriter.println("< " + str);
    }

    public void sendMessage(String str){
        socketWriter.println("PRIVMSG #"+username+" :" + str);
        logWriter.println("< PRIVMSG #"+username+" :" + str);
    }

    public int start(String token) throws IOException {
        this.token = token;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(USERINFO_URL+"?oauth_token="+token)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            JSONObject obj = new JSONObject(response.body().string());
            username = obj.getString("name");
            connect();
        }
        return response.code();
    }
}
