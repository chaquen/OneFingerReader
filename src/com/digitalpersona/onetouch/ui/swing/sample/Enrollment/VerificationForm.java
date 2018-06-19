package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.verification.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.util.Calendar;

public class VerificationForm extends CaptureForm 
{
	private DPFPVerification verificator = DPFPGlobal.getVerificationFactory().createVerification();
                //Esta variable tambien captura una huella del lector y crea sus caracteristcas para auntetificarla
        // o verificarla con alguna guardada en la BD
        private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
        
        
        private DPFPTemplate template;
        
        public DPFPFeatureSet featuresverificacion;
        
        public static String TEMPLATE_PROPERTY = "template";
        
      
         
	VerificationForm(Frame owner) {
            
		super(owner);
          
                    
        }
    
	@Override protected void init()
	{
		super.init();
		this.setTitle("Verifica tu huella");
		updateStatus(0);
	}

	
        @Override protected void process(DPFPSample sample) {
            try {
                super.process(sample);
                
               
                DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                Connection c=con.conectar();
                //Obtiene la plantilla correspondiente a la persona indicada
                PreparedStatement verificarStmt = c.prepareStatement("SELECT id,CONCAT(pri_nombre,' ',pri_apellido) as nombre,updated_at,huella_binaria FROM participantes");     
                ResultSet rs = verificarStmt.executeQuery();
                
                    int i=0;
                    int id=0;
                    
                    while(rs.next()){
                       byte templateBuffer[] = rs.getBytes("huella_binaria");
                       
                       String nombre=rs.getString("nombre");
                       id=rs.getInt("id");
                       Date fecha=rs.getDate("updated_at");//fecha de registro actualizacion de la huella
                       //Date hoy=
                       //Date fecha2=sumarRestarDiasFecha(fecha,730);//dos años despues 
                       String msnAdi="";
                       //if(hoy.after(fecha)){
                         //  msnAdi=", Ya han pasado mas de dos años desde tu ultima vez, por favor actualiza tus datos";
                       //}
                       
                       
                       
       
       
                       DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                        //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
                        setTemplate(referenceTemplate);

                        // Compara las caracteriticas de la huella recientemente capturda con la
                        // alguna plantilla guardada en la base de datos que coincide con ese tipo
                        DPFPVerificationResult result = Verificador.verify(features, getTemplate());

                        //compara las plantilas (actual vs bd)
                        //Si encuentra correspondencia dibuja el mapa
                        //e indica el nombre de la persona que coincidió.
                        if (result.isVerified()){
                        //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
                            actualizarHuella(id);
                            JOptionPane.showMessageDialog(null, "Bienvenido "+nombre,"Verificacion de Huella"+msnAdi, JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }else{
                            
                        }
                        
                      System.out.print(i++);
                    }
                    
                   
                
                // Process the sample and create a feature set for the enrollment purpose.
            } catch (SQLException ex) {
                Logger.getLogger(VerificationForm.class.getName()).log(Level.SEVERE, null, ex);
            }
		
	}
	
	private void updateStatus(int FAR)
	{
		// Show "False accept rate" value
		setStatus(String.format("(FAR) = %1$s", FAR));
	}
    ConexionBD con=new ConexionBD(); 
 
    public void verificarHuella(DPFPFeatureSet features,DPFPTemplate template){
        
        
        //aqui busco el blob en la bd
    
    }

 /**
  * Identifica a una persona registrada por medio de su huella digital
  */
  public void identificarHuella() throws IOException{
     try {
       //Establece los valores para la sentencia SQL
       Connection c=con.conectar();

       //Obtiene todas las huellas de la bd
       PreparedStatement identificarStmt = c.prepareStatement("SELECT CONCAT(pri_nombre,pri_apellido) as nombre,updated_at,huella_binaria FROM participantes");
       ResultSet rs = identificarStmt.executeQuery();

       //Si se encuentra el nombre en la base de datos
       while(rs.next()){
       //Lee la plantilla de la base de datos
       byte templateBuffer[] = rs.getBytes("huella_binaria");
       String nombre=rs.getString("nombre");
       Date fecha=rs.getDate("update_at");
       
       sumarRestarDiasFecha(fecha,730);
       //Crea una nueva plantilla a partir de la guardada en la base de datos
       DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
       //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
       setTemplate(referenceTemplate);

       // Compara las caracteriticas de la huella recientemente capturda con la
       // alguna plantilla guardada en la base de datos que coincide con ese tipo
       DPFPVerificationResult result = Verificador.verify(featuresverificacion, getTemplate());

       //compara las plantilas (actual vs bd)
       //Si encuentra correspondencia dibuja el mapa
       //e indica el nombre de la persona que coincidiÃ³.
       if (result.isVerified()){
       //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
       JOptionPane.showMessageDialog(null, "Las huella capturada es de "+nombre,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
       return;
                               }
       }
       //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
       JOptionPane.showMessageDialog(null, "No existe ninguna registro que coincida con la huella", "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);
       setTemplate(null);
       } catch (SQLException e) {
       //Si ocurre un error lo indica en la consola
       System.err.println("Error al identificar huella dactilar."+e.getMessage());
       }finally{
       con.desconectar();
       }
   }
  
  public DPFPTemplate getTemplate() {
        return template;
    }

  public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template;
	this.template = template;
	firePropertyChange(TEMPLATE_PROPERTY, old, template);
  }
  
  
  
   public void actualizarHuella(int id){
     //Obtiene los datos del template de la huella actual
     

    //Pregunta el nombre de la persona a la cual corresponde dicha huella
    
     try {
     //Establece los valores para la sentencia SQL
     Connection c=con.conectar(); //establece la conexion con la BD
     PreparedStatement guardarStmt = c.prepareStatement("UPDATE participantes SET estado_registro = ? WHERE id = ? ");

     
     
     guardarStmt.setString(1, "verificado");
     guardarStmt.setInt(2, id);
     //Ejecuta la sentencia
     guardarStmt.execute();
     
    
     guardarStmt.close();
   
     //JOptionPane.showMessageDialog(null,"Huella Guardada Correctamente");
     con.desconectar();
     //btnGuardar.setEnabled(false);
     //btnVerificar.grabFocus();
     } catch (SQLException ex) {
     //Si ocurre un error lo indica en la consola
     System.err.println("Error al guardar los datos de la huella."+ ex.getMessage());
     }finally{
     con.desconectar();
     }
   }
    
   public Date sumarRestarDiasFecha(Date fecha, int dias){	
 
	
      Calendar calendar = Calendar.getInstance();
	
      calendar.setTime(fecha); // Configuramos la fecha que se recibe
	
      calendar.add(Calendar.DAY_OF_YEAR, dias);  // numero de días a añadir, o restar en caso de días<0
	
 	
      return calendar.getTime(); // Devuelve el objeto Date con los nuevos días añadidos
	
 
	
 }
    
}
