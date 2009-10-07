package org.sonatype.nexus.mock.util;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;

public class SocketTestWaitCondition implements ThreadUtils.WaitCondition {
    private String host;
    private int port;
    private int timeout;

    public SocketTestWaitCondition(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public boolean checkCondition(long elapsedTimeInMs) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
