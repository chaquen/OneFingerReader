package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.verification.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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
		this.setTitle("Fingerprint Enrollment");
		updateStatus(0);
	}

	/*@Override protected void process(DPFPSample sample) {
		super.process(sample);

		// Process the sample and create a feature set for the enrollment purpose.
		DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

		// Check quality of the sample and start verification if it's good
		if (features != null)
		{
			// Compare the feature set with our template
			//DPFPVerificationResult result = 
			//	verificator.verify(features, ((MainForm)getOwner()).getTemplate());
                        verificarHuella(features,((MainForm)getOwner()).getTemplate());
                        DPFPVerificationResult result = verificator.verify(features, ((MainForm)getOwner()).getTemplate());
                    
			updateStatus(result.getFalseAcceptRate());
			if (result.isVerified())
				makeReport("The fingerprint was VERIFIED.");
			else
				makeReport("The fingerprint was NOT VERIFIED.");
		}
	}*/
        @Override protected void process(DPFPSample sample) {
            try {
                super.process(sample);
                
               
                DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                Connection c=con.conectar();
                //Obtiene la plantilla correspondiente a la persona indicada
                PreparedStatement verificarStmt = c.prepareStatement("SELECT huella_binaria,pri_nombre FROM participantes");
               
                ResultSet rs = verificarStmt.executeQuery();
                
                    int i=0;
                    while(rs.next()){
                       byte templateBuffer[] = rs.getBytes("huella_binaria");
                       
                       String strNombre=rs.getString("pri_nombre");
                       
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
                        JOptionPane.showMessageDialog(null, "Bienvenido "+strNombre,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                        return;
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
		setStatus(String.format("False Accept Rate (FAR) = %1$s", FAR));
	}
    ConexionBD con=new ConexionBD(); 
   /* public void verificarHuella(String nom) {
        try {
        //Establece los valores para la sentencia SQL
        Connection c=con.conectar();
        //Obtiene la plantilla correspondiente a la persona indicada
        PreparedStatement verificarStmt = c.prepareStatement("SELECT huehuella FROM somhue WHERE huenombre=?");
        verificarStmt.setString(1,nom);
        ResultSet rs = verificarStmt.executeQuery();

        //Si se encuentra el nombre en la base de datos
        if (rs.next()){
        //Lee la plantilla de la base de datos
        byte templateBuffer[] = rs.getBytes("huehuella");
        //Crea una nueva plantilla a partir de la guardada en la base de datos
        DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
        //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
        setTemplate(referenceTemplate);

        // Compara las caracteriticas de la huella recientemente capturda con la
        // plantilla guardada al usuario especifico en la base de datos
        DPFPVerificationResult result = Verificador.verify(featuresverificacion, getTemplate());

        //compara las plantilas (actual vs bd)
        if (result.isVerified())
        JOptionPane.showMessageDialog(null, "Las huella capturada coinciden con la de "+nom,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
        else
        JOptionPane.showMessageDialog(null, "No corresponde la huella con "+nom, "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);

        //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
        } else {
        JOptionPane.showMessageDialog(null, "No existe un registro de huella para "+nom, "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);
        }
        } catch (SQLException e) {
        //Si ocurre un error lo indica en la consola
        System.err.println("Error al verificar los datos de la huella.");
        }finally{
           con.desconectar();
        }
   }*/
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
       PreparedStatement identificarStmt = c.prepareStatement("SELECT huehuella FROM participantes");
       ResultSet rs = identificarStmt.executeQuery();

       //Si se encuentra el nombre en la base de datos
       while(rs.next()){
       //Lee la plantilla de la base de datos
       byte templateBuffer[] = rs.getBytes("huehuella");
       String nombre=rs.getString("huenombre");
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
       JOptionPane.showMessageDialog(null, "No existe ningÃºn registro que coincida con la huella", "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);
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
}
