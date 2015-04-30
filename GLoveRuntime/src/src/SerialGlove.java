package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import gnu.io.*;


public class SerialGlove implements SerialPortEventListener {

//	private short acc_x = 0;
//	private short acc_y = 0;
//	private short acc_z = 0;
//	private short gyr_x = 0;
//	private short gyr_y = 0;
//	private short gyr_z = 0;
//	private short magn_x = 0;
//	private short magn_y = 0;
//	private short magn_z = 0;
//	
	//***************************************
	
	private  Enumeration<CommPortIdentifier> ports = null;
	private  HashMap<String, CommPortIdentifier> portMap;
	
	private  CommPortIdentifier selectedPortID = null;
	private  SerialPort serialPort = null;
	
	private  InputStream input = null;
	private  OutputStream output = null;
	
	
	final  int TIMEOUT = 2000;
	
	private final  byte[] START_CODE= new byte[] {0x01, 0x02, 0x01, 0x03};
	// private final  byte[] STOP_CODE= new byte[] {0x01, 0x02, 0x00, 0x03};
	
	private String PORT = "COM15"; //non posso cablarlo nel codice	
	private byte[] GLoveData;
	
	private boolean ready = false;
	//private PrintWriter log;
		
	
	@SuppressWarnings("unchecked")
	public SerialGlove(String porta) {
		
		portMap = new HashMap<String, CommPortIdentifier>();
		GLoveData = new byte[21];
		
		//this.log = log;		
		
		if (porta!=null)
			this.PORT = porta;
		
		// RICERCA PORTE SERIALI DISPONIBILI *******************************
		
		ports = CommPortIdentifier.getPortIdentifiers();
		
		
		while(ports.hasMoreElements())
		{
			CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
			
			if(curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
			{
				//log.println(curPort.getName());
				portMap.put(curPort.getName(), curPort);
			}		
		}	
		
		
		// APERTURA CONNESSIONE VERSO LA PORTA INDICATA *********************
		
		selectedPortID = (CommPortIdentifier)portMap.get(PORT);
		if(selectedPortID == null)
			throw new IllegalArgumentException("Porta non valida");
		
		CommPort commPort = null;
		
		try
		{
			commPort = selectedPortID.open("GLoveRuntime", TIMEOUT);
			
			serialPort = (SerialPort) commPort;
			
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.enableReceiveTimeout(TIMEOUT);
			serialPort.notifyOnDataAvailable(true);
			
						
			//log.println(porta + "opened Successfully");
		}
		catch(PortInUseException e)	{
			
			//log.println(e.getMessage());
		}
		catch (UnsupportedCommOperationException e) {
			
			//log.println(e.getMessage());
		}
		catch (Exception e)
		{
			//log.println(e.getMessage());
		}
		
		//log.println("Connessione stabilita");
		
		// INIT STREAM I/0 ***************************************************
		
		try
		{
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
		}
		catch(IOException e)
		{
			//log.println("Errore apertura stream I/0 "+e.getMessage());
		}
		
		//log.println("Stream I/O predisposto");
		// TRASMISSIONE PACCHETTO DI START ************************************
		
		try
		{
			output.write(START_CODE, 0, START_CODE.length);
			output.flush();
			
			
		}
		catch(Exception e)
		{
			//log.println("Errore trasmissione START_CODE "+e.getMessage());
		}
		
		//log.println("Trasmesso condice di start");
		
		GLoveData = new byte[21];
		
		ready = true;
	}	
	
	
	

	public short[] getData() {
				
		
		byte[] temp = new byte[2];
		
		ArrayList<Short> ret = new ArrayList<Short>();
		
		for(int i=2; i<(GLoveData.length-1); i+=2)
		{
			temp[0] = GLoveData[0];
			temp[1] = GLoveData[1];
			ret.add(this.bytesToShort(temp));
		}
		
		short[] ar = new short[9];
		for(int i=0; i<9; i++)
			ar[i] = ret.get(i);
		
		return ar;		
	}
	
	private short bytesToShort(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		
		int r=0;
		
		try
		{
			r = input.read(GLoveData, 0, 21);			
		}
		catch(Exception e)
		{
			System.out.println("Errore lettura GLoveData "+e.getMessage());
		}
		
		System.out.println("Ricevuti " + r + " bytes");	
	}
	
	public boolean isReady()
	{
		return ready;
	}
	
}
