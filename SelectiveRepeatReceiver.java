/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selectiverepeatreceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Danyal
 */
public class SelectiveRepeatReceiver {

   public static void main(String[] args) throws InterruptedException
    {
        Scanner scan = new Scanner(System.in);
        try
        {
            System.out.println("Enter the IP Address: ");
            String IP = scan.next();
            System.out.println("Enter port number: ");
            String port = scan.next();
            Socket usocket = new Socket(IP, Integer.parseInt(port));
            BufferedReader inbuffer = new BufferedReader(new InputStreamReader(usocket.getInputStream()));
            PrintWriter outbuffer = new PrintWriter(usocket.getOutputStream(), true);
            int rn = 0;
            boolean naksent = false, ackneeded = false;
            boolean datareceived = false;
            int m = Integer.parseInt(inbuffer.readLine());
            boolean[] marked = new boolean[m];
            String data[] = new String[m];
            for(int i = 0; i < marked.length; i++)
            {
                marked[i] = false;
            }
            while(!datareceived)
            {
                outbuffer.println("FRAME");
                sleep(300);
                int frame = Integer.parseInt(inbuffer.readLine());
                if(frame != rn && !naksent && frame > rn)
                {
                    outbuffer.println("NACK" + rn);
                    System.out.println("Received frame number" + frame + " when " + rn + " was expected. Sending NACK" + rn + ".");
                    System.out.println("Sent NACK" + rn);
                    naksent = true;
                    continue;
                }
                else if(frame < rn)
                {
                    System.out.println("Discarded frame number " + frame + " as it's < rn");
                    continue;
                }
                
                if(isBetween(frame, rn, rn + m, m)&&(!marked[frame]))
                {
                    System.out.println("Stored " + frame + " at " + frame + ".");
                    data[frame] = "Frame" + Integer.toString(frame);
                    marked[frame] = true;
                    System.out.println("Marked " + frame + " as received.");
                    while(marked[rn])
                    {
                        System.out.println(rn + " is marked. Moving to " + (rn + 1) + ".");
                        rn++;
                        ackneeded = true;
                         if(rn >= marked.length)
                         {
                             datareceived = true;
                             break;
                         }
                    }
                    if(ackneeded)
                    {
                        System.out.println("ACK is needed. Sending out ACK" + rn + ".");
                        outbuffer.println("ACK"+rn);
                        ackneeded = false;
                        naksent = false;
                    }
                }
                System.out.println(Arrays.toString(data));
            }
            System.out.println("Data successfully retreieved. Final output is:\n" + Arrays.toString(data));
            outbuffer.println("FINISH");
            inbuffer.close();
            outbuffer.close();
            usocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
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