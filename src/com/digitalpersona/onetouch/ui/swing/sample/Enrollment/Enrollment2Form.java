package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Enrollment2Form extends CaptureForm 
{
    
        private DPFPEnrollment enroller = DPFPGlobal.getEnrollmentFactory().createEnrollment();
      
	//private DPFPVerification verificator = DPFPGlobal.getVerificationFactory().createVerification();
                //Esta variable tambien captura una huella del lector y crea sus caracteristcas para auntetificarla
        // o verificarla con alguna guardada en la BD
        private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
        
        
        private DPFPTemplate template;
        
        public DPFPFeatureSet featuresverificacion;
        
        public static String TEMPLATE_PROPERTY = "template";
        
      
         
	Enrollment2Form(Frame owner) {
            
		super(owner);          
                    
        }
    
	@Override protected void init()
	{
		super.init();
		this.setTitle("Registra tu huella");
		
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
                Boolean salir=true;    
                while(rs.next()){
                       byte templateBuffer[] = rs.getBytes("huella_binaria");
                       
                       
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
                            //actualizarHuella(id);
                            JOptionPane.showMessageDialog(null,"Esta huella ya esta registrada","Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                            salir=false;
                            break;
                        }
                        
                }
                    
                if(salir){
                
                    DPFPFeatureSet features2 = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

                    // Check quality of the sample and add to enroller if it's good
                    if (features2 != null) try
                    {
                            //makeReport("The fingerprint feature set was created.");
                            makeReport("Las caracteristicas de la huella han sido creadas");
                            enroller.addFeatures(features2);		// Add feature set to template.
                    }
                    catch (DPFPImageQualityException ex) { 
                        FileManager fl= new FileManager();
                        fl.Escribir("DPFPImageQualityException: "+ ex.getMessage());
                    }
                    finally {
                            updateStatus();

                            // Check if template has been created.
                            switch(enroller.getTemplateStatus())
                            {
                                    case TEMPLATE_STATUS_READY:	// report success and stop capturing
                                            stop();
                                            ((MainForm)getOwner()).setTemplate(enroller.getTemplate());
                                            this.template=((MainForm)getOwner()).getTemplate();

                                            guardarHuella(this.template);
                                            setPrompt("Huella almacenada");
                                            break;

                                    case TEMPLATE_STATUS_FAILED:	// report failure and restart capturing
                                            enroller.clear();
                                            stop();
                                            updateStatus();
                                            ((MainForm)getOwner()).setTemplate(null);
                                            JOptionPane.showMessageDialog(Enrollment2Form.this, "La muestra de la huella no es valida,Intente de nuevo el registro de la huella", "Registro de huella", JOptionPane.ERROR_MESSAGE);
                                            start();
                                            break;
                            }
                    }
                
                }   
                
                // Process the sample and create a feature set for the enrollment purpose.
            } catch (SQLException ex) {
                Logger.getLogger(Enrollment2Form.class.getName()).log(Level.SEVERE, null, ex);
                FileManager fl= new FileManager();
                fl.Escribir("SqlException: "+ex.getMessage());
                JOptionPane.showMessageDialog(null,"Se ha generado un error por favor intenta el registro de nuevo, si la falla persiste comunicate con el administrador de el sistema","Registro de Huella", JOptionPane.INFORMATION_MESSAGE);
            }
		
	}
	
	private void updateStatus()
	{
		// Show "False accept rate" value
		setStatus(String.format("Numero de capturas necesarias para registro de huella %1$s", enroller.getFeaturesNeeded()));
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
  
   /*
  * Guarda los datos de la huella digital actual en la base de datos
  */
    public void guardarHuella(DPFPTemplate template){
     //Obtiene los datos del template de la huella actual
     ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
     Integer tamañoHuella=template.serialize().length;

    //Pregunta el nombre de la persona a la cual corresponde dicha huella
    
     try {
     //Establece los valores para la sentencia SQL
     Connection c=con.conectar(); //establece la conexion con la BD
     PreparedStatement guardarStmt = c.prepareStatement("INSERT INTO participantes(huella_binaria,estado_registro) values(?,?)",Statement.RETURN_GENERATED_KEYS);

     
     guardarStmt.setBinaryStream(1, datosHuella,tamañoHuella);
     guardarStmt.setString(2, "por_registrar");
     //Ejecuta la sentencia
     guardarStmt.execute();
     
     ResultSet rs=guardarStmt.getGeneratedKeys();
     int insert_id = 0;
     if (rs.next()) {
        insert_id = rs.getInt(1);
     }
     guardarStmt.close();
          
     //consultar_http(Integer.toString(insert_id));
     JOptionPane.showMessageDialog(null,"Huella Guardada Correctamente");
     con.desconectar();
     try {
        Desktop desktop = java.awt.Desktop.getDesktop();
        URI oURL = new URI("http://localhost/Biometrico/registroUsuario.html?id="+insert_id);
        desktop.browse(oURL);
     } catch (Exception ex) {
          ex.printStackTrace();
          FileManager fl= new FileManager();
          fl.Escribir("Exception "+ ex.getMessage());
          JOptionPane.showMessageDialog(null,"Se ha generado un error por favor intenta el registro de nuevo, si la falla persiste comunicate con el administrador de el sistema","Registro de Huella", JOptionPane.INFORMATION_MESSAGE);
     }   
     //btnGuardar.setEnabled(false);
     //btnVerificar.grabFocus();
     } catch (SQLException ex) {
     //Si ocurre un error lo indica en la consola
                System.err.println("Error al guardar los datos de la huella."+ ex.getMessage());
                FileManager fl= new FileManager();
                fl.Escribir("SqlException:"+ ex.getMessage());
                JOptionPane.showMessageDialog(null,"Se ha generado un error por favor intenta el registro de nuevo, si la falla persiste comunicate con el administrador de el sistema","Registro de Huella", JOptionPane.INFORMATION_MESSAGE);
     }finally{
        con.desconectar();
     }
   }  
    
}
