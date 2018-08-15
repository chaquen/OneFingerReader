package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import java.io.File;
import java.io.FileWriter;

public class FileManager  
{
	String dir_archivo;
	
	public FileManager(String strRuta){
		this.dir_archivo=strRuta;
		File archivo = new File(strRuta);
		
	}
    	
	public Boolean Escribir(String txt){
	   String fin = System.getProperty( "line.separator" );
	   java.util.Date fecha = new Date();
	   Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           String fec = formatter.format(fecha); 	
	   FileWriter escribir = new FileWriter(this.dir_archivo);	
	   String texto=" -"+fec+"- {"+texto+"}- "+fin;
	
		for(int i=0; i<texto.length();i++){
	            escribir.write(texto.charAt(i));
		}

		escribir.close();
		return true;
	
	} 

    
}
