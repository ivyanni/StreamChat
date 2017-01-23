package ru.tersoft.streamchat;

import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * Project streamchat.
 * Created by ivyanni on 22.01.2017.
 */
public class Consumer implements Runnable {
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private PrintWriter logWriter;
    private Thread consumerThread;

    public Consumer(BufferedReader socketReader, PrintWriter socketWriter, PrintWriter logWriter) {
        this.socketReader = socketReader;
        this.socketWriter = socketWriter;
        this.logWriter = logWriter;
        consumerThread = new Thread(this);
        consumerThread.start();
    }

    public void run () {
        try {
            while (true) {
                String str = socketReader.readLine();
                logWriter.println("> " + str);
                if (str.startsWith("PING")) {
                    socketWriter.println(str.replace("PING", "PONG"));
                    logWriter.println("< " + str.replace("PING", "PONG"));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stop() {
        consumerThread = null;
    }
}
