package com.ftc8813.pycontrol;

import android.icu.util.TimeUnit;
import android.os.Environment;
import android.text.TextUtils;

import com.qualcomm.robotcore.util.RobotLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <p>
 * The Python class represents a Python sub-process, which may or may not be running. The enclosed
 * Process object, which represents the actual running sub-process, can be used to access its
 * standard input, standard output, and standard error streams.
 * </p>
 * <p>
 * The {@link #close()} method should <b>always</b> be called to forcibly clean up the Python process;
 * otherwise, it could potentially keep running indefinitely until the phone or Control Hub is restarted.
 * </p>
 */
public class Python implements AutoCloseable
{
    private static final String TAG = "Python";
    private static final String PYTHON_PATH = "/system/bin/python";
    
    private String scriptPath;
    private File workDir = Environment.getExternalStorageDirectory(); // "/sdcard"
    private Process proc;
    
    private boolean started = false;
    private boolean closed = false;
    
    public Python(String scriptPath)
    {
        this.scriptPath = scriptPath;
    }
    
    public void setScriptPath(String scriptPath)
    {
        // in case you change your mind, I guess
        this.scriptPath = scriptPath;
    }
    
    public void setWorkDir(File workDir)
    {
        this.workDir = workDir;
    }
    
    public String getScriptPath()
    {
        return scriptPath;
    }
    
    public File getWorkDir()
    {
        return workDir;
    }
    
    /**
     * <p>Start the Python process, optionally passing some arguments to it.</p>
     * <p>
     * NOTE: If the Python script cannot be found, the process will exit immediately with a message
     * to the standard error stream and an exit value of 2. This event can be difficult to capture,
     * so it is advisable to check if the file exists BEFORE starting the process.
     * </p>
     * @param args Arguments to pass to the Python script.
     * @return The started process
     * @throws IOException if an error occurs when creating the process.
     * @throws IllegalStateException if this object has been started already or is closed.
     */
    public Process start(String... args) throws IOException
    {
        if (closed)
            throw new IllegalStateException("Python instance closed");
        if (started)
            throw new IllegalStateException("Cannot start() a Python instance multiple times");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command().add(PYTHON_PATH);
        if (scriptPath != null)builder.command().add(scriptPath);
        builder.command().addAll(Arrays.asList(args));
        builder.directory(workDir);
        String cmdString = TextUtils.join(" ", builder.command());
        
        RobotLog.dd(TAG, "Starting Python process: " + cmdString);
    
        proc = builder.start();
        started = true;
    
        return proc;
    }
    
    /**
     * Get the underlying Process object. This will return null until {@link #start(String...)} is
     * called or after {@link #close()} has been called. The returned Process should NOT be destroyed
     * using the {@link Process#destroy()} or {@link Process#destroyForcibly()} methods. Use
     * {@link #close()} to destroy the process instead.
     * @return The running process, or null if the process is not running.
     */
    public Process getProcess()
    {
        return proc;
    }
    
    /**
     * Tries to guess whether the process is still running based on the state of this object.
     * @return true if the process is {@link #started()} but has not been {@link #closed()}; returns
     *         false otherwise.
     */
    public boolean running()
    {
        return !closed && started && proc != null;
    }
    
    public boolean started()
    {
        return started;
    }
    
    public boolean closed()
    {
        return closed;
    }
    
    /**
     * <p>
     * Close the process by sending a SIGTERM signal to it. If the Python script wishes to clean up
     * here, it must handle this SIGTERM signal.
     * </p>
     * <p>
     * If this Python object is already closed or has not been started, this method has no effect.
     * </p>
     */
    @Override
    public void close()
    {
        if (!running()) return;
        
        closed = true;
        
        proc.destroy(); // Python code must handle SIGTERM if it wants to clean up
        
        proc = null;
    }
    
    
    ////////
    // Convenience functions
    
    /**
     * Run a Python process until it completes (or its standard output stream is closed). Captures the
     * entire standard output of the process and returns it as a String. However, standard error is
     * NOT captured.
     * @param script The script to run, or null to pass arguments directly to Python itself.
     * @param args Arguments to be passed to the script
     * @return The standard output captured from the Python process
     * @throws IOException if an error occurs creating the process or reading its output
     */
    public static String run(String script, String... args) throws IOException
    {
        if (!new File(script).isFile())
            throw new FileNotFoundException(String.format("Script %s does not exist or is not a regular file", script));
        
        Python py = new Python(script);
        py.start(args);
        InputStream in = py.getProcess().getInputStream();
        
        byte[] buf = new byte[8192];
        StringBuilder result = new StringBuilder();
        while (true)
        {
            int n = in.read(buf);
            if (n < 0) break;
            
            result.append(new String(buf, 0, n, StandardCharsets.UTF_8));
        }
        
        py.close();
        
        return result.toString();
    }
    
    /**
     * Get the currently installed Python version by running <code>python --version</code>.
     * @return The python version, formatted as <code>"Python X.Y.Z\n"</code>
     * @throws IOException if an error occurs while running Python
     */
    public static String getPythonVersion() throws IOException
    {
        return run(null, "--version");
    }
}
