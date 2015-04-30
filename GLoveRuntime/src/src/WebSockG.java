package src;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.Random;
//import java.util.Scanner;
//import java.util.logging.Level;
//import java.util.logging.Logger;

import java.util.Scanner;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebSockG {

    private static final int port = 8887;
    //private static final String BLUE_PORT = "";
    private static SensorData latestData =
    		new SensorData((short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0);
    private static boolean run = true;
    private  static SerialGlove serial;
	private static PrintWriter log;

    
    public static void main(String[] args) throws UnknownHostException {

        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
        
        
        
//        try {
//			log = new PrintWriter(new BufferedWriter(new FileWriter("C:/Users/Gabri/Desktop/TIROCINIO/GLoveRuntime/src/src/log.txt")));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			System.out.println(e1.getMessage());
//		}

        serial = new SerialGlove("COM25");
        
        //log.println("AVVIATO SERIAL LISTENER");
       
		Thread t1 = new Thread(new SensorFetcher(serial));
        t1.start();

        WebSock sock = new WebSock(port);
        sock.start();

        System.out.println("Press 'q' to quit..");
        System.out.println("");
        Scanner sc = new Scanner(System.in);
        
        while (run);          
    }
    

    private static class WebSock extends WebSocketServer {

        private final Gson gson = new Gson();

        public WebSock(int port) throws UnknownHostException {
            super(new InetSocketAddress(port));
            System.out.println("Websocket server running... Listning on port " + port);
        }

        @Override
        public void onOpen(WebSocket ws, ClientHandshake ch) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println("");
            System.out.println(dateFormat.format(date) + " : " + ws.getRemoteSocketAddress() + " connected!");
            System.out.println("");
        }

        @Override
        public void onClose(WebSocket ws, int i, String string, boolean bln) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println(dateFormat.format(date) + " : " + ws.getRemoteSocketAddress() + " disconnected!");
        }

        @Override
        public void onError(WebSocket ws, Exception excptn) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println(dateFormat.format(date) + " : " + ws.getRemoteSocketAddress() + " error! "+excptn.getMessage());
        }

        @Override
        public void onMessage(WebSocket ws, String string) {
            ws.send(gson.toJson(latestData.asArray()));           
        }
    }

    private static class SensorFetcher implements Runnable {

    	private SerialGlove blueSerial;
    	
        public SensorFetcher(SerialGlove serial) {
        	this.blueSerial = serial;
        }

        @Override
        public void run() {
            while (run) {

//            	short acc_x = 0;
//              short acc_y = 0;
//              short acc_z = 0;
//              short gyr_x = 0;
//				short gyr_y = 0;
//				short gyr_z = 0;
//				short magn_x = 0;
//				short magn_y = 0;
//				short magn_z = 0; 
//
//              latestData = new SensorData(acc_x, acc_y, acc_z, gyr_x, gyr_y, gyr_z, magn_x, magn_y, magn_z);
            	
            	while(!blueSerial.isReady());
            	
            	latestData = new SensorData(blueSerial.getData());
            	//log.println(latestData.toString());
               
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    System.out.println("Errore sleep");
                }
            }
        }
    }

    private static class SensorData {

        private final short acc_x, acc_y, acc_z, gyr_x, gyr_y, gyr_z, magn_x, magn_y, magn_z;

        public SensorData(short acc_x, short acc_y, short acc_z, short gyr_x,
							short gyr_y, short gyr_z, short magn_x, short magn_y, short magn_z) {
			
        	super();
			this.acc_x = acc_x;
			this.acc_y = acc_y;
			this.acc_z = acc_z;
			this.gyr_x = gyr_x;
			this.gyr_y = gyr_y;
			this.gyr_z = gyr_z;
			this.magn_x = magn_x;
			this.magn_y = magn_y;
			this.magn_z = magn_z;
		}
        
        public SensorData(short data[])
        {
        	super();
        	if(data.length != 9)
        		throw new IllegalArgumentException();
        	
        	this.acc_x = data[0];
			this.acc_y = data[1];
			this.acc_z = data[2];
			this.gyr_x = data[3];
			this.gyr_y = data[4];
			this.gyr_z = data[5];
			this.magn_x = data[6];
			this.magn_y = data[7];
			this.magn_z = data[8];
        	
        }

        public short[] asArray() {
            return new short[]{acc_x, acc_y, acc_z, gyr_x, gyr_y, gyr_z, magn_x, magn_y, magn_z};
        }

        @Override
        public String toString() {
            return String.format("Accelleration: %d  %d  %d | Gyro:  %d  %d  %d | Magnetic:  %d  %d  %d",
            									acc_x, acc_y, acc_z, gyr_x, gyr_y, gyr_z, magn_x, magn_y, magn_z);
        }
    }
}