/*
 * SingleInstance.java
 *
 * Created on March 22, 2004, 2:05 PM
 */

/**
 *
 * @author  akumar03
 */
package tufts.vue;

import java.net.*;
import java.io.*;
public class SingleInstance {
    
    /** Creates a new instance of SingleInstance */
    
    public static boolean running = false;
    private static final int port = 12000;
    private static int count = 0;
    Socket socket = null;
    Socket client = null;
    ServerSocket server;
    String[] args;
    public SingleInstance(String[] args) {
        try {
            if(args.length > 0) {
                System.out.println("Creating client");
                client =  new Socket("localhost", port);
                PrintWriter writer = new PrintWriter(client.getOutputStream(),true);
                writer.write(args[0]);
                writer.close();
                client.close();
            }
        } catch(Exception ex) {
            //ignore
        }
        try {
            if(client == null) {
                createServerSocket(args);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        running = true;
        Thread t = new Thread() {
            public void run() {
                while(running) {
                    try {
                        running = tufts.vue.VUE.getInstance() != null;
                        Thread.sleep(2000);
                    } catch(Exception e) {
                        System.out.println("Error checking instance of vue: running="+running);
                        running = false;
                        
                    }
                    System.out.println(running);
                }
            }
        };
        //t.start();
        System.out.println("Starting Single Instance");
        SingleInstance singleInstance = new SingleInstance(args);
    }
    
    private void createServerSocket(String[] args) {
        this.args = args;
        System.out.println("Create Server Socket");
        try {
            server = new ServerSocket(port);
            
            Thread vueThread = new Thread() {
                public void run() {
                    tufts.vue.VUE.main(SingleInstance.this.args);
                }
            };
            vueThread.start();
            while(running) {
                try {
                    Socket socket = server.accept();
                    System.out.println("New connection accepted " +
                    socket.getInetAddress() +
                    ":" + socket.getPort());
                    //if(socket.getInetAddress().equals("localhost")) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        
                        while(running) {
                            String message = input.readLine();
                            if (message==null) break;
                            tufts.vue.action.OpenAction.displayMap(new File(message));
                        }
                    //}
                    socket.close();
                    System.out.println("Connection closed by client");
                    
                }
                catch (Exception e) {
                    System.out.println(e);
                }
                
            }
            
            System.out.println("SOCKET CLOSED");
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
}
