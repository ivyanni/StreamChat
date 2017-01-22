package ru.tersoft.streamchat;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Project streamchat.
 * Created by ivyanni on 22.01.2017.
 */
public class Consumer implements Runnable {
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread consumerThread;

    public Consumer(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
        consumerThread = new Thread(this);
        consumerThread.start();
    }

    public void run () {
        try {
            while (true) {
                String str = reader.readLine();
                writer.println(str);
                if (str.startsWith("PING"))
                    writer.println(str.replace("PING", "PONG"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stop() {
        consumerThread = null;
    }
}
