package com.ftc8813.pycontrol;

import com.ftc8813.pycontrol.websocket.Server;
import com.ftc8813.pycontrol.websocket.UnixSocketServer;

import java.io.IOException;
import java.util.Random;

public class PythonServer implements AutoCloseable
{
    private Python python;
    private Server server;
    private String sockFile;
    
    private boolean started = false;
    private boolean closed = false;
    
    public PythonServer(String script) throws IOException
    {
        python = new Python(script);
        sockFile = String.format("/data/local/tmp/python-ipc-%08x.sock", ((long) new Random().nextInt()) & 0xFFFFFFFFL);
        server = new Server(new UnixSocketServer(sockFile));
    }
    
    public void registerProcessor(int id, Server.CommandProcessor processor)
    {
        if (started) throw new IllegalStateException("Cannot register processors; server already started");
        if (closed)  throw new IllegalStateException("Python server closed");
        
        server.registerProcessor(id, processor);
    }
    
    public String getServerStatus()
    {
        return server.getStatus();
    }
    
    public void start(String... args) throws IOException
    {
        if (started) throw new IllegalStateException("Script already started");
        if (closed)  throw new IllegalStateException("Python server closed");
    
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = sockFile;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        
        server.startServer();
        
        python.start(newArgs);
        
        started = true;
    }
    
    @Override
    public void close()
    {
        if (closed) return;
        
        server.close();
        python.close();
        
        closed = true;
    }
    
    public boolean started()
    {
        return started;
    }
    
    public boolean closed()
    {
        return closed;
    }
}
