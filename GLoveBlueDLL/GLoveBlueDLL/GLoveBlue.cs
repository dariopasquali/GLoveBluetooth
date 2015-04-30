using System;
using System.Collections.Generic;

using System.Text;
using System.Threading;
using System.IO.Ports;
using System.IO;

namespace GLoveBlueDLL
{
    public class GLoveBlue
    {
        private SerialPort serial = new SerialPort();
        private byte[] START_CODE = new byte[] { 0x01, 0x02, 0x01, 0x03 };
        private byte[] STOP_CODE = new byte[] { 0x01, 0x02, 0x00, 0x03 };

        private byte[] GLoveData = new byte[21];
        private bool ready = false;

        public string log;


        public GLoveBlue()
        {
            for (int i = 0; i < 21; i++)
                GLoveData[i] = 0;
        }

        public static string findPorts()
        {

            string[] ports = SerialPort.GetPortNames();
            string ret = "";
            for (int i = 0; i < ports.Length; i++)
            {
                ret += ports[i] + "\n";
            }
            return ret;
        }

        public void openPort(string name)
        {
            serial.PortName = name;
            serial.ReadBufferSize = 22;
            Console.WriteLine(name);
            try
            {
                serial.Open();
            }
            catch (IOException e)
            {
                Console.WriteLine(e.Message);
                return;
            }
            serial.Parity = Parity.None;
            serial.BaudRate = 115200;
            serial.DataBits = 8;
            serial.StopBits = StopBits.One;


            Console.WriteLine("Aperta porta " + name);
        }

        public void enableGLoveSensing()
        {

            serial.Write(START_CODE, 0, START_CODE.Length);
            ready = true;
            Console.WriteLine("GLove attivato");
        }

        public void disableGLoveSensing()
        {
            serial.Write(STOP_CODE, 0, STOP_CODE.Length);
            ready = false;
            Console.WriteLine("GLove disattivato");
        }

        public short[] getGLoveData()
        {
            serial.Read(GLoveData, 0, 21);
            short[] ret = new short[9];
            int i = 0;

            for (int j = 2; j < GLoveData.Length - 1 && i < 9; j += 2)
            {
                ret[i] = (short)BitConverter.ToUInt16(new byte[2] { GLoveData[j], GLoveData[j + 1] }, 0);
                i++;
            }

            return ret;
        }


        public bool isReady()
        {
            return ready;
        }
    }
}

