package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.verification.*;
import com.mysql.jdbc.exceptions.MySQLDataException;
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
import java.text.DateFormat;
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
                
                Connection cc=con.conectar();
                //Obtiene la plantilla correspondiente a la persona indicada
                PreparedStatement verificarStmt2 = cc.prepareStatement("SELECT id FROM eventos WHERE estado_evento = 'activo' ");     
                ResultSet rs2 = verificarStmt2.executeQuery();
                int id_e=0;
                while(rs2.next()){
                    id_e= rs2.getInt("id");
                    
                }
                
                
                
                DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                Connection c=con.conectar();
                //Obtiene la plantilla correspondiente a la persona indicada
                PreparedStatement verificarStmt = c.prepareStatement("SELECT documento,CONCAT(pri_nombre,' ',pri_apellido) as nombre,updated_at,huella_binaria FROM participantes");     
                ResultSet rs = verificarStmt.executeQuery();
                
                    int i=0;
                    int id_u=0;
                    Boolean no_existe=false;
                while(rs.next()){
                       byte templateBuffer[] = rs.getBytes("huella_binaria");
                       
                       
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
                            String nombre=rs.getString("nombre");
                            id_u=rs.getInt("documento");
                            Date fecha=rs.getDate("updated_at");//fecha de registro actualizacion de la huella
                            //Date hoy=
                            //Date fecha2=sumarRestarDiasFecha(fecha,730);//dos años despues 
                            String msnAdi="";
                        //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
                            actualizarHuella(id_u,id_e,nombre);
                            
                            return;
                        }else{
                            no_existe=true;
                        }
                        
                      
                }
                if(no_existe){
                    JOptionPane.showMessageDialog(null, "Esta usuario no aprece registrado, por favor registro un usuario nuevo","Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                }
                //fin while
                    
                   
                
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
 
  
  
  public DPFPTemplate getTemplate() {
        return template;
    }

  public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template;
	this.template = template;
	firePropertyChange(TEMPLATE_PROPERTY, old, template);
  }
  
  
  
   public void actualizarHuella(int id_u,int id_e,String nombre){
     //Obtiene los datos del template de la huella actual
     

    //Pregunta el nombre de la persona a la cual corresponde dicha huella
    
     try {
     //Establece los valores para la sentencia SQL
     Connection c=con.conectar(); //establece la conexion con la BD
     PreparedStatement guardarStmt = c.prepareStatement("UPDATE participantes SET estado_registro = ?, updated_at = ? WHERE documento = ? ");

     
     
     guardarStmt.setString(1, "verificado");
     guardarStmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
     guardarStmt.setInt(3, id_u);
     
     //Ejecuta la sentencia
     guardarStmt.execute();
     
    
     guardarStmt.close();
   
     //JOptionPane.showMessageDialog(null,"Huella Guardada Correctamente");
     con.desconectar();
     
     Connection c2=con.conectar();
                //Obtiene la plantilla correspondiente a la persona indicada
                PreparedStatement verificarStmt2 = c2.prepareStatement("SELECT id FROM detalle_participantes WHERE user_id = ? AND event_id = ?  ");     
                verificarStmt2.setInt(1, id_u);
                verificarStmt2.setInt(2, id_e);
                ResultSet rs2 = verificarStmt2.executeQuery();
                int id_ex=0;
                while(rs2.next()){
                    id_ex= rs2.getInt("id");
                    
                }
                
     con.desconectar();           
                
     
     if(id_ex==0){
         Connection cc=con.conectar(); //establece la conexion con la BD
        PreparedStatement guardarStmt2 = cc.prepareStatement("INSERT INTO detalle_participantes (user_id , event_id ,created_at ,updated_at) VALUES(?,?,?,?)" );




        guardarStmt2.setInt(1,id_u);
        guardarStmt2.setInt(2, id_e);
        guardarStmt2.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()) );
        guardarStmt2.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));

        //Ejecuta la sentencia
        guardarStmt2.execute();


        guardarStmt.close();

        //JOptionPane.showMessageDialog(null,"Huella Guardada Correctamente");
        con.desconectar();
        
        JOptionPane.showMessageDialog(null, "Bienvenido "+nombre,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
     }else{
         JOptionPane.showMessageDialog(null, nombre+", ya te habias registrado a este evento","Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
     }
     
     
     //btnGuardar.setEnabled(false);
     //btnVerificar.grabFocus();
     }catch(MySQLDataException exm){
         System.err.println("Error al guardar dato los datos de la huella."+ exm.getMessage());
     }
     catch (SQLException ex) {
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
