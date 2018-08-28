package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;

import java.io.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.digitalpersona.onetouch.*;
import java.net.URI;

public class MainForm extends JFrame
{
	public static String TEMPLATE_PROPERTY = "template";
	private DPFPTemplate template;

	public class TemplateFileFilter extends javax.swing.filechooser.FileFilter {
		@Override public boolean accept(File f) {
			return f.getName().endsWith(".fpt");
		}
		@Override public String getDescription() {
			return "Fingerprint Template File (*.fpt)";
		}
	}
	MainForm() {
            
        setState(Frame.NORMAL);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setTitle("Verifica tu huella");
		setResizable(false);

		final JButton enroll = new JButton("Registrar Huella");
        enroll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onEnroll(); }});
		
		final JButton verify = new JButton("Verificar Huella");
        verify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onVerify(); }});

		/*final JButton save = new JButton("Save Fingerprint Template");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onSave(); }});*/

		/*final JButton load = new JButton("Read Fingerprint Template");
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onLoad(); }});*/

		final JButton quit = new JButton("Salir");
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }});
		
		this.addPropertyChangeListener(TEMPLATE_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				//verify.setEnabled(template != null);
				//save.setEnabled(template != null);
				if (evt.getNewValue() == evt.getOldValue()) return;
				//if (template != null)
					//JOptionPane.showMessageDialog(MainForm.this, "El lector esta listo para la captura de huellas", "Lector de huellas", JOptionPane.INFORMATION_MESSAGE);
			}
		});
			
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(4, 1, 0, 5));
		center.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
		center.add(enroll);
		center.add(verify);
		//center.add(save);
		//center.add(load);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		bottom.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		bottom.add(quit);

		setLayout(new BorderLayout());
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.PAGE_END);
		
		pack();
		setSize((int)(getSize().width*1.6), getSize().height);
                setLocationRelativeTo(null);
		setTemplate(null);
		setVisible(true);
                  try {
                    Desktop desktop = java.awt.Desktop.getDesktop();
                    URI oURL = new URI("http://localhost/Biometrico/");
                    desktop.browse(oURL);
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }   
	}
	
	private void onEnroll() {
		Enrollment2Form form = new Enrollment2Form(this);
		form.setVisible(true);
	}

	private void onVerify() {
		VerificationForm form = new VerificationForm(this);
		form.setVisible(true);
	}

	public DPFPTemplate getTemplate() {
		return template;
	}
	public void setTemplate(DPFPTemplate template) {
		DPFPTemplate old = this.template;
		this.template = template;
		firePropertyChange(TEMPLATE_PROPERTY, old, template);
	}
	
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainForm();
            }
        });
    }

}
