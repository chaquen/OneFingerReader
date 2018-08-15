package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager  
{
	String dir_archivo="C:\\xampp\\htdocs\\OneFingerReader\\logs\\logs.txt";
	
	public FileManager(){
		
		File archivo = new File(this.dir_archivo);
		
	}
    	
	public Boolean Escribir(String txt){
           try{
               String fin = System.getProperty( "line.separator" );
                java.util.Date fecha = new Date();
                Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String fec = formatter.format(fecha); 	
                FileWriter escribir = new FileWriter(this.dir_archivo,true);	
                String texto="";
                texto+=" -"+fec+"- {"+txt+"}- "+fin;

                     for(int i=0; i<texto.length();i++){
                         escribir.write(texto.charAt(i));
                     }

                     escribir.close();
                     return true;
           }catch(IOException iex){
               System.err.println("Error al guardar dato los datos de la huella."+ iex.getMessage());
               return false;
           } 
            
	   
	
	} 

    
}
