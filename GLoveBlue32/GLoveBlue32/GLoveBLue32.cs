using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using InTheHand.Net.Bluetooth;
using InTheHand.Net;
using InTheHand.Windows.Forms;
using InTheHand.Net.Sockets;
using System.IO;

class GloveBlue32
{

    private static readonly Guid guid = new Guid("{4d36e978-e325-11ce-bfc1-08002be10318}");
    private static BluetoothAddress GLoveAddr;
    private static List<BluetoothDeviceInfo> deviceList = new List<BluetoothDeviceInfo>();
    private BluetoothEndPoint endPoint;
    private BluetoothClient client;
    private BluetoothComponent blueComponent;

    private static BluetoothDeviceInfo GLove;

    private static bool discoverEnded=false;
    private static bool isSensing = false;

    private static Stream stream;

    private static byte[] START_CODE = new byte[] { 0x01, 0x02, 0x01, 0x03 };
    private static byte[] STOP_CODE = new byte[] { 0x01, 0x02, 0x00, 0x03 };
    private static byte[] GLoveData = new byte[21];
   

    public GloveBlue32()
	{
        for (int i = 0; i < 21; i++)
            GLoveData[i] = 0;

        GLoveAddr = BluetoothAddress.Parse("0080E1B32953");
        endPoint = new BluetoothEndPoint(BluetoothAddress.Parse("00:09:dd:50:81:3c"), BluetoothService.SerialPort);
        client = new BluetoothClient(endPoint);
	}

    public void connectToGLove()
    {
        blueComponent = new BluetoothComponent(client);

        Console.WriteLine("Discover All Devices");

        blueComponent.DiscoverDevicesAsync(255, true, true, true, true, null);
        blueComponent.DiscoverDevicesProgress += new EventHandler<DiscoverDevicesEventArgs>(component_DiscoverDevicesProgress);
        blueComponent.DiscoverDevicesComplete += new EventHandler<DiscoverDevicesEventArgs>(component_DiscoverDevicesComplete);

        while (!discoverEnded) ;

        Console.WriteLine("Identifing GLove");

        foreach (BluetoothDeviceInfo dev in deviceList)
            if (dev.DeviceAddress == GLoveAddr)
                GLove = dev;

        client.BeginConnect(GLove.DeviceAddress, BluetoothService.SerialPort, new AsyncCallback(Connect), GLove);
        stream = client.GetStream();

        Console.WriteLine("Stream created");

    }

    private static void Connect(IAsyncResult ar)
    {
        if (ar.IsCompleted)
            Console.WriteLine("Conncetion Completed");       
    }

    private void component_DiscoverDevicesProgress(object sender, DiscoverDevicesEventArgs e)
    {
        for (int i = 0; i < e.Devices.Length; i++)
        {
            if (e.Devices[i].Remembered)
            {
                Console.WriteLine(e.Devices[i].DeviceName + " (" + e.Devices[i].DeviceAddress + "): Device is known");
            }
            else
            {
                Console.WriteLine(e.Devices[i].DeviceName + " (" + e.Devices[i].DeviceAddress + "): Device is unknown");
            }
            deviceList.Add(e.Devices[i]);
        }
    }

    private void component_DiscoverDevicesComplete(object sender, DiscoverDevicesEventArgs e)
    {
        Console.WriteLine("Dicover Ended");
        discoverEnded = true;
            
    }

    
    
    public void enableGloveSensing()
    {
        stream.Write(START_CODE, 0, START_CODE.Length);
        isSensing = true;

    }

    public void disableGLoveSensing()
    {
        stream.Write(STOP_CODE, 0, STOP_CODE.Length);
        isSensing = false;
    }

    public short[] getData()
    {
        stream.Read(GLoveData, 0, 21);
        short[] ret = new short[9];
        int i = 0;

        for (int j = 2; j < GLoveData.Length - 1 && i < 9; j += 2)
        {
            ret[i] = (short)BitConverter.ToUInt16(new byte[2] { GLoveData[j], GLoveData[j + 1] }, 0);
            i++;
        }

        return ret;
    }

    public Boolean GLoveSensing()
    {
        return isSensing;
    }



    
}
