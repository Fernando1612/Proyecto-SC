String BT_input;                          // Variable para el mensaje mandado
int LED = 13;                             // Variable para luz de la cocina
int LED1 = 12;                            // Variable para luz de la sala


void setup()  
{  
  Serial.begin(9600);                      //Velocidad default de comunicación
  pinMode(LED, OUTPUT);                    //Asignar la variable del foco de la cocina como salida
  digitalWrite(LED, HIGH);                 //Poner en alto el foco de la cocina
  pinMode(LED1, OUTPUT);                   //Asignar la variable del foco de la sala como salida
  digitalWrite(LED1, HIGH);                //Poner en alto el foco de la sala
}

void loop() 

 { 
  if (Serial.available())                   //Comprobar si el Serial esta disponible
    {   
        BT_input = Serial.readString();   // Leer la cadena de salida de bluethoot
        
                if (BT_input=="enciende la luz de la cocina")    //Comprobar el mensaje enviado            
                {
                  digitalWrite(LED, LOW);           //Poner en bajo el foco de la cocina
                  Serial.println(BT_input);         //Mensaje de salida
                  Serial.println("LED is ON");      //Mensaje de salida
                }
                
                else if (BT_input=="apaga la luz de la cocina")     //Comprobar el mensaje enviado
                {
                  digitalWrite(LED, HIGH);          //Poner en alto el foco de la cocina
                  Serial.println(BT_input);         //Mensaje de salida
                  Serial.println("LED is OFF");     //Mensaje de salida
                }


               else if (BT_input=="enciende la luz de la sala")    //Comprobar el mensaje enviado             
                {
                  digitalWrite(LED1, LOW);          //Poner en bajo el foco de la sala
                  Serial.println(BT_input);         //Mensaje de salida
                  Serial.println("LED1 is ON");     //Mensaje de salida
                }
                
                else if (BT_input=="apaga la luz de la sala")       //Comprobar el mensaje enviado 
                {
                  digitalWrite(LED1, HIGH);         //Poner en alto el foco de la sala
                  Serial.println(BT_input);         //Mensaje de salida
                  Serial.println("LED1 is OFF");    //Mensaje de salida
                }

               else if (BT_input=="enciende las luces")      //Comprobar el mensaje enviado             
                {
                  digitalWrite(LED, LOW);             //Poner en bajo el foco de la cocina
                  Serial.println(BT_input);           //Mensaje de salida
                  Serial.println("LED is ON");        //Mensaje de salida

                  digitalWrite(LED1, LOW);            //Poner en bajo el foco de la sala
                  Serial.println(BT_input);           //Mensaje de salida
                  Serial.println("LED1 is ON");       //Mensaje de salida
                }
                
                else if (BT_input=="apaga las luces")     //Comprobar el mensaje enviado 
                {
                  digitalWrite(LED, HIGH);            //Poner en alto el foco de la cocina
                  Serial.println(BT_input);           //Mensaje de salida
                  Serial.println("LED is OFF");       //Mensaje de salida

                  digitalWrite(LED1, HIGH);           //Poner en alto el foco de la sala
                  Serial.println(BT_input);           //Mensaje de salida
                  Serial.println("LED1 is OFF");      //Mensaje de salida
                }
                
                else                                  //Si no se cumple ninguna de la condiciones anteriores ejecuta el siguiente codigo
                {
                  Serial.println(BT_input);                       //Mensaje de salida
                  Serial.println("Send 'A' to get LED ON");       //Mensaje de salida
                  Serial.println("Send 'B' to get LED OFF");      //Mensaje de salida
                }
           
    }
 
}
