package com.ftc8813.pycontrol.websocket;

import com.qualcomm.robotcore.util.RobotLog;

import java.io.File;
import java.io.IOException;

public class UnixSocketServer extends ServerIO
{
    static
    {
        System.loadLibrary("socketwrench");
    }
    
    private int fd;
    private String path;
    private static final String TAG = "Unix Socket Server";
    
    public UnixSocketServer(String path) throws IOException
    {
        this.path = path;
        int fd = _create(path);
        if (fd < 0) throw new IOException(String.format("Error creating socket: %d", fd));
        this.fd = fd;
    }
    
    @Override
    public SocketIO accept() throws IOException
    {
        int client_fd = _accept(fd);
        if (client_fd < 0)
            throw new IOException(String.format("Error creating connection: %d", client_fd));
        return new UnixSocket(path, client_fd);
    }
    
    @Override
    public void close() throws IOException
    {
        _close(fd);
        if (!new File(path).delete()) RobotLog.ww("Failed to delete socket file %s", path);
        super.close();
    }
    
    private native int _create(String path);
    
    private native int _accept(int server_fd);
    
    private native int _close(int fd);
}
