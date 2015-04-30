using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;



   class Controller
   {
       public static void Main()
       {
           GLoveBlue serial = new GLoveBlue();
           Console.Write(GLoveBlue.findPorts());
           serial.openPort(Console.ReadLine());
           serial.enableGLoveSensing();

           while (true)
           {
               short[] data = serial.getGLoveData();
               for (int i = 0; i < 9; i++)
                   Console.Write(data[i] + " ");
               Console.Write("\n");
           }
       }

   }

