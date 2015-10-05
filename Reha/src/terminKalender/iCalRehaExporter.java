package terminKalender;

import hauptFenster.Reha;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Vector;

import CommonTools.ZeitFunk;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import dialoge.EmailDialog;
import emailHandling.EmailSendenExtern;
import systemEinstellungen.SystemConfig;

public class iCalRehaExporter {
	boolean fehler = false;
	String plandatei = "";
	Vector<String> veczeilen = new Vector<String>();
	Vector<String> test = new Vector<String>();
	String inhaber = "";
	StringBuffer buf = new StringBuffer();
	public iCalRehaExporter(){
		if( (plandatei=oeffneDateiDialog()).equals("")){
			JOptionPane.showMessageDialog(null,"Es wurde keine Plandatei ausgewählt");
		}else{
			 if( (veczeilen = plandateiEinlesen()).size() <= 0 ){
				 JOptionPane.showMessageDialog(null,"Die Datei "+plandatei+" ist keine gültige Rehaplan-Datei");
			 }else{
				 inhaber = test.get(test.size()-1);
				 //Testen ob Vor und Nachname im Dateiname enthalten sind
				 //ist dies nicht der Fall FrageDialog ob trotzdem ICS produziert werden soll mit Vor- Und Nachname und Dateiname im Text
				 try{
					 if( (!inhaber.contains(Reha.thisClass.patpanel.patDaten.get(2))) ||
							 (!inhaber.contains(Reha.thisClass.patpanel.patDaten.get(3)))	 ){
						 String meldung = "<html><b>Achtung!</b><br>Sie versuchen dem Patient<b> -> "+Reha.thisClass.patpanel.patDaten.get(2)+", "+
								 Reha.thisClass.patpanel.patDaten.get(3)+" <- </b>, einen Rehaplan per Email zu senden<br>mit dem Dateiname: <b>"+inhaber+" </b></html>";
						 int frage = JOptionPane.showConfirmDialog(null, meldung,"Wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
						 if(frage != JOptionPane.YES_OPTION){
							 return;
						 }
					 }
					 erzeugeIcs(); 
					 String emailaddy = Reha.thisClass.patpanel.patDaten.get(50);
					
					 
					 String recipient = emailaddy+((Boolean) SystemConfig.hmIcalSettings.get("aufeigeneemail") ? ","+SystemConfig.hmEmailExtern.get("SenderAdresse") : "");
										 
					 String[] aufDat = {Reha.proghome+"temp/"+Reha.aktIK+"/iCal-RehaTermine.ics","iCal-RehaTermine.ics"};
					 ArrayList<String[]> attachments = new ArrayList<String[]>();
					 attachments.add(aufDat);
					 String mailtext = SystemConfig.hmAdrPDaten.get("<Pbanrede>")+
							 ",\nwie gewünscht senden wir Ihnen hiermit Ihre Reha-Termine im RTA\n\nMit freundlichen Grüßen\nIhr Planungsteam im RTA ";
					 
					 
					 Reha.thisFrame.setCursor(Reha.thisClass.wartenCursor);
					 EmailDialog emlDlg = new EmailDialog(Reha.thisFrame,"Ihre Reha-Termine als ICS Datei",recipient ,(String)SystemConfig.hmIcalSettings.get("betreff"),
								mailtext,attachments,(Integer)SystemConfig.hmIcalSettings.get("postfach"), (Boolean)SystemConfig.hmIcalSettings.get("direktsenden")	);
						emlDlg.setPreferredSize(new Dimension(575,370));
						emlDlg.setLocationRelativeTo(null);
						//emlDlg.setLocation(pt.x-350,pt.y+100);
						emlDlg.pack();
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								//emlDlg.setTextCursor(0);		
							}
						});
						
						emlDlg.setVisible(true);
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								//emlDlg.setTextCursor(0);		
							}
						});
					 
				 }catch(Exception ex){
					 Reha.thisFrame.setCursor(Reha.thisClass.normalCursor);
					 JOptionPane.showMessageDialog(null,"Es ist ein Fehler beim ICS-Export aufgetreten");
				 }
				 
			 }
		}
	}
	
	private boolean erzeugeIcs(){
		//{"08.10.2015",4,40,"12:00",720,15,"Blutdruckmessung 3.OG",0,{89,239,39},{0,0,0},8}
		//macheRehaVevent(String datum, String start, String end, String titel, String beschreibung,boolean warnen)
		buf.setLength(0);
		buf.trimToSize();
		buf.append(ICalGenerator.macheKopf());
		String[] parts = null;
		for(int i = 0; i < veczeilen.size(); i++){
			try{
				parts = veczeilen.get(i).split(",");
				buf.append(ICalGenerator.macheRehaVevent(DatFunk.sDatInSQL(parts[0].replace("{", "").replace("\"","")).replace("-", ""), 
						parts[3].replace("\"", "").replace(":", "")+"00",
						ZeitFunk.ZeitPlusMinuten(parts[3].replace("\"", ""), parts[5].replace("\"", "")).replace(":", "")+"00",
						parts[3].replace("\"", "")+"-"+parts[6].replace("\"", ""),
						inhaber,
						false) );				
			}catch(Exception ex){
				return false;
			}
		}
		buf.append(ICalGenerator.macheEnd());
		FileOutputStream outputFile;
		try {
			outputFile = new  FileOutputStream(Reha.proghome+"temp/"+Reha.aktIK+"/iCal-RehaTermine.ics");
	        OutputStreamWriter out = new OutputStreamWriter(outputFile, "UTF8");
			BufferedWriter bw = null;
			bw = new BufferedWriter(out);
			bw.write(buf.toString());
			bw.flush();
			bw.close();
			out.close();
			outputFile.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String giveMePart(String[] string, int part){
		return string[part];
	}
	
	private Vector<String> plandateiEinlesen(){
		Vector<String> vecz = new Vector<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(plandatei));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				if(zeile.startsWith("{\"")){
					vecz.add(zeile);	
				}
				test.add(zeile);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}	
		} 
		/*
		System.out.println("Elemente = "+vecz.size());
		for(int i = 0; i < vecz.size();i++){
			System.out.println("Element "+i+" = "+vecz.get(i));
		}
		*/
		return (Vector<String>)vecz.clone();
	}
	
	private String oeffneDateiDialog(){
		String sret = "";
		final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final File file = new File((String)SystemConfig.hmIcalSettings.get("rehaplanverzeichnis"));

        chooser.setCurrentDirectory(file);

        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    final File f = (File) e.getNewValue();
                }
            }
        });
        chooser.setVisible(true);
        Reha.thisFrame.setCursor(Reha.thisClass.normalCursor);
        final int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();
            //String inputVerzStr = inputVerzFile.getPath();

            if(inputVerzFile.getName().trim().equals("")){
            	sret = "";
            }else{
            	sret = inputVerzFile.getPath().trim().replace("\\", "/");	
            }
        }else{
        	sret = ""; //vorlagenname.setText(SystemConfig.oTerminListe.NameTemplate);
        }
        chooser.setVisible(false); 
		
		
		return sret;
	}

}
