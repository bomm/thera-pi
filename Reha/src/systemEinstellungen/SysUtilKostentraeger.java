package systemEinstellungen;

import hauptFenster.Reha;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import CommonTools.SqlInfo;
import CommonTools.JCompTools;
import sqlTools.PLServerAuslesen;
import terminKalender.DatFunk;

import CommonTools.INIFile;
import CommonTools.INITool;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SysUtilKostentraeger extends JXPanel implements KeyListener, ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JXTable ktrtbl = null;
	MyKtraegerModel ktrmod = null;
	JButton[] but = {null,null,null,null};
	INIFile inif = INITool.openIni(Reha.proghome+"ini/"+Reha.aktIK+"/", "ktraeger.ini");
	private TableCellRenderer JLabelRenderer = null; //= new DefaultTableRenderer(new MappedValue(StringValues.EMPTY, IconValues.ICON), JLabel.CENTER);
	PLServerAuslesen plServer = null;	
	boolean debug = false;
	JProgressBar progress = null;
	Vector<String> vKassenTest = new Vector<String>();
	Vector<Vector<String>> vDummyTest = new Vector<Vector<String>>();
	public SysUtilKostentraeger(){
		super(new BorderLayout());
		//System.out.println("Aufruf SysUtilKostentraeger");
		this.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));
		/****/
		setBackgroundPainter(Reha.thisClass.compoundPainter.get("SystemInit"));
		/****/
		JLabelRenderer = new KtreagerTblRenderer();
	     add(getVorlagenSeite());
	     /*
		*/
		return;
	}
	/************** Beginn der Methode für die Objekterstellung und -platzierung *********/
	private JPanel getVorlagenSeite(){
        //                                      1.            2.    3.    4.     5.     6.    7.      8.     9.  10.
		FormLayout lay = new FormLayout("right:max(60dlu;p), 4dlu, 40dlu, 4dlu, 40dlu, 4dlu, 40dlu, 4dlu, 40dlu, 40dlu",
       //1.    2. 3.   4.   5.    6.  7.  8.    9.  10.  11. 12. 13.  14.  15. 16.  17. 18.  19.   20.    21.   22.   23.
		"p, 10dlu,p, 10dlu,100dlu,p,10dlu, p, 10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p,  10dlu ,10dlu, 10dlu, p");
		
		PanelBuilder builder = new PanelBuilder(lay);
		builder.setDefaultDialogBorder();
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();
		
		builder.addSeparator("Kostenträgerdateien abholen",cc.xyw(1,1,10));
		but[0] = new JButton("Serverkontakt herstellen");
		but[0].setActionCommand("serverkontakt");
		but[0].addActionListener(this);
		builder.add(but[0],cc.xy(1,3));
		
		ktrmod = new MyKtraegerModel();
		ktrmod.setColumnIdentifiers(new String[] {"Kostenträger","gültig ab","Dateiname","DB Status",""});
		ktrtbl = new JXTable(ktrmod);
		ktrtbl.getColumn(1).setMaxWidth(75);
		ktrtbl.getColumn(2).setMinWidth(0);
		ktrtbl.getColumn(2).setMaxWidth(110);
		ktrtbl.getColumn(3).setMinWidth(0);
		ktrtbl.getColumn(3).setMaxWidth(60);
		ktrtbl.getColumn(3).setCellRenderer(JLabelRenderer);
		ktrtbl.getColumn(4).setMinWidth(0);
		ktrtbl.getColumn(4).setMaxWidth(0);

		ktrtbl.setSortable(false);
		JScrollPane jscr = JCompTools.getTransparentScrollPane(ktrtbl);
		jscr.validate();
		builder.add(jscr,cc.xywh(1,5,10,1));
		progress = new JProgressBar();
		progress.setStringPainted(true);
		builder.add(progress,cc.xywh(1,6,10,1));
		
		builder.addSeparator("Gewählte Datei in eigene Kostenträger einlesen",cc.xyw(1,8,10));
		
		but[1] = new JButton("abholen und verarbeiten");
		but[1].setActionCommand("abholen");
		but[1].addActionListener(this);
		builder.add(but[1],cc.xy(1,10));
		return builder.getPanel();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("serverkontakt")){
			/*
		     try{
					starteSession("","");
		     } catch (IOException e1) {
					e1.printStackTrace();
		     }
			 */
			try{
				startePLSession();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		     return;
		}
		if(cmd.equals("abholen")){
			
			
			/*
			if( row >= 0){
				try {
					holeKtraeger((String)ktrmod.getValueAt(row, 2),null);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			*/
			final int row = ktrtbl.getSelectedRow();
			if(row >= 0){
				new SwingWorker<Void,Void>(){

					@Override
					protected Void doInBackground() throws Exception {
						try{
							vKassenTest.clear();
							plServer = new PLServerAuslesen();
							Vector<Vector<String>> vec = PLServerAuslesen.holeFelder("select kttext from ktdateien where id = '"+ktrtbl.getValueAt(row,4).toString()+"' LIMIT 1");
							plServer.schliessePLConnection();
							holeKtraeger(ktrtbl.getValueAt(row,2).toString(),vec.get(0).get(0));
							if(vKassenTest.size() > 0){
								int frage = JOptionPane.showConfirmDialog(null,"<html>Sie haben <b>"+Integer.toString(vKassenTest.size())+" Krankenkassen</b> in Ihrem eigenen Kassenstamm<br>"+
										"die in der eingelesenen Kostenträgerdatei enthalten sind.<br>Möglicherweise sind eine oder mehrere Kassen <b>von Änderungen betroffen!</b><br><br>Wollen Sie Ihre Kassen "+
										"jetzt auf Änderungen hin prüfen lassen?<br><br></html>",
										"Wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
								if(frage==JOptionPane.YES_OPTION){
									doKassenTest();
								}
								
							}
							JOptionPane.showMessageDialog(null, "<html><b>Feddisch mit dem Abgleich der Kassen</b></html>");
						}catch(Exception ex){
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null,"Fehler beim Bezug und der Verarbeitung der Kostenträgerdatei");
						}
						return null;
					}
					
				}.execute();
			}
			return;
		}
		
	}
	private void startePLSession(){
		try{
			plServer = new PLServerAuslesen();
			Vector<Vector<String>> vec = PLServerAuslesen.holeFelder("select ktart,DATE_FORMAT(ktgueltigab,'%d.%m.%Y') AS gueltig,ktdatei,'',id from ktdateien Order by ktart");
			plServer.schliessePLConnection();
			ktrmod.setRowCount(0);
			for(int i = 0; i < vec.size();i++){
				ktrmod.addRow((Vector<?>)vec.get(i).clone());	
			}
			setFlags();
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Kein Kontakt zum Preislisten-/Kostträgerserver");
		}
	}
	private void starteSession(String land,String jahr) throws IOException{
		//String urltext = "http://www.gkv-datenaustausch.de/Leistungserbringer_Sole_Kostentraegerdateien.gkvnet";
		//String urltext = "http://www.gkv-datenaustausch.de/Leistungserbringer_Sole_Kotr.gkvnet";
		String urltext = "http://www.gkv-datenaustausch.de/leistungserbringer/sonstige_leistungserbringer/kostentraegerdateien_sle/kostentraegerdateien.jsp";
		String text = null;
		try{
		URL url = new URL(urltext);
		   
		      URLConnection conn = url.openConnection();
		      ////System.out.println(conn.getContentEncoding());
		      BufferedReader inS = null;
		      try{
		    	  inS = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
		      }catch(Exception ex){
		    	  JOptionPane.showMessageDialog(null, "Die Webseite\n"+urltext+"\nist derzeit nicht erreichbar!\n"+
		    			  "Überprüfen Sie Ihre Internetverbindung und überprüfen Sie ggfls.\n"+
		    			  "die o.g. Adresse mit Hilfe Ihres Browsers auf Verfügbarkeit");
		    	  return;
		      }
		      int durchlauf = 0;
		      //Vector<Vector<String>> ktraegerdat = new Vector<Vector<String>>();
		      Vector<String> kassendat = new Vector<String>();
		      int index;
		      boolean gestartet = false;
		      String saveLink = "";
		      String[] datLink = null;
		      while ( (text  = inS.readLine())!= null ) {
		    	  text = makeUTF8(text);
		    	  
		    	  if(durchlauf > 0){
		    		  if(text.startsWith("<a href=\"")){
		    			  //saveLink = text;
		    			  datLink = getDateiUndLink(text);
		    		  }
		        	  if(text.indexOf("<strong>Kosten") >= 0){
		        		  kassendat.clear();
		        		  
		        		  text = text.replace("<strong>", "");
		        		  text = text.replace("</strong>", "");
		        		  
		        		  if(text.indexOf("AOK")> 0){
		        			  text = "AOK-Bundesverband";
		        		  }else if(text.indexOf("Betriebs") > 0 || text.indexOf("BKK")> 0){
		        			  text = "BKK (Betriebskrankenkassen)";
		        		  }else if(text.indexOf("Landwirt") > 0 || text.indexOf("LKK")> 0){
		        			  text = "LKK (Landwirtschaftl. Krankenkassen)";
		        		  }else if(text.indexOf("Innungs") > 0 || text.indexOf("IKK")> 0){
		        			  text = "IKK (Innungskrankenkassen)";
		        		  }else if(text.indexOf("Knappschaft") > 0 || text.indexOf("KBS")> 0){
		        			  text = "Knappschaft";
		        		  }else if(text.indexOf("Ersatzkassen") > 0 || text.toUpperCase().indexOf("VDEK")> 0){
		        			  text = "VdEK (Ersatzkassen)";
		        		  }else{
		        			  continue;
		        		  }
		        			  
		        		  
		        		  gestartet = true;
		        		  kassendat.add(text.trim());
		        		  
		        		  //System.out.println(text);
		        		  //System.out.println(saveLink);

		        		  saveLink = "";
		        		  continue;
		        	  }
		        	  //g&uuml;ltig
		        	  if( ((index = text.indexOf("g&uuml;ltig")) >= 0) || ((index = text.indexOf("G&uuml;ltig")) >= 0) || ((index = text.indexOf("gueltig")) >= 0)|| ((index = text.indexOf("Gueltig")) >= 0)){ 
		        		  //text = text.substring(index+2);
		        		  text = text.replace("g&uuml;ltig ab", "");
		        		  text = text.replace("G&uuml;ltig ab", "");
		        		  text = text.replace("gueltig ab", "");
		        		  text = text.replace("Gueltig ab", "");
		        		  text = text.replace("dem", "");
		        		  text = text.replace("Januar", "01.");
		        		  text = text.replace("Februar", "02.");
		        		  text = text.replace("März", "03.");
		        		  text = text.replace("April", "04.");
		        		  text = text.replace("Mai", "05.");
		        		  text = text.replace("Juni", "06.");
		        		  text = text.replace("Juli", "07.");
		        		  text = text.replace("August", "08.");
		        		  text = text.replace("September", "09.");
		        		  text = text.replace("Oktober", "10.");
		        		  text = text.replace("November", "11.");
		        		  text = text.replace("Dezember", "12.");
		        		  text= text.replace(" ", "");
		        		  //if (text.substring(1, 2).equals(".")== true) {
		        		  if (text.substring(1, 2).equals(".")) {
		        			  text="0"+text;
		        		  }
		        		  kassendat.add(text.trim());
		        		  
		        		  kassendat.add(datLink[0]);
		        		  ktrmod.addRow((Vector<?>)kassendat.clone());
		        		  //ktraegerdat.add((Vector<String>)kassendat.clone());
		        		  gestartet = false;

		        		  continue;
		        	  }
		          }
		          ++durchlauf;
		      }
		      inS.close();
		      setFlags();
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Fehler bei der Analyse der Kostenträgerseite\n"+urltext);
		}
	}
	private String[] getDateiUndLink(String textzeile){
		String[] ret = {null,null};
		try{
			String nurlink = textzeile.split("\"")[1].replace("\"", "");
			ret[0] = nurlink.substring(nurlink.lastIndexOf("/")+1);
			ret[1] = nurlink.substring(0,nurlink.lastIndexOf("/")+1);
		}catch(Exception ex){
			return null;
		}
		return ret;
	}
	private void setFlags() {
		if(ktrmod.getRowCount() > 0){
			ktrtbl.setRowSelectionInterval(0, 0);
			ImageIcon[] img = {SystemConfig.hmSysIcons.get("zuzahlok"),SystemConfig.hmSysIcons.get("zuzahlnichtok"),SystemConfig.hmSysIcons.get("kleinehilfe")};
			String dateiName = "";
			String kassenArtKurz = "";
			int kANr;
			for( int i = 0; i < ktrmod.getRowCount(); i++ ){
				dateiName =(String) ktrmod.getValueAt(i, 2); //in eclipse column 1, da Datum nicht eingelesen wird
				kassenArtKurz = dateiName.substring(0,2).toUpperCase();
				kANr = inif.getIntegerProperty("KassenArtNr", kassenArtKurz);
				ktrmod.setValueAt(inif.getStringProperty("KABezeichner", "KALang"+kANr),i,0);				
				if ( inif.getStringProperty("KTraegerDateien", "KTDatei"+kANr).equals(dateiName) ){  
					ktrmod.setValueAt(img[0],i,3); // beide Versionen gleich -> "aktuell"
				} else if ( DatFunk.TageDifferenz(DatFunk.sHeute(), ktrmod.getValueAt(i,1).toString()) > 0){
					ktrmod.setValueAt(img[2],i,3); // GKV noch nicht gültig -> Erinnerungsfunktion wünschenswert
				} else if ( inif.getStringProperty("KTraegerDateien", "KTDatei"+kANr).equals("") ){ 
					ktrmod.setValueAt(img[1],i,3); // noch nicht in INI-Datei -> "update"
				} else if ( DatFunk.TageDifferenz(DatFunk.sHeute(), ktrmod.getValueAt(i,1).toString()) <= 0){
					if ( dateiNameCheck(dateiName,inif.getStringProperty("KTraegerDateien", "KTDatei"+kANr)) ){// Wenn Dateiname aktueller als INI-Dateiname (Monat/Quartal, insbesondere aber auch Version
						ktrmod.setValueAt(img[1],i,3); // GKV bereits gültig und neuer als Ini
					} else {
						ktrmod.setValueAt(img[0],i,3); //"DB aktuell 2"// GKV bereits gültig (ok), aber älter als Ini
					}
				} 	
				 else {
					ktrmod.setValueAt(img[2],i,3); 
				}
			}
		}
	}
	private boolean dateiNameCheck(String dateiNameGKV, String dateiNameIni) {
		String abJahrGKV = dateiNameGKV.substring(6, 8);  // Stelle 7-8
		String abJahrIni = dateiNameIni.substring(6, 8);  // Stelle 7-8
		String abMonatGKV = dateiNameGKV.substring(4, 6); // Stelle 5-6
		String abMonatIni = dateiNameIni.substring(4, 6);  // Stelle 5-6
		if (Integer.parseInt(abJahrGKV) > Integer.parseInt(abJahrIni)) return true;
		if (abMonatIni.substring(0).equals("Q")){
			if (abMonatIni.substring(1).equals("1")){
				abMonatIni= "01";
			} else if (abMonatIni.substring(1).equals("1")){
				abMonatIni= "04";
			} else if (abMonatIni.substring(1).equals("1")){
				abMonatIni= "07";
			}else {
				abMonatIni= "10";
			}
		}
		if (abMonatGKV.substring(0).equals("Q")){
			if (abMonatGKV.substring(1).equals("1")){
				abMonatGKV= "01";
			} else if (abMonatGKV.substring(1).equals("1")){
				abMonatGKV= "04";
			} else if (abMonatGKV.substring(1).equals("1")){
				abMonatGKV= "07";
			}else {
				abMonatGKV= "10";
			}
		}
		if (Integer.parseInt(abJahrGKV)>Integer.parseInt(abJahrIni)) return true;
		else if ( (Integer.parseInt(abJahrGKV)==Integer.parseInt(abJahrIni)) 
				&& (Integer.parseInt(dateiNameGKV.substring(11, 12))>Integer.parseInt(dateiNameGKV.substring(11, 12))) ) return true;
		
		return false;
	}
	public static String makeUTF8(final String toConvert){
		try {
			return new String(toConvert.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

/*************************/
	private void holeKtraeger(String datei, String inhalt) throws IOException{

		Vector<Vector<String>> ktraegerdat = new Vector<Vector<String>>();
		Vector<String> kassendat = new Vector<String>();
		boolean start = false;
		boolean gestartet = false;
		if(inhalt==null){
			String urltext = "http://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/sonstige_leistungserbringer/kostentraegerdateien_1/"+datei;
			//String urltext = "http://www.gkv-datenaustausch.de/upload/"+datei;
			String text = null;
			URL url = new URL(urltext);
			   
			      URLConnection conn = url.openConnection();
			      ////System.out.println(conn.getContentEncoding());
			      int lang=0,gesamt;
			      BufferedReader inS = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
			      gesamt = conn.getContentLength();
			      JOptionPane.showMessageDialog(null, "Länge des Contents = "+gesamt);
			      //System.out.println("Länge des Contents = "+conn.getContentLength());

			      int index;

			      while ( (text  = inS.readLine())!= null ) {
			    	  if(text.startsWith("IDK+")){
			    		  gestartet = true;
			    		  kassendat.clear();
			    		  kassendat.add(text);
			    		  continue;
			    	  }
			    	  if((!text.startsWith("UNT+")) && gestartet){
			    		  kassendat.add(text);
			    		  continue;
			    	  }
			    	  if(text.startsWith("UNT+") && gestartet){
			    		  kassendat.add(text);
			    		  ktraegerdat.add((Vector<String>)kassendat.clone());
			    		  gestartet = false;
			    		  continue;
			    	  }
			    	  lang+=text.length();
			      }
			inS.close();
		
		}else{
			//JOptionPane.showMessageDialog(null, "Länge des Contents = "+inhalt.length());

			String[] text = inhalt.split("\n");
			progress.setMaximum(text.length-1);
			progress.setValue(0);
			for(int i = 0; i < text.length; i++){
				progress.setValue(i);
		    	  if(text[i].startsWith("IDK+")){
		    		  gestartet = true;
		    		  kassendat.clear();
		    		  kassendat.add(text[i]);
		    		  continue;
		    	  }
		    	  if((!text[i].startsWith("UNT+")) && gestartet){
		    		  kassendat.add(text[i]);
		    		  continue;
		    	  }
		    	  if(text[i].startsWith("UNT+") && gestartet){
		    		  kassendat.add(text[i]);
		    		  ktraegerdat.add((Vector<String>)kassendat.clone());
		    		  gestartet = false;
		    		  continue;
		    	  }
			}
		}
		
		kassendat.clear();
		kassendat = null;
		//System.out.println(ktraegerdat);
		progress.setMaximum(ktraegerdat.size()-1);
		progress.setValue(0);

		for(int i = 0; i < ktraegerdat.size(); i++){
			progress.setValue(i);
			//System.out.println(ktraegerdat.get(i));
			ktrAuswerten(ktraegerdat.get(i));
		}

		/***2-te Stufe***/
		// 
		Vector<Vector<String>> vec1 = SqlInfo.holeFelder("select ikkostentraeger from ktraeger where ikdaten ='' AND email='' ORDER BY id");
		//System.out.println("Anzahl der felder ohne Emaildaten = "+vec1.size());
		//System.out.println("***********************************************");
		Vector<String> vec2 = new Vector<String>();
		Vector<Vector<String>> mailvec;
		Vector<Vector<String>> dumyvec;
		Vector<Vector<String>> dummyvec;
		String aemail="";
		String papier="";
		String entschluessel="";
		String kostentr = "";
		String sdummy = "";
		String daten = "";
		progress.setMaximum(vec1.size()-1);
		progress.setValue(0);

		for(int i = 0;i < vec1.size();i++){
			kostentr = vec1.get(i).get(0);
			progress.setValue(i);
			if( (!vec2.contains(kostentr)) && (!kostentr.equals("")) ){
				
				vec2.add(kostentr);
				try{
					//System.out.println("IK-Kostenträger addiert = "+kostentr);
					//Vom Kostenträger die IK-Datenholen
					dummyvec = SqlInfo.holeFelder("select ikdaten,ikpapier,ikentschluesselung from ktraeger where ikkasse='"+kostentr+"' LIMIT 1");
					daten = dummyvec.get(0).get(0);
					papier = dummyvec.get(0).get(1);
					entschluessel = dummyvec.get(0).get(2);
					//Von der Datenannahmestelle die Email holen
					dumyvec = SqlInfo.holeFelder("select email from ktraeger where ikkasse='"+daten+"' LIMIT 1");
					if(dumyvec.size() > 0){
						aemail = dumyvec.get(0).get(0);	
					}else{
						aemail = "";
					}
					SqlInfo.sqlAusfuehren("update ktraeger set ikdaten='"+daten+"', "+
							"ikpapier='"+papier+"', "+
							"ikentschluesselung='"+entschluessel+"', "+
							"email='"+aemail+"' where ikkostentraeger='"+kostentr+"'");
				}catch(Exception ex){
					ex.printStackTrace();
				}
				
				
			}
		}
		int kANr = inif.getIntegerProperty("KassenArtNr", datei.substring(0,2).toUpperCase());
		inif.setStringProperty("KTraegerDateien", "KTDatei"+kANr, datei.toString(), null);
		INITool.saveIni(inif);
		//progress.setValue(0);
		JOptionPane.showMessageDialog(null,"Kostenträgerdatei erfolgreich verarbeitet");
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setFlags();
	}
	private void ktrAuswerten(Vector<String> ktr){
		String ikkost ="";
		String ikdat = "";
		String ikpap = "";
		String ikent = "";
		String xemail = "";
		String nam1="",nam2="",nam3="";
		String adr1="",adr2="",adr3="";
		
		String dummy1 = "";
		String ikkas = "";
		
		String[] spdummy;
		String cmd = "";
		String notikdat = "";
		String notikent = "";
		String notikpap = "";
		String notxemail = "";
		int lang;
		
		spdummy = ktr.get(0).split("\\+");
		ikkas = spdummy[1];

		
		for(int i = 1;i < ktr.size();i++){
			if(ktr.get(i).indexOf("VKG+01+") >= 0){
				// Verweis auf den Kostenträger
				ikkost = ktr.get(i).split("\\+")[2]; 
			}
			if(ktr.get(i).indexOf("VKG+03+") >= 0){
				//Verweis auf Datenannahme mit Entschlüsselung //Schlüssel 07 Art der Datenlieferung
				spdummy = ktr.get(i).replace("'","").split("\\+");
				if(spdummy[5].equals("07")){
					/*
					if(ikkas.equals("103500706")){
						String meldung = "Abrechnungsschlüssel = "+spdummy[9]+"\n"+
						"IKDAT = "+ikdat;
						JOptionPane.showMessageDialog(null, meldung);
					}
					*/
					if( (spdummy[9].equals("00") || spdummy[9].startsWith("2")) ){
						ikdat = spdummy[2];
						ikent = spdummy[2];
					}else{
						if( (spdummy[9].equals("99")) && (ikdat.equals("")) ){
							ikdat = spdummy[2];
							ikent = spdummy[2];
						}
					}
				}	
			}
			if(ktr.get(i).indexOf("VKG+02+") >= 0){
				//Verweis auf Datenannahme ohne Entschlüsselung //Schlüssel 07 Art der Datenlieferung
			}
			if(ktr.get(i).indexOf("VKG+09+") >= 0){
				//Verweis auf Papierannahmestelle
				spdummy = ktr.get(i).replace("'","").split("\\+");
				if(   (spdummy[5].equals("28") || spdummy[5].equals("29")) &&
						(spdummy[9].equals("00") || spdummy[9].startsWith("2"))	){
					ikpap = spdummy[2];
				}else{
					if(spdummy[9].equals("99") && (ikpap.equals(""))){
						ikpap = spdummy[2];
					}
				}
			}
			if(ktr.get(i).indexOf("NAM+01+") >= 0){
				//Name der Kasse
				spdummy = ktr.get(i).replace("'","").split("\\+");
				lang = spdummy.length-2;
				for(int i2 = 0;i2 < lang;i2++){
					if(i2==0){nam1 = spdummy[i2+2];}
					if(i2==1){nam2 = spdummy[i2+2];}
					if(i2==2){nam3 = spdummy[i2+2];}
				}
			}
			if(ktr.get(i).indexOf("ANS+1+") >= 0){
				//Anschrift der Kasse
				spdummy = ktr.get(i).replace("'","").split("\\+");
				lang = spdummy.length-2;
				for(int i2 = 0;i2 < lang;i2++){
					if(i2==0){adr1 = spdummy[i2+2];}
					if(i2==1){adr2 = spdummy[i2+2];}
					if(i2==2){adr3 = spdummy[i2+2];}
				}
			}
			//DFU+01+070+++++DTA@KV-Service-Plus-GmbH.de'
			if(ktr.get(i).indexOf("DFU+") >= 0){
				spdummy = ktr.get(i).replace("'","").split("\\+");
				if(spdummy[2].equals("070")){
					xemail = spdummy[7];					
				}
			}

		}
		
		boolean existiert=false;
		existiert = SqlInfo.gibtsSchon("select id from ktraeger where ikkasse='"+ikkas+"' LIMIT 1");
		if(existiert){
			cmd = "update ktraeger set ";
		}else{
			cmd = "insert into ktraeger set ";
		}
		cmd = cmd+"ikkasse='"+ikkas+"', ikkostentraeger='"+ikkost+"', ikpapier='"+ikpap+"', "+
		"ikdaten='"+ikdat+"', "+
		"ikentschluesselung='"+ikent+"', name1='"+nam1+"', name2='"+nam2+"', name3='"+nam3+"', "+
		"adresse1='"+adr1+"', adresse2='"+adr2+"', adresse3='"+adr3+"', email ='"+xemail+"'"+
		(existiert ? " where ikkasse='"+ikkas+"' LIMIT 1" : "");
		//System.out.println(cmd);
		SqlInfo.sqlAusfuehren(cmd);

		
		if(SqlInfo.gibtsSchon("select id from kass_adr where ik_kasse = '"+ikkas+"' LIMIT 1")){
			vKassenTest.add(ikkas.toString());			
		}
		
		
	}
	
/*************************/	
	class MyKtraegerModel extends DefaultTableModel{
		private static final long serialVersionUID = 1L;

		public Class<?> getColumnClass(int columnIndex) {
			   if(columnIndex==3){return JLabel.class;}
			   else{return String.class;}
	}

		    public boolean isCellEditable(int row, int col) {
		        //Note that the data/cell address is constant,
		        //no matter where the cell appears onscreen.
		    	return false;
		      }
			public Object getValueAt(int rowIndex, int columnIndex) {
				Object theData;
				if (columnIndex==3){theData = (ImageIcon) ((Vector<?>)getDataVector().get(rowIndex)).get(columnIndex);}
				else{theData = (String) ((Vector<?>)getDataVector().get(rowIndex)).get(columnIndex);}
				Object result = null;
				result = theData;
				return result;
			}
	}
	
	class KtreagerTblRenderer extends JLabel implements TableCellRenderer{
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			   public KtreagerTblRenderer(){
				   super();
				   this.setHorizontalAlignment(SwingConstants.CENTER);
			   }
			   @Override
			   public Component getTableCellRendererComponent(JTable table, Object value,
			         boolean isSelected, boolean hasFocus, int row, int column) {
				   if (isSelected) {
					   setOpaque(true);
			    	   setForeground(table.getSelectionForeground());
		               setBackground(table.getSelectionBackground());
			       }else {
					   setOpaque(true);	    	   
		               setForeground(table.getForeground());
		               setBackground(table.getBackground());
			       }
			        if(value instanceof ImageIcon){
			        	//ImageIcon[] img = {SystemConfig.hmSysIcons.get("zuzahlok"),SystemConfig.hmSysIcons.get("zuzahlnichtok"),SystemConfig.hmSysIcons.get("kleinehilfe")};
				    	  if( (value).equals(SystemConfig.hmSysIcons.get("zuzahlok")) ){
				    		  setToolTipText("Datei aktuell") ;
				    		  setIcon(SystemConfig.hmSysIcons.get("zuzahlok"));
				    	  }else if( (value).equals(SystemConfig.hmSysIcons.get("zuzahlnichtok")) ){
				    		  setToolTipText("update durchführen") ;
				    		  setIcon(SystemConfig.hmSysIcons.get("zuzahlnichtok"));
				    	  }else if( (value).equals(SystemConfig.hmSysIcons.get("kleinehilfe")) ){
				    		  setToolTipText("Datei noch nicht gültig") ;
				    		  setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
				    	  }
				     }else{
				    	 //System.out.println(value.getClass());
				     }
			        return this;
			   }
			}
/***************************************************************/
	private void doKassenTest(){
		Vector<Vector<String>> vDummyKt = new Vector<Vector<String>>();
		String htmlString = null;
		//String emailkas,emailkt;
		int frage; 
		String kakostentr,ktkostentr,kadaten,ktdaten,kadecode,ktdecode,kapapier,ktpapier,kaemail,ktemail;
		for(int i = 0; i < vKassenTest.size();i++){

			//System.out.println(vKassenTest.get(i));
			htmlString = "<html>"+
			"<head>"+"<STYLE TYPE=\"text/css\">"+"<!--"+"A{text-decoration:none;background-color:transparent;border:none}"+
			"TD{font-family: Arial; font-size: 12pt; padding-left:5px;padding-right:30px}"+".spalte1{color:#0000FF;}"+".spalte2{color:#FF0000;}"+"---></STYLE></head>"+
			
			"Vergleiche "+Integer.toString(i+1)+" von "+Integer.toString(vKassenTest.size())+"<br><br><table><tr><td></td><td>&nbsp;</td><td class=\"spalte1\"><i><b><u>Daten im eigenen Kassenstamm</u></b></i></td><td>&nbsp;&nbsp;</td><td class=\"spalte2\"><i><b><u>Daten in der Kostenträgerdatei</u></b></i></td></tr>";

			vDummyKt = SqlInfo.holeFelder("select * from ktraeger where ikkasse = '"+vKassenTest.get(i)+"'");
			ktkostentr = vDummyKt.get(0).get(1).trim();
			ktdaten = vDummyKt.get(0).get(3).trim();
			ktdecode = vDummyKt.get(0).get(4).trim();
			ktpapier = vDummyKt.get(0).get(2).trim();

			vDummyTest = SqlInfo.holeFelder("select * from kass_adr where ik_kasse = '"+vKassenTest.get(i)+"'");
			kakostentr = vDummyTest.get(0).get(18).trim();
			kadaten = vDummyTest.get(0).get(16).trim();
			kadecode = vDummyTest.get(0).get(17).trim();
			kapapier = vDummyTest.get(0).get(20).trim();
			
			if(kakostentr.equals(ktkostentr) && kadaten.equals(ktdaten)){continue;}
			
			htmlString = htmlString+ "<tr><td>Name1:</td><td>&nbsp;</td><td class=\"spalte1\">"+vDummyTest.get(0).get(2)+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+vDummyKt.get(0).get(5)+"</td></tr>";
			htmlString = htmlString+ "<tr><td>Name2:</td><td>&nbsp;</td><td class=\"spalte1\">"+vDummyTest.get(0).get(3)+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+vDummyKt.get(0).get(6)+"</td></tr>";
			htmlString = htmlString+ "<tr><td>Strasse:</td><td>&nbsp;</td><td class=\"spalte1\">"+vDummyTest.get(0).get(4)+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+vDummyKt.get(0).get(10)+"</td></tr>";
			htmlString = htmlString+ "<tr><td>PLZ:</td><td>&nbsp;</td><td class=\"spalte1\">"+vDummyTest.get(0).get(5)+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+vDummyKt.get(0).get(8)+"</td></tr>";
			htmlString = htmlString+ "<tr><td>Ort:</td><td>&nbsp;</td><td class=\"spalte1\">"+vDummyTest.get(0).get(6)+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+vDummyKt.get(0).get(9)+"</td></tr>";
			htmlString = htmlString+ "<tr><td>IK-Kasse:</td><td>&nbsp;</td><td class=\"spalte1\">"+vDummyTest.get(0).get(15)+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+vDummyKt.get(0).get(0)+"</td></tr>";
			htmlString = htmlString+ "<tr><td>IK-Kostenträger:</td><td>&nbsp;</td><td class=\"spalte1\">"+kakostentr+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+ktkostentr+"</td></tr>";
			htmlString = htmlString+ "<tr><td>IK-Datenannahme:</td><td>&nbsp;</td><td class=\"spalte1\">"+kadaten+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+ktdaten+"</td></tr>";
			htmlString = htmlString+ "<tr><td>IK-Entschlüsselung:</td><td>&nbsp;</td><td class=\"spalte1\">"+kadecode+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+ktdecode+"</td></tr>";
			htmlString = htmlString+ "<tr><td>IK-Papierannahme:</td><td>&nbsp;</td><td class=\"spalte1\">"+kapapier+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+ktpapier+"</td></tr>";
			
			kaemail = SqlInfo.holeEinzelFeld("select email1 from kass_adr where ik_kasse = '"+vDummyTest.get(0).get(16)+"' LIMIT 1");
			ktemail = SqlInfo.holeEinzelFeld("select email from ktraeger where ikkasse = '"+vDummyKt.get(0).get(3)+"' LIMIT 1");
			
			htmlString = htmlString+ "<tr><td>Email Abrechnungsdaten:</td><td>&nbsp;</td><td class=\"spalte1\">"+kaemail+"</td><td>&nbsp;&nbsp;</td><td class=\"spalte2\">"+ktemail+"</td></tr>";
			htmlString = htmlString+"</table><br><br><br><b>Wollen Sie die Daten der Kostenträgerdatei in Ihren Kassenstamm übernehmen?</b><br></html>";

			if(ktkostentr.equals("") && ktdaten.equals("") && ktdecode.equals("") && ktpapier.equals("")){
				//es handelt sich zu 99,99% um eine Datenannahmestelle // ToDo: testen ob Emailadresse für die Datenannahmestelle existiert
				if(! SqlInfo.gibtsSchon("select email1 from kass_adr where ik_kasse = '"+ktdaten+"'")){
					htmlString = "<html><b>Achtung der Datenannahmestelle</b><br>"+vDummyKt.get(0).get(5)+"<br>"+
					"IK: "+vDummyKt.get(0).get(0)+" ist keine gültige Emailadresse zugeordenet"+
					"<br>Bitte die Datenannahmestelle manuell anlegen!</html>";
					JOptionPane.showMessageDialog(null,htmlString);					
				}
				vDummyKt.clear();
				vDummyTest.clear();
				htmlString = null;
				continue;
			}
			frage = JOptionPane.showConfirmDialog(null,htmlString,"Wichtige Benutzeranfrage",JOptionPane.YES_NO_CANCEL_OPTION);
			//System.out.println(frage);

			htmlString = null; 
			if(frage == JOptionPane.CANCEL_OPTION){break;}
			if(frage == JOptionPane.YES_OPTION){
				// hier die Updatefunktion
				String stmt = null; 
				if(ktkostentr.equals("") || ktdaten.equals("") || ktdecode.equals("") || ktpapier.equals("") ){
					htmlString = "<html><b>Achtung die Daten die Sie übernehmen wollen sind nicht vollständig!</b><br><br><table>"+
					"<tr><td>IK-Kostenträger:</td><td>&nbsp;</td><td>"+(ktkostentr.equals("") ? "<b>->fehlt!</b>" : ktkostentr)+"</td></tr>"+
					"<tr><td>IK-Datenannahme:</td><td>&nbsp;</td><td>"+(ktdaten.equals("") ? "<b>->fehlt!</b>" : ktdaten) +"</td></tr>"+
					"<tr><td>IK-Entschlüsselung:</td><td>&nbsp;</td><td>"+(ktdecode.equals("") ? "<b>->fehlt!</b>" : ktdecode) +"</td></tr>"+
					"<tr><td>IK-Papierannahme:</td><td>&nbsp;</td><td>"+(ktpapier.equals("") ? "<b>->fehlt!</b>" : ktpapier)+"</td></tr>"+
					"<br><br><b>Daten trotzdem übernehmen?</b><br>"+
					"</table></html>";
					frage = JOptionPane.showConfirmDialog(null,htmlString,"Wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage == JOptionPane.YES_OPTION){
						stmt = "update kass_adr set ik_kostent='"+ktkostentr+"', ik_physika='"+ktdaten+"', ik_nutzer='"+ktdecode+"', ik_papier='"+ktpapier+"' "+
						"where ik_kasse = '"+vDummyTest.get(0).get(15)+"'";
						SqlInfo.sqlAusfuehren(stmt);
						//System.out.println(stmt);
					}
				}/*else if(ktemail.equals("") && (!kaemail.equals("")) ){
					htmlString = "<html><b>Die Emailadresse in der Kostenträgerdatei ist leer!<br>Die bisherige Emailadresse in Ihrem Kassenstamm wird deshalb nicht überschrieben</html>";
					if(ktemail.equals("")){
						frage = JOptionPane.showConfirmDialog(null,htmlString,JOptionPane.YES_NO_OPTION);
					}
				}*/else{
					stmt = "update kass_adr set ik_kostent='"+ktkostentr+"', ik_physika='"+ktdaten+"', ik_nutzer='"+ktdecode+"', ik_papier='"+ktpapier+"' "+
					"where ik_kasse = '"+vDummyTest.get(0).get(15)+"'";
					SqlInfo.sqlAusfuehren(stmt);
					//nachsehen ob die Emailadresse im Kassenstamm vorhanden ist...
					if( (!ktemail.equals("")) && (kaemail.equals("") || kaemail != ktemail) ){
						if(! SqlInfo.gibtsSchon("select id from kass_adr where ik_kasse = '"+ktdaten+"'")){
							htmlString = "<html><b>Achtung die Datenannahmestelle</b><br>"+vDummyKt.get(0).get(5)+"<br>"+
							"IK: "+vDummyKt.get(0).get(0)+" existiert in Ihrem Kassenstamm nicht"+
							"<br>Bitte die Datenannahmestelle manuell anlegen!</html>";
							JOptionPane.showMessageDialog(null,htmlString);
						}else{
							stmt = "update kass_adr set email1='"+ktemail+"' where ik_kasse='"+ktdaten+"'"; 
							SqlInfo.sqlAusfuehren(stmt);
						}
					}
				}

			}
			vDummyKt.clear();
			vDummyTest.clear();
			htmlString = null;
		}
		 
	}
	

}
