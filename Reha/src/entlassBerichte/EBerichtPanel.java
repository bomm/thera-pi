package entlassBerichte;

import hauptFenster.Reha;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import oOorgTools.OOTools;


import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;

import patientenFenster.Gutachten;
import patientenFenster.PatGrundPanel;
import pdfDrucker.PdfDrucker;

import com.jgoodies.forms.layout.CellConstraints;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import events.PatStammEventClass;
import events.PatStammEventListener;
import events.RehaEvent;
import events.RehaEventClass;
import events.RehaEventListener;

import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.filter.PDFFilter;
import ag.ion.bion.officelayer.filter.RTFFilter;
import ag.ion.bion.officelayer.text.ITextDocument;

import sqlTools.SqlInfo;
import systemEinstellungen.SystemConfig;
import systemTools.Colors;
import systemTools.FileTools;
import systemTools.JRtaCheckBox;
import systemTools.JRtaComboBox;
import systemTools.JRtaTextField;
import terminKalender.datFunk;

import RehaInternalFrame.JArztInternal;
import RehaInternalFrame.JGutachtenInternal;

public class EBerichtPanel extends JXPanel implements ChangeListener,RehaEventListener,PropertyChangeListener,TableModelListener,KeyListener,FocusListener,ActionListener, MouseListener{
	JGutachtenInternal jry = null;
	public EBerichtPanel thisClass = null;
	public JXPanel seite1;
	public JXPanel seite2;
	public JXPanel seite3;
	public JXPanel seite4;
	public JTabbedPane ebtab = null;
	JButton[] gutbut = {null,null,null,null,null};
	String[] ktraeger = {"DRV Bund","DRV Baden-W�rttemberg","DRV Bayern","DRV Berlin","DRV Brandenburg","DRV Bremen",
			"DRV Hamburg","DRV Hessen","DRV Mecklenburg-Vorpommern","DRV Niedersachsen","DRV Rheinland-Pfalz",
			"DRV Saarland","DRV Sachsen","DRV Sachsensen-Anhalt","DRV Schleswig-Holstein","DRV Th�ringen","DRV Knappschaft Bahn/See","GKV"};
//	String[] ktraeger = {"DRV Bund","DRV Baden-W�rttemberg","DRV Knappschaft Bahn/See","DRV Bayer","GKV"};

	/**********************/
	public JRtaComboBox cbktraeger = null;
	/**********************/
	public String pat_intern = null;
	public int berichtid = -1;
	public String berichttyp = null;
	public String empfaenger = null;
	public String berichtart = null;
	public boolean neu = false;
	public boolean jetztneu = false;
	public boolean inebericht = false;
	public String tempPfad = Reha.proghome+"temp/"+Reha.aktIK+"/";
	public String vorlagenPfad = Reha.proghome+"vorlagen/"+Reha.aktIK+"/";
	public String[] rvVorlagen = {null,null,null,null};
	EBerichtTab ebt;
	NachsorgeTab nat;
	IFrame officeFrame = null;
	RehaEventClass evt = null;
	
	String[][] tempDateien = {null,null,null,null,null};
	
	static ITextDocument document = null;
	
 
	
	public JRtaTextField[] barzttf = {null,null,null}; 

	public JRtaTextField[] btf = {  null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};
	public JRtaComboBox[] bcmb = {  null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};
	
	public JRtaCheckBox[] bchb = {  null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};
	public JTextArea[] 	  bta = {  null,null,null,null,null,null,null,null,null,null};

	public JRtaComboBox[] ktlcmb={  null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};
	public JRtaTextField[] ktltfc={ null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};

