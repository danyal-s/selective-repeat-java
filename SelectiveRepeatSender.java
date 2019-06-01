/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selectiverepeatsender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Danyal
 */
public class SelectiveRepeatSender 
{
    public static void main(String[] args) throws InterruptedException
    {
        Scanner scan = new Scanner(System.in);
        int sw, sf = 0, sn = 0, m;
        System.out.println("Input the value of 'm': ");
        m = scan.nextInt();
        sw = (int)Math.pow(2, m - 1);
        int timeout = 200;
        String[] data = new String[(int)Math.pow(2,m)];
        Timeout[] timers = new Timeout[sw];
        for(int i = 0; i < data.length; i++)
        {
            data[i] = Integer.toString(i);
        } //Generating string data to send.
        m = (int)Math.pow(2, m);
        try
        {  
            int portnumber = 2325;
            ServerSocket uservice = new ServerSocket(portnumber);
            System.out.println("Port number is: " + portnumber);
            Socket usocket = uservice.accept();
            System.out.println("accepted");
            BufferedReader inbuffer = new BufferedReader(new InputStreamReader(usocket.getInputStream()));
            PrintWriter outbuffer = new PrintWriter(usocket.getOutputStream(), true);
            long[] timervals = new long[sw];
            for(int i = 0; i < timervals.length; i++)
                timervals[i] = Long.MAX_VALUE;
            outbuffer.println(Integer.toString(m));
            System.out.println("Done writing.");
            while(true)//for(int i = 0; i < data.length; i++)
            {
                String incoming = inbuffer.readLine();
                if(incoming.contains("ACK"))
                {
                    if(incoming.contains("NACK"))
                    {
                        System.out.println(incoming  + " received.");
                        int nackno = Integer.parseInt(incoming.substring(4, incoming.length()));
                        if(isBetween(nackno, Integer.parseInt(data[sf%m]), Integer.parseInt(data[sn%m]), m))
                        {
                            System.out.println("Sending out " + data[nackno] + ".");
                            outbuffer.println(data[nackno]);
                            if(timers[nackno%sw]!=null)
                            {
                                System.out.println("Cancelling timer number " + nackno + ".");
                                timers[nackno%sw].timer.cancel();
                                timers[nackno%sw] = null;
                            }
                            System.out.println("Creating new timer in " + nackno);
                            timers[nackno%sw] = new Timeout(outbuffer, data[nackno], timeout);     
                        }
                    }
                    else
                    {
                        System.out.println(incoming + " received.");
                        int ackno = Integer.parseInt(incoming.substring(3, incoming.length()));
                        if(isBetween(ackno, Integer.parseInt(data[sf%m]), Integer.parseInt(data[sn%m]), m))
                        {
                            while(Integer.parseInt(data[sf%m]) != ackno)
                            {
                                System.out.println("Cancelling timer number " + sf%sw + ".");
                                timers[sf%sw].timer.cancel();
                                timers[sf%sw] = null;
                                sf++;
                            }
                        }
                    }
                }
                else if(incoming.contains("FRAME"))
                {
                    System.out.println("Receieved frame request packet.");
                    if(sn-sf >= sw)
                    {
                        System.out.println("Window size is already max. Not sending out frame.");
                        continue;
                    }
                    System.out.println("Sending out frame number " + sn%m);
                    outbuffer.println(data[sn%m]);
                    timers[sn%sw] = new Timeout(outbuffer, data[sn%m], timeout);
                    sn++;
                }
                else if(incoming.equals("FINISH"))
                {
                    System.out.println("Finished serving the client.");
                    break;
                }
                //sleep(50);
            }
            inbuffer.close();
            outbuffer.close();
            usocket.close();
            uservice.close();
            System.exit(0);
        }
        catch (IOException e) 
        {
           System.out.println(e);
        }
    }  
    static boolean isBetween(int num, int n1, int n2, int mod)
    {
        if(num == n1)
            return true;
        
        while(n1 != n2)
        {
            n1 = (n1 + 1)%mod;
            if(num == n1)
                return true;
        }
        return false;
    } 
}