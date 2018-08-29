/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Adrian
 */
public class ValidarTerminos
	extends JDialog
{
    
    ConexionBD con=new ConexionBD(); 
    int _id_event;
    long _id_user;
    int _acepto;
    JLabel texto = new JLabel();
    JCheckBox option1 = new JCheckBox("Si, Acepto uso de datos");
    JCheckBox option2 = new JCheckBox("Si, Acepto uso de foto");
	
    public ValidarTerminos(Frame owner,long id_user,int id_event,String nombre) {
        super (owner, true);
        setTitle("Términos y Condiciones");

		setLayout(new BorderLayout());
		rootPane.setBorder(BorderFactory.createEmptyBorder(80, 80, 80, 80));
                
                
                JPanel right = new JPanel(new BorderLayout());
                right.setPreferredSize(new Dimension(80, 80));
		right.setBackground(Color.getColor("control"));
		right.add(texto, BorderLayout.PAGE_START);
		
                
		JPanel center = new JPanel(new BorderLayout());
                
		center.setBackground(Color.getColor("control"));
		center.add(option1, BorderLayout.WEST);
		center.add(option2, BorderLayout.EAST);
                
                JPanel button = new JPanel(new BorderLayout());
                button.setBorder(new EmptyBorder(20, 20, 20, 20));
                JButton aceptar = new JButton("Aceptar");
                
                aceptar.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { 
                        String valor_datos="";
                        String valor_foto="";
                        if(option1.isSelected()) {
                            valor_datos="SI";
                        }else{
                            valor_datos="NO";
                        }
                        if(option2.isSelected()){
                            valor_foto="SI";
                        }else{
                            valor_foto="NO";
                        }
                        if(valor_foto!="" ){
                            registrar_en_evento(_id_user,_id_event,valor_datos,valor_foto);

                            setVisible(false);     
                        }else{
                            JOptionPane.showMessageDialog(null,"Debes seleccionar una opción,para poder registrar al participante","Términos y condiciones", JOptionPane.INFORMATION_MESSAGE);
                        }
                        



                    }
                });
                JButton cancelar = new JButton("Cancelar");
                
                cancelar.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { 
                        
                            setVisible(false);     
                        
                        



                    }
                });
                button.add(aceptar, BorderLayout.CENTER);
                button.add(cancelar,BorderLayout.PAGE_END);
                
                add(right,BorderLayout.PAGE_START);
                add(center,BorderLayout.CENTER);
                add(button,BorderLayout.PAGE_END);
                
                
                
                
                
		
		this.addComponentListener(new ComponentAdapter() {
			@Override public void componentShown(ComponentEvent e) {
                                

				init(id_user,id_event,nombre);
				
                                
			}
			@Override public void componentHidden(ComponentEvent e) {
				
			}
			
		});
		
		pack();
        setLocationRelativeTo(null);
	}
    protected void init(long id_u,int id_e,String nombre)
    {
	this.texto.setText("Bienvenido "+nombre);       
        _id_user=id_u;
        _id_event=id_e;
        
        	
    }
    
      
    //funcion para registrar el asistente
    public void registrar_en_evento(long id_u,int id_e,String acepta_datos,String acepta_foto){
        Connection cc=con.conectar(); //establece la conexion con la BD
        PreparedStatement guardarStmt2;
        try {
            guardarStmt2 = cc.prepareStatement("INSERT INTO detalle_participantes (user_id , event_id, acepta_terminos, acepta_terminos_foto,created_at ,updated_at) VALUES(?,?,?,?,?,?)" );
            guardarStmt2.setLong(1,id_u);
            guardarStmt2.setInt(2, id_e);
            guardarStmt2.setString(3, acepta_datos);
            guardarStmt2.setString(4, acepta_foto);
            guardarStmt2.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()) );
            guardarStmt2.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            //Ejecuta la sentencia
            guardarStmt2.execute();
            guardarStmt2.close();
            //JOptionPane.showMessageDialog(null,"Huella Guardada Correctamente");
            con.desconectar();
        } catch (SQLException ex) {
            Logger.getLogger(ValidarTerminos.class.getName()).log(Level.SEVERE, null, ex);
            FileManager fl= new FileManager();
            fl.Escribir("SQLException: "+ ex.getMessage());
        }
    }    
    
    
    
	
	
	
}