	public JRtaTextField[] ktltfd={ null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};
	public JRtaTextField[] ktltfa={ null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null,
									null,null,null,null,null,null,null,null,null,null
	};
	public JRtaComboBox[] acmb = {  null,null,null};
			
	
	public EBerichtPanel(JGutachtenInternal xjry,String xpat_intern,int xberichtid,String xberichttyp,boolean xneu,String xempfaenger ){
		setBorder(null);
		
		this.jry = xjry;
		this.pat_intern = xpat_intern;
		this.berichtid = xberichtid;
		this.berichttyp = xberichttyp;
		this.empfaenger = xempfaenger;
		this.neu = xneu;
		
		thisClass = this;
		
		evt = new RehaEventClass();
		evt.addRehaEventListener((RehaEventListener) this);

		addFocusListener(this);
				
		Point2D start = new Point2D.Float(0, 0);
	    Point2D end = new Point2D.Float(400,550);
	    float[] dist = {0.0f, 0.75f};
	    Color[] colors = {Color.WHITE,Colors.Gray.alpha(0.15f)};
	    LinearGradientPaint p =  new LinearGradientPaint(start, end, dist, colors);
	    MattePainter mp = new MattePainter(p);
	    setBackgroundPainter(new CompoundPainter(mp));
		setLayout(new BorderLayout());
		
		add(this.getToolbar(),BorderLayout.NORTH);

		if(berichttyp.contains("E-Bericht") || berichttyp.contains("LVA-A") || berichttyp.contains("BfA-A") 
				|| berichttyp.contains("GKV-A")){
			cbktraeger = new JRtaComboBox(SystemConfig.vGutachtenEmpfaenger);
			UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
			ebtab = getEBerichtTab();
			ebtab.setSelectedIndex(0);
			add(ebtab,BorderLayout.CENTER);
			ebtab.addChangeListener(this);
			UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);
			UIManager.put("TabbedPane.contentOpaque", Boolean.TRUE);
			//rvVorlagen[0]  = vorlagenPfad+"RV-EBericht-Seite1-Variante2.pdf";
			rvVorlagen[0]  = vorlagenPfad+"EBericht-Seite1-Variante2.pdf";
			rvVorlagen[1]  = vorlagenPfad+"EBericht-Seite2-Variante2.pdf";
			rvVorlagen[2]  = vorlagenPfad+"EBericht-Seite3-Variante2.pdf";
			rvVorlagen[3]  = vorlagenPfad+"EBericht-Seite4-Variante2.pdf";
			berichtart = "entlassbericht";
			inebericht = true;
		}else{
			cbktraeger = new JRtaComboBox(SystemConfig.vGutachtenEmpfaenger);
			UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
			ebtab = getNachsorgeTab();
			add(ebtab,BorderLayout.CENTER);
			UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);
			UIManager.put("TabbedPane.contentOpaque", Boolean.TRUE);
			berichtart = "nachsorgedokumentation";
			inebericht = false;
		}
		
		System.out.println("Bericht von Patient Nr. ="+ this.pat_intern);
		System.out.println("             Bericht ID ="+ this.berichtid);
		System.out.println("             Berichttyp ="+ this.berichttyp);
		System.out.println("             Empfaenger ="+ this.empfaenger);
		System.out.println("          Neuer Bericht ="+ this.neu);
	}
	/******************************************************************/
	private JTabbedPane getEBerichtTab(){
		ebt = new EBerichtTab(this);
		return ebt.getTab();
	}
	private JTabbedPane getNachsorgeTab(){
		nat = new NachsorgeTab(this);
		return nat.getTab();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub
            // Get current tab
		//System.out.println(arg0);
		JTabbedPane pane = (JTabbedPane)arg0.getSource();
        int sel = pane.getSelectedIndex();
        //System.out.println("Tab mit Index "+sel+" wurde selektiert");
        if(sel==2){
        	new SwingWorker<Void,Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					if(ebt.getTab3().framegetrennt){
						System.out.println("OOTab ist!!!! selektiert und getrennt deshalb BaueSeite");
						ebt.getTab3().baueSeite();
					}else{
						System.out.println("OOTab ist!!!! selektiert und nicht!!!!getrennt deshalb nur tempTextSpeichern");
						ebt.getTab3().tempTextSpeichern();
					}
			
					return null;
				}
        	}.execute();
        }else{
        }

	}    

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		
		/**************************************************/
		if(cmd.equals("gutsave")){
			if(berichtart.equals("entlassbericht")){
				if(this.neu){
					doSpeichernNeu();	
					JOptionPane.showMessageDialog(null,"Entlassbericht wurde gespeichert");
				}else{
					doSpeichernAlt();
					JOptionPane.showMessageDialog(null,"Entlassbericht wurde gespeichert");
				}
			}else if(berichtart.equals("nachsorgedokumentation")){
				if(this.neu){
					doSpeichernNachsorgeNeu();
					JOptionPane.showMessageDialog(null,"Nachsorge-Dokumentation wurde gespeichert");
				}else{
					doSpeichernNachsorgeAlt();
					JOptionPane.showMessageDialog(null,"Nachsorge-Dokumentation wurde gespeichert");
				}
			}
		}
		/**************************************************/			
		if(cmd.equals("gutvorschau")){
			if(berichtart.equals("entlassbericht")){
				doVorschauEntlassBericht(true,new int[] {});
			}else if(berichtart.equals("nachsorgedokumentation")){
				doVorschauNachsorge(true,0);
			}
		}
		if(cmd.equals("gutprint")){
			if(berichtart.equals("entlassbericht")){
				doVorschauEntlassBericht(false,new int[] {});
			}else if(berichtart.equals("nachsorgedokumentation")){
				
			}
		}
		if(cmd.equals("guttools")){
			
		}
		if(cmd.equals("guttext")){
			if(!neu){
        		InputStream is = SqlInfo.holeStream("bericht2","freitext","berichtid='"+berichtid+"'");
        		document = OOTools.starteWriterMitStream(is, "Vorhandener Bericht");
			}else{
				document = OOTools.starteLeerenWriter();
			}
		}
		
	}
	private void doSpeichernNachsorgeAlt(){
		try{
			Reha.thisClass.progressStarten(true);
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			StringBuffer buf = new StringBuffer();
			buf.append("update bericht2 set ");
			// erst die Textfelder auswerten
			for(int i = 0; i < 24;i++){
				if(!btf[i].getRtaType().equals("DATUM")){
					buf.append(btf[i].getName()+"='"+btf[i].getText()+"', ");				
				}else{
					if(!btf[i].getText().trim().equals(".  .")){
						buf.append(btf[i].getName()+"='"+datFunk.sDatInSQL(btf[i].getText())+"', ");
					}else{
						buf.append(btf[i].getName()+"=null, ");
					}
				}
			}
			// dann die Checkboxen
			for(int i = 0; i < 19;i++){
				buf.append(bchb[i].getName()+"='"+(bchb[i].isSelected() ? "1" : "0")+"', ");
			}
			// die TextAreas
			for(int i = 0; i < 10;i++){
				buf.append(bta[i].getName()+"='"+bta[i].getText()+"', ");
			}
			for(int i = 0; i < 15;i++){
				if(i < 14){
					buf.append(bcmb[i].getName()+"='"+bcmb[i].getSelectedItem()+"', ");				
				}else{
					buf.append(bcmb[i].getName()+"='"+bcmb[i].getSelectedItem()+"' ");
				}
			}	
			buf.append( " where berichtid = '"+berichtid+"'");
			SqlInfo.sqlAusfuehren(buf.toString());
			
			buf = new StringBuffer();
			buf.append("Update bericht2ktl set " ); 
			for(int i = 0;i < 10;i++){
				buf.append(ktlcmb[i].getName()+"='"+ktlcmb[i].getValue()+"', ");
				buf.append(ktltfc[i].getName()+"='"+ktltfc[i].getText()+"', ");
				buf.append(ktltfd[i].getName()+"='"+ktltfd[i].getText()+"', ");
				if(i < 9){
					buf.append(ktltfa[i].getName()+"='"+ktltfa[i].getText()+"', ");				
				}else{
					buf.append(ktltfa[i].getName()+"='"+ktltfa[i].getText()+"'");
				}
				
			}
			buf.append( " where berichtid = '"+berichtid+"'");
			SqlInfo.sqlAusfuehren(buf.toString());
			Reha.thisClass.progressStarten(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			if(!jetztneu){
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				String empf = (String) cbktraeger.getSelectedItem();
				String btype = (empf.contains("DRV") && empf.contains("Bund")? "IRENA Nachsorgedoku" : "ASP Nachsorgedoku");
				Gutachten.gutachten.aktualisiereGutachten(datFunk.sHeute(),btype,empf,"Reha-Arzt",berichtid,pat_intern);
				Reha.thisClass.progressStarten(false);
			}else{
				jetztneu = false;
				Reha.thisClass.progressStarten(false);
			}			
		}catch(Exception ex){
			ex.printStackTrace();
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			Reha.thisClass.progressStarten(false);
		}
	}
	/*************************************************************************/
	private void doSpeichernNachsorgeNeu(){
		try{
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			Reha.thisClass.progressStarten(true);
			int nummer = SqlInfo.erzeugeNummer("bericht");
			berichtid = nummer;
			String empf = (String) cbktraeger.getSelectedItem();
			if(! empf.contains("DRV")){
				Reha.thisClass.progressStarten(false);
				return;
			}
			String btype = (empf.contains("DRV") && empf.contains("Bund")? "IRENA Nachsorgedoku" : "ASP Nachsorgedoku");
	/////
			String cmd = "insert into berhist set berichtid='"+berichtid+"', erstelldat='"
			+datFunk.sDatInSQL(datFunk.sHeute())+"', berichttyp='"+btype+"', "+
			"verfasser='Reha-Arzt', empfaenger='"+empf+"', pat_intern='"+pat_intern+"', bertitel='Reha-Entlassbericht'";
			SqlInfo.sqlAusfuehren(cmd);
			cmd = "insert into bericht2 set berichtid='"+berichtid+"', pat_intern='"+pat_intern+"'";
			SqlInfo.sqlAusfuehren(cmd);
			cmd = "insert into bericht2ktl set berichtid='"+berichtid+"', pat_intern='"+pat_intern+"'";
			SqlInfo.sqlAusfuehren(cmd);
			jetztneu = true; // ganz wichtig
			neu = false;
	/////		
			doSpeichernNachsorgeAlt();
			System.out.println("Nach Speichern alt");
			Gutachten.gutachten.neuesGutachten(new Integer(berichtid).toString(),
					btype,"Reha-Arzt",datFunk.sHeute() ,empf, pat_intern,"Nachsorgedokumentation");
			
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}catch(Exception ex){
			Reha.thisClass.progressStarten(true);
			ex.printStackTrace();
		}
		Reha.thisClass.progressStarten(false);
	}
/*************************************************************************/		

	private void doSpeichernAlt(){
		Reha.thisClass.progressStarten(true);
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		StringBuffer buf = new StringBuffer();
		buf.append("update bericht2 set ");
		for(int i = 0; i < 28;i++){
			if(!btf[i].getRtaType().equals("DATUM")){
				buf.append(btf[i].getName()+"='"+btf[i].getText()+"', ");				
			}else{
				if(!btf[i].getText().trim().equals(".  .")){
					buf.append(btf[i].getName()+"='"+datFunk.sDatInSQL(btf[i].getText())+"', ");
				}else{
					buf.append(btf[i].getName()+"=null, ");
				}
			}

		}
		for(int i = 0; i < 3;i++){
			buf.append(barzttf[i].getName()+"='"+barzttf[i].getText()+"', ");
		}
		for(int i = 0; i < 44;i++){
			buf.append(bchb[i].getName()+"='"+(bchb[i].isSelected() ? "1" : "0")+"', ");
		}
		for(int i = 0; i < 8;i++){
			buf.append(bta[i].getName()+"='"+bta[i].getText()+"', ");
		}
		for(int i = 0; i < 20;i++){
			if(i < 19){
				buf.append(bcmb[i].getName()+"='"+bcmb[i].getSelectedItem()+"', ");				
			}else{
				buf.append(bcmb[i].getName()+"='"+bcmb[i].getSelectedItem()+"' ");
			}
		}
		buf.append( " where berichtid = '"+berichtid+"'");
		SqlInfo.sqlAusfuehren(buf.toString());
		/******************************************************************************************/
		buf = new StringBuffer();
		buf.append("Update bericht2ktl set " ); 
		for(int i = 0;i < 50;i++){
			buf.append(ktlcmb[i].getName()+"='"+ktlcmb[i].getValue()+"', ");
			buf.append(ktltfc[i].getName()+"='"+ktltfc[i].getText()+"', ");
			buf.append(ktltfd[i].getName()+"='"+ktltfd[i].getText()+"', ");
			buf.append(ktltfa[i].getName()+"='"+ktltfa[i].getText()+"', ");			
		}
		for(int i = 8; i < 10;i++){
			if(i == 8){
				buf.append(bta[i].getName()+"='"+bta[i].getText()+"', ");				
			}else{
				buf.append(bta[i].getName()+"='"+bta[i].getText()+"'");				
			}
		}
		buf.append( " where berichtid = '"+berichtid+"'");
		//System.out.println(buf.toString());
		SqlInfo.sqlAusfuehren(buf.toString());
		
		ebt.getTab3().textSpeichernInDB(true);
		if(!jetztneu){
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			String empf = (String) cbktraeger.getSelectedItem();
			Gutachten.gutachten.aktualisiereGutachten(datFunk.sHeute(),(empf.contains("DRV") ? "DRV E-Bericht" : "GKV E-Bericht"),empf,"Reha-Arzt",berichtid,pat_intern);
			Reha.thisClass.progressStarten(false);
		}else{
			jetztneu = false;
			Reha.thisClass.progressStarten(false);
		}
	}
	/*************************************************************************/	
	private void doSpeichernNeu(){
		try{
			Reha.thisClass.progressStarten(true);
			int nummer = SqlInfo.erzeugeNummer("bericht");
			berichtid = nummer;
			String empf = (String) cbktraeger.getSelectedItem();
			String btype = (empf.contains("DRV") ? "DRV E-Bericht" : "GKV E-Bericht");
			String cmd = "insert into berhist set berichtid='"+berichtid+"', erstelldat='"
			+datFunk.sDatInSQL(datFunk.sHeute())+"', berichttyp='"+btype+"', "+
			"verfasser='Reha-Arzt', empfaenger='"+empf+"', pat_intern='"+pat_intern+"', bertitel='Reha-Entlassbericht'";
			SqlInfo.sqlAusfuehren(cmd);
			cmd = "insert into bericht2 set berichtid='"+berichtid+"', pat_intern='"+pat_intern+"'";
			SqlInfo.sqlAusfuehren(cmd);
			cmd = "insert into bericht2ktl set berichtid='"+berichtid+"', pat_intern='"+pat_intern+"'";
			SqlInfo.sqlAusfuehren(cmd);
			jetztneu = true; // ganz wichtig
			neu = false;
			System.out.println("Historie- und Bericht wurden angelegt");
			doSpeichernAlt();
			System.out.println("Nach Speichern alt");
			Gutachten.gutachten.neuesGutachten(new Integer(berichtid).toString(),
					btype,"Reha-Arzt",datFunk.sHeute() ,empf, pat_intern,"Reha-Entlassbericht");
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			Reha.thisClass.progressStarten(false);
		}catch(Exception ex){
			Reha.thisClass.progressStarten(true);
			ex.printStackTrace();
		}
	}
	final EBerichtPanel xthis = this;
	private void doVorschauEntlassBericht(boolean nurVorschau,int[] versionen){
		new Thread(){
			public void run(){
				new SwingWorker<Void,Void>(){
					@Override
					protected Void doInBackground() throws Exception {
						Reha.thisClass.progressStarten(true);
						ebtab.setSelectedIndex(0);
						if(((String)cbktraeger.getSelectedItem()).contains("DRV ")){
							try {
								 
						        int sel = ebtab.getSelectedIndex();
								if(document.isModified()){
									System.out.println("in getrennt.....");
									if(sel != 2){
											ebt.getTab3().tempTextSpeichern();
											document.close();
									}else{
										if(document.isModified()){											
											ebt.getTab3().tempTextSpeichern();
										}
									}

								}else{
									System.out.println("in nicht!!!!!getrennt.....");
									if(sel != 2){
										ebt.getTab3().tempTextSpeichern();
										document.close();
									}else{
										ebt.getTab3().tempTextSpeichern();
									}
								}
								
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Hier abpr�fen ob gedruckt werden soll
							//public RVEBerichtPDF(EBerichtPanel xeltern, boolean nurVorschau,int[] versionen)
							new RVEBerichtPDF(xthis,true,new int[] {});
							//doRVVorschau(true,"Ausfertigung f�r den RV-Tr�ger  "+(String)cbktraeger.getSelectedItem(),"Bereich Reha");

						}
						return null;
					}
				}.execute();
			}
		}.start();
	}
	private void doVorschauNachsorge(boolean nurVorschau,int version){
		new NachsorgePDF(this,nurVorschau,version);
	}

	
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void dokumentSchliessen(){
		try{
			document.close();			
		}catch(Exception ex){
			
		}

	}
	
	public JToolBar getToolbar(){
		JToolBar jtb = new JToolBar();
		jtb.setOpaque(false);
		jtb.setRollover(true);
		jtb.setBorder(null);
		jtb.setOpaque(false);

		gutbut[0] = new JButton();
		gutbut[0].setIcon(SystemConfig.hmSysIcons.get("save"));
		gutbut[0].setToolTipText("Gutachten speichern");
		gutbut[0].setActionCommand("gutsave");
		gutbut[0].addActionListener(this);		
		jtb.add(gutbut[0]);
/*
		gutbut[4] = new JButton();
		gutbut[4].setIcon(new ImageIcon(SystemConfig.hmSysIcons.get("ooowriter").getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
		gutbut[4].setToolTipText("Freitext starten");
		gutbut[4].setActionCommand("guttext");
		gutbut[4].addActionListener(this);		
		jtb.add(gutbut[4]);
*/		
		jtb.addSeparator(new Dimension(30,0));
		
		gutbut[1] = new JButton();
		gutbut[1].setIcon( SystemConfig.hmSysIcons.get("vorschau") );
		gutbut[1].setToolTipText("Druckvorschau");
		gutbut[1].setActionCommand("gutvorschau");
		gutbut[1].addActionListener(this);		
		jtb.add(gutbut[1]);

		gutbut[2] = new JButton();
		gutbut[2].setIcon(SystemConfig.hmSysIcons.get("print"));
		gutbut[2].setToolTipText("Alle Ausfertigungen des Gutachten drucken");
		gutbut[2].setActionCommand("gutprint");
		gutbut[2].addActionListener(this);		
		jtb.add(gutbut[2]);
		
		jtb.addSeparator(new Dimension(30,0));
		
		gutbut[3] = new JButton();
		gutbut[3].setIcon(SystemConfig.hmSysIcons.get("tools"));
		gutbut[3].setToolTipText("Werkzeugkasten f�r Gutachten");
		gutbut[3].setActionCommand("guttools");
		gutbut[3].addActionListener(this);		
		jtb.add(gutbut[3]);
		for(int i = 0; i < 5;i++){
			//gutbut[i].setEnabled(false);
		}
		return jtb;
	}
	@Override
	public void RehaEventOccurred(RehaEvent evt) {
		// TODO Auto-generated method stub
		System.out.println(evt);
		System.out.println("GutachtenFenster wird geschlossen.......");
		if(evt.getDetails()[0].contains("GutachtenFenster")){
			if(evt.getDetails()[1].equals("#SCHLIESSEN")){
				if(inebericht){
					dokumentSchliessen();
					if(document != null){
						if(document.isOpen()){
							document.close();
						}
					}
					this.evt.removeRehaEventListener((RehaEventListener)this);
				}else{
					
				}
				FileTools.delFileWithSuffixAndPraefix(new File(tempPfad), "EB", ".pdf");
			}
		}
	}


}
