package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.processing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import java.io.IOException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;

public class EnrollmentForm extends CaptureForm
{
	private DPFPEnrollment enroller = DPFPGlobal.getEnrollmentFactory().createEnrollment();
	private DPFPTemplate template;
	EnrollmentForm(Frame owner) {
        
		super(owner);
	}
	
	@Override protected void init()
	{
		super.init();
		this.setTitle("Registrar tu huella");
		updateStatus();
	}

	@Override protected void process(DPFPSample sample) {
		super.process(sample);
		// Process the sample and create a feature set for the enrollment purpose.
		DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

		// Check quality of the sample and add to enroller if it's good
		if (features != null) try
		{
			//makeReport("The fingerprint feature set was created.");
                        makeReport("Las caracteristicas de la huella han sido creadas");
			enroller.addFeatures(features);		// Add feature set to template.
		}
		catch (DPFPImageQualityException ex) { }
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
					JOptionPane.showMessageDialog(EnrollmentForm.this, "La muestra de la huella no es valida,Intente de nuevo el registro de la huella", "Registro de huella", JOptionPane.ERROR_MESSAGE);
					start();
					break;
			}
		}
	}
	
	private void updateStatus()
	{
		// Show number of samples needed.
		setStatus(String.format("Numero de capturas necesarias para registro de huella %1$s", enroller.getFeaturesNeeded()));
	}
    ConexionBD con=new ConexionBD();
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
     //btnGuardar.setEnabled(false);
     //btnVerificar.grabFocus();
     } catch (SQLException ex) {
     //Si ocurre un error lo indica en la consola
     System.err.println("Error al guardar los datos de la huella."+ ex.getMessage());
     }finally{
     con.desconectar();
     }
   }
    
    
    public void consultar_http(String str){
                CloseableHttpClient httpclient = HttpClients.createDefault();
		ResponseHandler<JSONObject> responseHandler = (ResponseHandler<JSONObject>) new JsonHandlerOwn();
		
		try {
			//HttpGet httpget = new HttpGet("https://biometric.mohansoft.com/usuario/us="+jTxtUsuario.getText()+"&ps="+jPass.getPassword().toString());
                        HttpGet httpget = new HttpGet("http://localhost/api_biometric/activar_registro_participante/"+str);

			JSONObject responseBody = (JSONObject) httpclient.execute(httpget, responseHandler);


                        //esto cambia de acuerdo a los campos que se devuelven de la api
			Boolean statusCode = (Boolean) responseBody.get("respuesta");
			if(statusCode == true){
                                //cast
				JSONObject data = (JSONObject) responseBody;
                               
                                //String strUs=(String) data.get("usuario");
                                
                                
                                //aqui incoco el otro frame
                               
                                dispose();
                                
                                
			} else {
                                System.out.println("false");
				System.out.println(responseBody);
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
        
    
    
    }
}
