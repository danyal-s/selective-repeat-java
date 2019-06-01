/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selectiverepeatsender;

import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Danyal
 */
public class Timeout 
{
    Timer timer;
    PrintWriter stream;
    String data;
    public Timeout(PrintWriter stream, String data, int milliseconds)
    {
        this.stream = stream;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimeoutTask(), milliseconds, milliseconds);
        this.data = data;
        System.out.println("Created new timer with data " +  data + ".");
        
    }
    class TimeoutTask extends TimerTask
    {
        @Override
        public void run() 
        {
            System.out.println("Timed out, resending " + data + ".");
            stream.println(data);
        }
    }
}