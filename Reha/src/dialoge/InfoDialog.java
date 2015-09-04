package dialoge;

import hauptFenster.Reha;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.ListenerTools;
import terminKalender.ParameterLaden;
import terminKalender.SchnellSuche;
import utils.ButtonTools;
import utils.DatFunk;
import CommonTools.JRtaTextField;
import CommonTools.StringTools;
import CommonTools.JCompTools;
import CommonTools.SqlInfo;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;

public class InfoDialog extends JDialog implements WindowListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2903001967749371315L;
	private JLabel textlab;
	private JLabel bildlab;
	private KeyListener kl;
	Font font = new Font("Arial",Font.PLAIN,12);
	
	private String arg1 = null;
	private String infoArt = null;
	JEditorPane htmlPane1 = null;
	JEditorPane htmlPane2 = null;
	
	Vector <Vector<String>> vecResult = null;
	Vector<String> tage = null;
	Vector<Vector<String>> tageplus = null;
	boolean historie = false;
	boolean notfound = false;
	String endhinweis = "";
	JScrollPane scr1 = null;
	JScrollPane scr2 = null;
	
	String last12Wo = null;
	
	String disziplin = "";
	int tagebreak = 0;
	
	DecimalFormat df = new DecimalFormat( "0.00" );
	
	boolean muststop = false;
	
	public InfoDialog(String arg1,String infoArt,Vector<Vector<String>> data) {
		super();
		setUndecorated(true);
		setModal(true);
		this.arg1 = arg1;
		this.infoArt = infoArt;
		activateListener();
		this.setLayout(new BorderLayout());
		if(this.infoArt.equals("terminInfo")){
			//this.getContentPane().setLayout(new BorderLayout());
			//this.add(getTerminInfoContent());
			this.setContentPane(getTerminInfoContent());
		}else if(this.infoArt.equals("offenRGAF")){
			this.setContentPane(getOffeneRechnungenInfoContent(data));
		}else{	
			this.setContentPane(getContent());	
		}
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addKeyListener(kl);
		//validate();
		this.getContentPane().validate();
	}
	
	public JXPanel getTerminInfoContent(){
		JXPanel jpan = new JXPanel();
		jpan.addKeyListener(kl);
		//jpan.setPreferredSize(new Dimension(400,100));
		jpan.setBackground(Color.WHITE);
		jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		//jpan.setPreferredSize(new Dimension(1000,750));
		FormLayout lay = new FormLayout("5dlu,p:g,p,p:g,5dlu",
				"5dlu,p,5dlu,p,p,max(350dlu;p),0dlu,100dlu,5dlu");
		jpan.setLayout(lay);
		CellConstraints cc = new CellConstraints();
		bildlab = new JLabel(" ");
		bildlab.setIcon(SystemConfig.hmSysIcons.get("tporgklein"));
		jpan.add(bildlab,cc.xy(3, 2,CellConstraints.FILL,CellConstraints.DEFAULT));
		htmlPane1 = new JEditorPane(/*initialURL*/);
        htmlPane1.setContentType("text/html");
        htmlPane1.setEditable(false);
        htmlPane1.setOpaque(false);
        htmlPane1.addKeyListener(kl);
        //htmlPane1.setPreferredSize(new Dimension(1000,750));
        //htmlPane.addHyperlinkListener(this);
        scr1 = JCompTools.getTransparentScrollPane(htmlPane1);
        
        scr1.validate();	
        jpan.add(scr1,cc.xywh(2,4,3, 3,CellConstraints.FILL,CellConstraints.FILL));

		htmlPane2 = new JEditorPane(/*initialURL*/);
        htmlPane2.setContentType("text/html");
        htmlPane2.setEditable(false);
        htmlPane2.setOpaque(false);
        htmlPane2.addKeyListener(kl);
        

        scr2 = JCompTools.getTransparentScrollPane(htmlPane2);
        scr2.validate();	
        jpan.add(scr2,cc.xyw(2,8,3,CellConstraints.FILL,CellConstraints.FILL));
        
        holeTerminInfo();
       
        htmlPane1.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
					if( extractFieldName(e.getURL().toString()).equals("weiteretermine") ){
						//System.err.println(e.getURL().toString());
						htmlPane1.requestFocus();
						//hier muß der Suchendialog gestartet werden
						starteFolgeTermine();
						
						return;
					}
				}
			}
			private String extractFieldName(String url){
				String ext = url.substring(7);
				return ext.replace(".de", "");
			}
	    });
        
		jpan.validate();
		return jpan;
	}
	
	public JXPanel getOffeneRechnungenInfoContent(Vector<Vector<String>> vdata){
		JXPanel jpan = new JXPanel();
		jpan.addKeyListener(kl);
		//jpan.setPreferredSize(new Dimension(400,100));
		jpan.setBackground(Color.WHITE);
		jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		FormLayout lay = new FormLayout("5dlu,fill:0:grow(0.5),p,fill:0:grow(0.5),5dlu",
				"5dlu,p,5dlu,p,p,250dlu,5dlu,2dlu,150dlu:g,5dlu");
		jpan.setLayout(lay);
		CellConstraints cc = new CellConstraints();
		bildlab = new JLabel(" ");
		bildlab.setIcon(SystemConfig.hmSysIcons.get("tporgklein"));
		jpan.add(bildlab,cc.xy(3, 2));
		htmlPane1 = new JEditorPane(/*initialURL*/);
        htmlPane1.setContentType("text/html");
        htmlPane1.setEditable(false);
        htmlPane1.setOpaque(false);
        htmlPane1.addKeyListener(kl);
        //htmlPane.addHyperlinkListener(this);
        scr1 = JCompTools.getTransparentScrollPane(htmlPane1);
        scr1.validate();	
        jpan.add(scr1,cc.xywh(2,4,3, 4));

		htmlPane2 = new JEditorPane(/*initialURL*/);
        htmlPane2.setContentType("text/html");
        htmlPane2.setEditable(false);
        htmlPane2.setOpaque(false);
        htmlPane2.addKeyListener(kl);
        scr2 = JCompTools.getTransparentScrollPane(htmlPane2);
        scr2.validate();	
        jpan.add(scr2,cc.xywh(2,8,3,2));		

		holeOffeneRechnungen(vdata);
		scr1.validate();
		scr2.validate();
		jpan.revalidate();
		return jpan;
	}	

	private void activateListener(){
		kl = new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_F1 && (!e.isControlDown()) && (!e.isShiftDown())){
					dispose();
				}
				
			}
			
		};
	}
	public JXPanel getContent(){
	JXPanel jpan = new JXPanel();
	jpan.addKeyListener(kl);
	jpan.setPreferredSize(new Dimension(400,100));
	jpan.setBackground(Color.WHITE);
	jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));

	FormLayout lay = new FormLayout("fill:0:grow(0.5),p,fill:0:grow(0.5)",
			"fill:0:grow(0.25),p,15dlu,p,fill:0:grow(0.75)");
	jpan.setLayout(lay);
	CellConstraints cc = new CellConstraints();
	bildlab = new JLabel(" ");
	bildlab.setIcon(SystemConfig.hmSysIcons.get("tporgklein"));
	jpan.add(bildlab,cc.xy(2, 2));
	textlab = new JLabel(" ");
	textlab.setFont(font);
	textlab.setForeground(Color.BLUE);
	jpan.add(textlab,cc.xy(2, 4,CellConstraints.CENTER,CellConstraints.CENTER));
	jpan.validate();
	int tagebeginn = 0;
	int tagebreak = 0;
	return jpan;
	
	}
	public void setzeLabel(String labelText){
		textlab.setText(labelText);
		textlab.getParent().validate();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void windowOpened(WindowEvent e) {
	}
	@Override
	public void windowClosing(WindowEvent e) {
	}
	@Override
	public void windowClosed(WindowEvent e) {
	}
	@Override
	public void windowIconified(WindowEvent e) {
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	@Override
	public void windowActivated(WindowEvent e) {
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	private void holeTerminInfo(){
		//vecResult = SqlInfo.holeFelder("select lastdate,termine from verordn where rez_nr = '"+macheNummer(this.arg1)+"' LIMIT 1");
		vecResult = SqlInfo.holeFelder("select t1.lastdate,t1.termine,t2.n_name,t2.v_name,t1.kuerzel1,t1.anzahl1,t1.kuerzel2,t1.anzahl2,t1.kuerzel3,t1.anzahl3,t1.kuerzel4,t1.anzahl4,ktraeger,rezeptart,zzstatus,preisgruppe,rez_datum,arztbericht,berid from verordn as t1 join pat5 as t2 on(t2.pat_intern = t1.pat_intern) where t1.rez_nr = '"+macheNummer(this.arg1)+"' LIMIT 1");
		if(vecResult.size()<=0){
			this.historie=true;
			vecResult = SqlInfo.holeFelder("select t1.lastdate,t1.termine,t2.n_name,t2.v_name,t1.kuerzel1,t1.anzahl1,t1.kuerzel2,t1.anzahl2,t1.kuerzel3,t1.anzahl3,t1.kuerzel4,t1.anzahl4,ktraeger,rezeptart,zzstatus,preisgruppe,rez_datum,arztbericht,berid from lza as t1 join pat5 as t2 on(t2.pat_intern = t1.pat_intern) where t1.rez_nr = '"+macheNummer(this.arg1)+"' LIMIT 1");
			if(vecResult.size() > 0){
				JOptionPane.showMessageDialog(null,"Dieses Rezept ist bereits in der Historie - also abgerechnet!"+"\nRezeptnummer: "+this.arg1);				
			}else{
				JOptionPane.showMessageDialog(null,"Dieses Rezept ist weder im aktuellen Rezeptstamm noch in der Historie - also gelöscht!"+"\nRezeptnummer: "+this.arg1);
			}
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					dispose();
				}
			});
			return;
		}
		
		try{
			//int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(disziplin).get((erstebehandlung ? 0 : 2))).get(preisgruppe);
			disziplin = StringTools.getDisziplin(this.arg1);
			
			tagebreak = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(disziplin).get(2)).get(Integer.parseInt(vecResult.get(0).get(15))-1 );

		}catch(Exception ex){
			tagebreak = 28;
			ex.printStackTrace();
		}
		
		fuelleHTML();
	}
	
	private void fuelleHTML(){
		try{
			String shtml = ladehead()+
			getMittelTeil()+
			ladeend();
			htmlPane1.setText(shtml);
			scr1.revalidate();
			htmlPane1.validate();
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					if(scr1.isVisible()){
						scr1.getVerticalScrollBar().setValue(0);
						//scr1.getViewport().scrollRectToVisible(new Rectangle(0,0));
					}
				}
			});
			/**************/
			shtml = ladehead()+
					getErgebnisTeil();
					ladeend();
			htmlPane2.setText(shtml);
			scr2.revalidate();
			htmlPane2.validate();
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					if(scr2.isVisible()){
						scr2.getVerticalScrollBar().setValue(0);
						//scr1.getViewport().scrollRectToVisible(new Rectangle(0,0));
					}
				}
			});
			
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static String macheNummer(String string){
		if(string.indexOf("\\") >= 0){
			return string.substring(0,string.indexOf("\\"));
		}
		return string;
	}
	/***************************************************/
	public String getErgebnisTeil(){
		StringBuffer  ergebnis = new StringBuffer();
		if(tage.size() == Integer.parseInt(vecResult.get(0).get(5))){
			ergebnis.append("<span "+getSpanStyle("12","#ff0000")+"<b>Das Rezept ist voll</b></span>\n");
		}else if(tage.size() > 0 && tage.size() < Integer.parseInt(vecResult.get(0).get(5))){
			String slastdate = DatFunk.sDatPlusTage(tage.get(tage.size()-1), tagebreak);
			String lastdate = DatFunk.WochenTag(slastdate)+" "+slastdate;
			long diff = 0;
			if( (diff = DatFunk.TageDifferenz(slastdate, DatFunk.sHeute())) > 0 ){
				ergebnis.append("<span "+getSpanStyle("14","#ff0000")+"<b>"+lastdate+"</b></span><br>"+"<span "+getSpanStyle("10","")+"Maximale Unterbrechung verpasst "+lastdate+"!! ("+Integer.toString(tagebreak)+" Tage)</span>\n");	
			}else{
				ergebnis.append("<span "+getSpanStyle("14","#008000")+"<b>"+lastdate+"</b></span><br>"+"<span "+getSpanStyle("10","")+"nächster Termin spätestens am " +lastdate+" ("+Integer.toString(tagebreak)+" Tage)</span>\n");
			}
			
		}else if(tage.size() == 0){
			long diff = 0;
			if((diff = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(vecResult.get(0).get(0)), DatFunk.sHeute())) > 0){
				ergebnis.append("<span "+getSpanStyle("14","#ff0000")+"<b>"+DatFunk.sDatInDeutsch(vecResult.get(0).get(0))+"</b></span><br>"+"<span "+getSpanStyle("10","")+"Achtung Behandlungsbeginn heute überschreitet<br>den spätestesn Behandlungsbeginn<br>um "+Long.toString(diff)+" Tage!!</span>\n");
			}else{
				ergebnis.append("<span "+getSpanStyle("14","#008000")+"<b>"+DatFunk.sDatInDeutsch(vecResult.get(0).get(0))+"</b></span><br>"+"<span "+getSpanStyle("10","")+"Behandlung noch nicht begonnen stand heute<br>spätester Behandlungsbeginn<br>in "+Long.toString(diff)+" Tage(n)!!</span>\n");				
			}
		}
		//jetzt den Saich mit der 12-Wochenfrist für ADR
		if(vecResult.get(0).get(13).equals("2") && tage.size() > 0){
			String lastdate = DatFunk.sDatPlusTage(tage.get(0), (12*7)-1);
			long diff = DatFunk.TageDifferenz(lastdate, DatFunk.sHeute());
			if(diff > 0){
				ergebnis.append("<span "+getSpanStyle("14","#ff0000")+"<br><br><b>"+DatFunk.WochenTag(lastdate)+" "+lastdate+" VO-AdR</b></span><br>"+"<span "+getSpanStyle("10","")+"letzter Behandlungstermin spätestens am "+lastdate+" (12 Wochenfrist)</span>\n");	
			}else{
				ergebnis.append("<span "+getSpanStyle("14","#008000")+"<br><br><b>"+DatFunk.WochenTag(lastdate)+" "+lastdate+" VO-AdR</b></span><br>"+"<span "+getSpanStyle("10","")+"letzter Behandlungstermin spätestens am  "+lastdate+" (12 Wochenfrist)</span>\n");
			}
		}else if(!vecResult.get(0).get(13).equals("2") && Integer.parseInt(vecResult.get(0).get(5)) >= 10){
			String lastdate = DatFunk.sDatPlusTage(tage.get(0), (12*7)-1);
			long diff = DatFunk.TageDifferenz(lastdate, DatFunk.sHeute());
			if(diff > 0){
				ergebnis.append("<span "+getSpanStyle("14","#ff0000")+"<br><br><b>"+DatFunk.WochenTag(lastdate)+" "+lastdate+"</b></span><br>"+"<span "+getSpanStyle("10","")+"letzter Behandlungstermin spätestens am "+lastdate+" (12 Wochenfrist)</span>\n");	
			}else{
				ergebnis.append("<span "+getSpanStyle("14","#008000")+"<br><br><b>"+DatFunk.WochenTag(lastdate)+" "+lastdate+"</b></span><br>"+"<span "+getSpanStyle("10","")+"letzter Behandlungstermin spätestens am "+lastdate+" (12 Wochenfrist)</span>\n");
			}
		}
		
		

		
		return ergebnis.toString();
	}	
	/***************************************************/
	public String getMittelTeil(){
		StringBuffer  mitte = new StringBuffer();
		boolean zuzahl = (vecResult.get(0).get(14).equals("0") ? false : true);
		tage =  RezTools.holeEinzelTermineAusRezept("",vecResult.get(0).get(1)) ;
		tageplus = RezTools.holeTermineUndBehandlerAusRezept("", vecResult.get(0).get(1));
		Object[] otest = {null,null};
		/**********************/
		if(tageplus.size()/*tage.size()*/ > 0){
			last12Wo = DatFunk.sDatPlusTage(tageplus.get(0).get(0)/*tage.get(0)*/, (12*7)-1);	
		}else{
			last12Wo = DatFunk.sDatPlusTage(DatFunk.sDatInDeutsch(vecResult.get(0).get(0)), (12*7)-1);
		}
		
		//System.out.println("12 Wochenfrist läuft ab am "+last12Wo);
		/*********************/
		
		mitte.append("<span "+getSpanStyle("12","")+StringTools.EGross(vecResult.get(0).get(2))+", "+StringTools.EGross(vecResult.get(0).get(3))+"<br>Rezeptnummer: "+this.arg1+
				"<br>"+StringTools.EGross(vecResult.get(0).get(12))+"</span>"+"<br><br>\n");
		mitte.append("<span "+getSpanStyle("10","")+getPositionen()+"</span>"+"<br><br>\n");
		
		if(!zuzahl){
			mitte.append("<span "+getSpanStyle("10","")+"Rezeptgebühr: </span><span "+getSpanStyle("10","#008000")+ "<b>nicht erforderlich oder befreit</b>"+"</span>"+"<br>\n");
		}else{
			if(vecResult.get(0).get(14).equals("1")){
				mitte.append("<span "+getSpanStyle("10","")+"Rezeptgebühr: </span><span "+getSpanStyle("10","#008000")+ "<b>bezahlt</b>"+"</span>"+"<br>\n");
			}else if(vecResult.get(0).get(14).equals("2")){
				mitte.append("<span "+getSpanStyle("10","")+"Rezeptgebühr: </span><span "+getSpanStyle("10","#ff0000")+ "<b>nicht bezahlt</b>"+"</span>"+"<br>\n");
			}
				
		}
		if(vecResult.get(0).get(17).equals("T")){
			if(vecResult.get(0).get(18).equals("") || vecResult.get(0).get(18).equals("-1") || vecResult.get(0).get(18).equals("0") ){
				mitte.append("<span "+getSpanStyle("10","")+"Arztbericht: </span><span "+getSpanStyle("10","#ff0000")+ "<b>noch nicht erstellt</b>"+"</span>"+"<br>\n");
			}else{
				mitte.append("<span "+getSpanStyle("10","")+"Arztbericht: </span><span "+getSpanStyle("10","#008000")+ "<b>bereits angelegt</b>"+"</span>"+"<br>\n");
			}
		}
		mitte.append("<span "+getSpanStyle("10","")+"Bislang durchgeführt: <b>"+Integer.toString(tageplus.size()/*tage.size()*/)+" von "+vecResult.get(0).get(5)+"</b></span>"+"<br>\n");
		mitte.append("<span "+getSpanStyle("10","")+"Rezeptdatum: <b>"+DatFunk.sDatInDeutsch(vecResult.get(0).get(16))+"</b></span>"+"<br>\n");
		mitte.append("<span "+getSpanStyle("10","")+"Spätester Behandlungsbeginn: <b>"+DatFunk.sDatInDeutsch(vecResult.get(0).get(0))+"</b></span>"+"<br><br>\n");	
		
		int pghmr = Integer.parseInt(vecResult.get(0).get(15));
		String disziplin = StringTools.getDisziplin(this.arg1);

		if(SystemPreislisten.hmHMRAbrechnung.get(disziplin).get(pghmr-1) < 1){
			mitte.append("<span "+getSpanStyle("14","#ff0000")+"<b>Keine HMR-Prüfung erforderlich</b>"+"</span>"+"<br><br>\n");
		}
		mitte.append("<table width='100%'>\n");
		/***********************************************************************************/
		long diff = 0;
		long diff1 = 0;
		
		for(int i = 0; i < tageplus.size()/*tage.size()*/;i++){
			mitte.append("<tr>\n");
			mitte.append("<td class='itemkleinodd'>"+Integer.toString(i+1)+"</td>\n");
			mitte.append("<td class='itemkleinodd' align=\"center\">"+tageplus.get(i).get(0)/*tage.get(i)*/+"</td>\n");
			
			if(i==0){
				//zuerst testen ob vor dem Rezeptdatum begonnen wurde
				if( (diff1=DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(vecResult.get(0).get(16)), tageplus.get(i).get(0)/*tage.get(i)*/)) < 0){
					mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+" < Rezeptdatum<br>"+Long.toString(diff1)+" Tage"+"</td>\n");
					mitte.append("<td>&nbsp;</td>\n");
					//mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+"</td>\n");
					//mitte.append("<td>"+" < Rezeptdatum<br>"+Long.toString(diff1)+" Tage"+"</td>\n");
				}else{
					if( (diff = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(vecResult.get(0).get(0)), tageplus.get(i).get(0)/*tage.get(i)*/)) > 0){
						mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+" > "+Long.toString(diff)+" Tage"+"</td>\n");
						//mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+"</td>\n");
						//mitte.append("<td> > "+Long.toString(diff)+" Tage</td>\n");
						otest = Wochen12Test(last12Wo,tageplus.get(i).get(0)/*tage.get(i)*/);
						if( ((Boolean)otest[0]) == (Boolean) true){
							mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
						}else{
							mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
							//mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+"</td>\n");
							//mitte.append("<td>"+" 12 Wo."+"</td>\n");
						}
					}else{
						mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+" <= "+Long.toString(diff)+" Tage"+"</td>\n");
						otest = Wochen12Test(last12Wo,tageplus.get(i).get(0)/*tage.get(i)*/);
						if( ((Boolean)otest[0]) == (Boolean) true){
							mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
							//mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+"</td>\n");
							//mitte.append("<td>"+" 12 Wo."+"</td>\n");
						}else{
							mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
							//mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+"</td>\n");
							//mitte.append("<td>"+" 12 Wo."+"</td>\n");
						}
					}
				}
				mitte.append("<td>"+tageplus.get(i).get(1)+"</td>\n");
			}else if(i > 0 ){
				if( (diff = DatFunk.TageDifferenz(tageplus.get(i-1).get(0)/*tage.get(i-1)*/,tageplus.get(i).get(0)/*tage.get(i)*/)) > tagebreak){
					mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+" > "+Long.toString(diff)+" Tage"+"</td>\n");
					otest = Wochen12Test(last12Wo,tageplus.get(i).get(0)/*tage.get(i)*/);
					if( ((Boolean)otest[0]) == (Boolean) true){
						mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
					}else{
						mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
					}

				}else{
					mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+" <= "+Long.toString(diff)+" Tage"+"</td>\n");
					otest = Wochen12Test(last12Wo,tageplus.get(i).get(0)/*tage.get(i)*/);
					if( ((Boolean)otest[0]) == (Boolean) true){
						mitte.append("<td><img src='file:///"+Reha.proghome+"icons/nichtok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
					}else{
						mitte.append("<td><img src='file:///"+Reha.proghome+"icons/ok.gif"+"'>"+/*otest[1].toString()*/""+" 12 Wo."+"</td>\n");
					}

				}
				mitte.append("<td style='white-space: nowrap;'>"+tageplus.get(i).get(1)+"</td>\n");
				//mitte.append("<td style='white-space: nowrap;'>&nbsp;</td>\n");
			}
			
			mitte.append("</tr>\n");
		}
		/***********************************************************************************/
		mitte.append("<tr><td>&nbsp;</td>\n");
		mitte.append("<td align=\"center\">"+makeLink("fragezeichen.png","weiteretermine")+"</td></tr>");
		mitte.append("</table>\n");
		/*
		mitte.append("<br>");
		mitte.append("<form action='http://fakeurl.com:1'><input type='submit' value=' Folgetermine ? ' /></form>");
		*/
//		mitte.append("<object classid=\"javax.swing.JButton\" label=\" Folgetermine ?\"></object>");

		return mitte.toString();
	}
	/***************************************************/
	private String makeLink(String ico, String url){
		String linktext = "<img src='file:///"+Reha.proghome+"icons/"+ico+"'  border=0>";
		linktext = "<a href=\"http://"+url+".de\">"+linktext+"</a>\n";
		return linktext;
		
	}
	private Object[] Tage28Test(String datumalt,String datumneu){
		Object[] oret = {null,null};
		long wert = 0;
		return oret;
	}
	private Object[] Wochen12Test(String datumalt,String datumneu){
		Object[] oret = {null,null};
		long wert = 0;
		wert = DatFunk.TageDifferenz(datumalt,datumneu);
		oret[1] = (int) Integer.parseInt( Long.toString(wert-1) );
		if( wert > 0){
			oret[0] = (Boolean) true;	
		}else{
			oret[0] = (Boolean) false;
		}
		return (Object[])oret.clone();
	}
	private String getPositionen(){
		StringBuffer  positionen = new StringBuffer();
		positionen.append( (!vecResult.get(0).get(4).equals("") ? vecResult.get(0).get(5)+" x "+vecResult.get(0).get(4) : "")+
				(!vecResult.get(0).get(6).equals("") ? ", "+vecResult.get(0).get(7)+" x "+vecResult.get(0).get(6) : "")+
				(!vecResult.get(0).get(8).equals("") ? ", "+vecResult.get(0).get(9)+" x "+vecResult.get(0).get(8) : "")+
				(!vecResult.get(0).get(10).equals("") ? ", "+vecResult.get(0).get(11)+" x "+vecResult.get(0).get(10) : "") );
		
		return positionen.toString();
	}
	private String getSpanStyle(String pix,String color){
		return "style='font-family: Arial, Helvetica, sans-serif; font-size: "+pix+"px;"+(color.length()>0 ? "color: "+color+";" : "")+ " '>";
	}
	final StringBuffer  bufhead = new StringBuffer();
	final StringBuffer  bufend = new StringBuffer();
	public String ladehead(){
		bufhead.append("<html>\n<head>\n");
		bufhead.append("<STYLE TYPE=\"text/css\">\n");
		bufhead.append("<!--\n");
		bufhead.append("A{text-decoration:none;background-color:transparent;border:none}\n");
		bufhead.append("A.even{text-decoration:underline;color: #000000; background-color:transparent;border:none}\n");
		bufhead.append("A.odd{text-decoration:underline;color: #FFFFFF;background-color:transparent;border:none}\n");
		bufhead.append("TD{font-family: Arial; font-size: 12pt; vertical-align: top;white-space: nowrap;}\n");
		bufhead.append("TD.inhalt {font-family: Arial, Helvetica, sans-serif; font-size: 20px;background-color: #7356AC;color: #FFFFFF;}\n");
		bufhead.append("TD.inhaltinfo {font-family: Arial, Helvetica, sans-serif; font-size: 20px;background-color: #DACFE7; color: #1E0F87;}\n");
		bufhead.append("TD.headline1 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #EADFF7; color: #000000;}\n");
		bufhead.append("TD.headline2 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #DACFE7; color: #000000;}\n");		
		bufhead.append("TD.headline3 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #7356AC; color: #FFFFFF;}\n");		
		bufhead.append("TD.headline4 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #1E0F87; color: #FFFFFF;}\n");		
		bufhead.append("TD.itemeven {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #E6E6E6; color: #000000;}\n");
		bufhead.append("TD.itemodd {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #737373; color: #F0F0F0;}\n");
		bufhead.append("TD.itemkleineven {font-family: Arial, Helvetica, sans-serif; font-size: 9px; background-color: #E6E6E6; color: #000000;}\n");
		bufhead.append("TD.itemkleinodd {font-family: Arial, Helvetica, sans-serif; font-size: 9px; background-color: #737373; color: #F0F0F0;}\n");
		bufhead.append("TD.header {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #1E0F87; color: #FFFFFF;}\n");
		bufhead.append("UL {font-family: Arial, Helvetica, sans-serif; font-size: 9px;}\n");
		bufhead.append("UL.paeb { margin-top: 0px; margin-bottom: 0px; }\n");
		bufhead.append("H1 {font-family: Arial, Helvetica, sans-serif; font-size: 12px; background-color: #1E0F87; color: #FFFFFF;}\n");
		bufhead.append("--->\n");
		bufhead.append("</STYLE>\n");
		bufhead.append("</head>\n");
		bufhead.append("<body>\n");
		//bufhead.append("<div style=margin-left:30px;>");
		//bufhead.append("<font face=\"Tahoma\"><style=margin-left=30px;>");
		return bufhead.toString();
		

					
	}
	public String ladeend(){
		bufend.append("</body>\n</html>\n");
		return bufend.toString();
	}
	
	/***************************************************/
	public void holeOffeneRechnungen(Vector<Vector<String>> data){
		String complete = ladehead();
		StringBuffer bdata = new StringBuffer();
		bdata.append("<span "+getSpanStyle("14","")+"Offene RGR-/AFR-Rechnungen</span><br>\n");
		bdata.append("<table width='100%'>\n"); 
		Double gesamt = 0.00;
		//String stmt = "select t1.rdatum,t1.rnr,t1.roffen,t1.pat_intern from rgaffaktura as t1 join pat5 as t2 on (t1.pat_intern=t2.pat_intern) where t1.roffen > '0' and t1.pat_intern = '"+xpatint+"' order by t1.rdatum";
		for( int i = 0; i < data.size();i++){
			bdata.append("<tr>\n");
			bdata.append("<td>"+Integer.toString(i+1)+".</td>\n");
			bdata.append("<td>\n");
			bdata.append(DatFunk.sDatInDeutsch(data.get(i).get(0)));
			bdata.append("</td>\n");
			bdata.append("<td>\n");
			bdata.append(data.get(i).get(1));
			bdata.append("</td>\n");
			bdata.append("<td>\n");
			bdata.append(data.get(i).get(2).replace(".", ",")+" EUR");
			bdata.append("</td>\n");
			bdata.append("</tr>\n");
			gesamt = gesamt+Double.parseDouble(data.get(i).get(2));
		}
		bdata.append("<tr>\n");
		bdata.append("<td>\n");
		bdata.append("&nbsp;");
		bdata.append("</td>\n");
		bdata.append("<td>\n");
		bdata.append("<b>Summe</b>");
		bdata.append("</td>\n");
		bdata.append("<td>\n");
		bdata.append("&nbsp;");
		bdata.append("</td>\n");
		bdata.append("<td>\n");
		bdata.append("<b>"+df.format( gesamt)+"</b> EUR");
		bdata.append("</td>\n");
		bdata.append("</tr>\n");
		
		bdata.append("</table>\n");
		complete = complete+bdata.toString()+ladeend();
		htmlPane1.setText(complete);
		
		bdata.setLength(0);
		bdata.trimToSize();
		complete = "";
		complete = ladehead();
		bdata.append("<span "+getSpanStyle("14","")+"Merkmale für diesen Patient</span><br>\n");
		int durchlauf = 0;
		for(int i = 62; i > 56;i--){
			if(Reha.thisClass.patpanel.patDaten.get(i).equals("T")){
				/*
				vPatMerker.add(inif.getStringProperty("Kriterien", "Krit"+i));
				String simg = inif.getStringProperty("Kriterien", "Image"+i);
				if(simg.equals("")){
					vPatMerkerIcon.add(null);
				}else{
					vPatMerkerIcon.add(new ImageIcon(Reha.proghome+"icons/"+simg));
				}*/
				if(SystemConfig.vPatMerkerIconFile.get(durchlauf) != null){
					bdata.append("<img src='file:///"+SystemConfig.vPatMerkerIconFile.get(durchlauf)+"'>&nbsp;&nbsp;");	
				}
				
				bdata.append("<span "+getSpanStyle("12","#FF0000")+SystemConfig.vPatMerker.get(durchlauf)+"</span><br>\n");
				
				
			}
			durchlauf++;
		}
		complete = complete+bdata.toString()+ladeend();
		htmlPane2.setText(complete);
		 
	}
	
	private void starteFolgeTermine(){
		FolgeTermine folgeTermine = new FolgeTermine();
		PinPanel pinPanel = new PinPanel();
		pinPanel.setName("FolgeTermine");
		pinPanel.getGruen().setVisible(false);
		folgeTermine.getSmartTitledPanel().setTitle("<html><b>Folgetermine suchen</b></html>");
		folgeTermine.setSize(500,800);
		folgeTermine.setPreferredSize(new Dimension(580+Reha.zugabex,350+Reha.zugabey));
		folgeTermine.getSmartTitledPanel().setPreferredSize(new Dimension (580,350)); //Original 630
		folgeTermine.setPinPanel(pinPanel);
		folgeTermine.getSmartTitledPanel().setContentContainer( new FolgeTermineSuchen() );
		folgeTermine.getSmartTitledPanel().getContentContainer().setName("FolgeTermine");
		folgeTermine.setName("FolgeTermine");
		folgeTermine.setModal(true);
		//folgeTermine.setLocationRelativeTo(this);
		folgeTermine.setLocation(this.getLocation().x+150, this.getLocation().y+250);
		folgeTermine.pack();
		folgeTermine.setVisible(true);
		folgeTermine.dispose();
		
	}
	
	/***************************************/
	class FolgeTermine extends RehaSmartDialog implements RehaTPEventListener,WindowListener{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private RehaTPEventClass rtp = null;
		public FolgeTermine(){
			super(null,"FolgeTermine");
			this.setName("FolgeTermine");
			//super.getPinPanel().setName("RezeptNeuanlage");
			rtp = new RehaTPEventClass();
			rtp.addRehaTPEventListener((RehaTPEventListener) this);

		}
		public void rehaTPEventOccurred(RehaTPEvent evt) {

			try{

				if(evt.getDetails()[0] != null){
					if(evt.getDetails()[0].equals(this.getName())){
						this.setVisible(false);
						this.dispose();
						rtp.removeRehaTPEventListener((RehaTPEventListener) this);
						rtp = null;
						ListenerTools.removeListeners(this);					
						super.dispose();
					}
				}else{
					//System.out.println("Details == null");
				}
			}catch(NullPointerException ne){
				ne.printStackTrace();
				//System.out.println("In FolgeTermine " +evt);
			}catch(Exception ex){
				
			}
		}	
		
		public void windowClosed(WindowEvent arg0) {
			if(rtp != null){
				this.setVisible(false);			
				rtp.removeRehaTPEventListener((RehaTPEventListener) this);		
				rtp = null;
				dispose();
				ListenerTools.removeListeners(this);
				super.dispose();
				//System.out.println("****************Rezept Neu/ändern -> Listener entfernt (Closed)**********");
			}
		}
	}
	/**************************************************/
	class FolgeTermineSuchen extends JXPanel implements KeyListener,FocusListener,RehaTPEventListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private RehaTPEventClass rtp = null;
		MyFolgeTermineTableModel mod = new MyFolgeTermineTableModel();
		JXTable tab;
		JRtaTextField tfstart;
		JRtaTextField tfsuche;
		JLabel lbstart;
		JLabel lbaktuell;
		JButton[] buts = {null,null,null};
		String suchkrit;
		String startdate;
		String aktdate;
		ActionListener al;
		public FolgeTermineSuchen(){
//			public RezNeuanlage(Vector<String> vec,boolean neu,String sfeldname, boolean bCtrlPressed){
				super();
				rtp = new RehaTPEventClass();
				rtp.addRehaTPEventListener((RehaTPEventListener) this);
				al = new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						String action = e.getActionCommand();
						if(action.equals("start")){
							muststop = false;
							new SuchenInTagen().setzeStatement(lbaktuell, startdate, tfsuche.getText().split(" "), mod);
						}else if(action.equals("stop")){
							muststop = true;
							
						}else if(action.equals("close")){
							muststop = true;
							dispose();
						}
						
					}
					
				};
				//           1   2  3    4     5   6  7    8     9   10 11   12
				String x = "5dlu,p,2dlu,80dlu,5dlu,p,2dlu,80dlu,5dlu,p,2dlu,80dlu,0dlu:g,5dlu";
				//           1   2  3   4  5    6  
				String y = "5dlu,p,5dlu,p,5dlu,30dlu:g,5dlu";
				FormLayout lay = new FormLayout(x , y);
				CellConstraints cc = new CellConstraints();
				
				setLayout(lay);
				add(new JLabel("suche nach"),cc.xy(2, 2));
				add((tfsuche= new JRtaTextField("GROSS",true)),cc.xy(4, 2));
				add(new JLabel("starte bei"),cc.xy(6, 2));
				add((tfstart = new JRtaTextField("DATUM",true)),cc.xy(8, 2));
				add(new JLabel("aktuell bei"),cc.xy(10, 2));
				add( (lbaktuell = new JLabel("")) ,cc.xy(12, 2));
				lbaktuell.setForeground(Color.RED);
				String[] column = {"Tag","Datum","Uhrzeit","Name","Rez.Nummer","Therapeut","Tage diff."};
				mod.setColumnIdentifiers(column);

				tab = new JXTable(mod);
				JScrollPane scr = JCompTools.getTransparentScrollPane(tab);
				scr.validate();
				add(scr,cc.xyw(2,6,12));
				//add( ButtonTools.macheBut("start","start", al),cc.xy(4,4));
				add( ButtonTools.macheBut("stop","stop", al),cc.xy(4,4));
				//add( ButtonTools.macheBut("schliessen","close", al),cc.xy(8,4));
				validate();
				if(tageplus.size() <= 0){
					startdate = DatFunk.sHeute();
				}else{
					startdate = DatFunk.sDatPlusTage(tageplus.get(tageplus.size()-1).get(0),1); 
				}
				tfstart.setText(startdate);
				suchkrit = vecResult.get(0).get(2) +" "+arg1;
				tfsuche.setText(suchkrit);
				lbaktuell.setText(startdate);
				starteSuche();
				
		}		
		@Override
		public void rehaTPEventOccurred(RehaTPEvent evt) {
			try{
				if(evt.getDetails()[0] != null){
					if(evt.getDetails()[0].equals(this.getName())){
						this.setVisible(false);
						rtp.removeRehaTPEventListener((RehaTPEventListener) this);
						rtp = null;
						//aufraeumen();
					}
				}
			}catch(NullPointerException ne){
				JOptionPane.showMessageDialog(null, "Fehler beim abhängen des Listeners Rezept-Neuanlage\n"+
						"Bitte informieren Sie den Administrator über diese Fehlermeldung");
			}
			
		}
		@Override
		public void focusGained(FocusEvent e) {
		}
		@Override
		public void focusLost(FocusEvent e) {
		}
		@Override
		public void keyTyped(KeyEvent e) {
		}
		@Override
		public void keyPressed(KeyEvent e) {
		}
		@Override
		public void keyReleased(KeyEvent e) {
		}
		public void starteSuche(){
			new SuchenInTagen().setzeStatement(lbaktuell, startdate, tfsuche.getText().split(" "), mod);
		}
		
		
	}
/*****************************************************/
	class MyFolgeTermineTableModel extends DefaultTableModel{
		   /**
		 * 
		 */
		private static final long serialVersionUID = 1L;


		public Class<?> getColumnClass(int columnIndex) {
			   if(columnIndex==0){return String.class;}
			  /* if(columnIndex==1){return JLabel.class;}*/
			   else{return String.class;}
	  //return (columnIndex == 0) ? Boolean.class : String.class;
	}

		    public boolean isCellEditable(int row, int col) {
		        //Note that the data/cell address is constant,
		        //no matter where the cell appears onscreen.
		    	return false;
		      }

			public Object getValueAt(int rowIndex, int columnIndex) {
				String theData = (String) ((Vector<?>)getDataVector().get(rowIndex)).get(columnIndex); 
				Object result = null;
				//result = theData.toUpperCase();
				result = theData;
				return result;
			}
		    
		   
	}
/*****************************************************/
	final class SuchenInTagen extends Thread implements Runnable{
		Statement stmt = null;
		ResultSet rs = null;
		String sergebnis = "";
		int belegt = 0;
		String anzeigedatum;
		String[] suchkrit = null;
		JLabel lbaktuell;
		String startdat;
		MyFolgeTermineTableModel mod;
		Vector<String> atermine = new Vector<String>(); 
		public void setzeStatement(JLabel lbaktuell, String startdat, String[] suchkrit,MyFolgeTermineTableModel mod){
			this.lbaktuell = lbaktuell;
			this.startdat = startdat;
			this.mod = mod;
			anzeigedatum = lbaktuell.getText();
			this.suchkrit = suchkrit;
			start();
		}
		public void run(){
			Vector treadVect = new Vector();
			try {
				stmt = (Statement) Reha.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
	                    ResultSet.CONCUR_UPDATABLE );
				while(!muststop){
					try{
						rs = (ResultSet) stmt.executeQuery("select * from flexkc where datum = '"+DatFunk.sDatInSQL(anzeigedatum)+"' LIMIT "+Integer.toString(ParameterLaden.maxKalZeile) ) ;
						////System.out.println("Nach for..."+exStatement[i]);
						//SchnellSuche.thisClass.setLabelDatum("nach ExecuteQuery");
						while(rs.next()){
							try{
								/*in Spalte 301 steht die Anzahl der belegten Bl�cke*/ 
								belegt = rs.getInt(301);
								//SchnellSuche.thisClass.setLabelDatum(DatFunk.sDatInDeutsch(rs.getString(305)));
								String name = "";
								String nummer = "";
								String termin = "";
								String sdatum = "";
								String sorigdatum = "";
								String uhrzeit = "";
								String skollege = "";
								int ikollege = 0;
								for(int ii = 0;ii < belegt;ii++){
									name = rs.getString("T"+(ii+1)); 
									nummer = rs.getString("N"+(ii+1));
									skollege = rs.getString(303).substring(0,2);
									if( skollege.substring(0,1).equals("0") ){
										ikollege = Integer.parseInt(skollege.substring(1,2));
									}else{
										ikollege = Integer.parseInt(skollege);								
									}
									if(name.contains(suchkrit[0]) || nummer.contains(suchkrit[1]) ){
										uhrzeit = rs.getString("TS"+(ii+1));
										sorigdatum = rs.getString(305); 
										sdatum = DatFunk.sDatInDeutsch(sorigdatum);
										skollege = (String) ParameterLaden.getKollegenUeberReihe(ikollege);
										//skollege = (String) ParameterLaden.vKollegen.get(ikollege).get(0);
										
										termin = DatFunk.WochenTag(sdatum)+" - "+sdatum+" - "+uhrzeit+
										"  -  "+name +" - "+nummer+" - "+skollege;
										//SchnellSuche.thisClass.setTextAreaText(termin);
										atermine.add(DatFunk.WochenTag(sdatum));
										atermine.add(sdatum);
										atermine.add(uhrzeit.substring(0,5));
										atermine.add(name);
										atermine.add(nummer);								
										atermine.add(skollege);								
										atermine.add(sorigdatum+uhrzeit.substring(0,5));	
										mod.addRow((Vector)atermine.clone());
										//treadVect.addElement(atermine.clone());
										//SchnellSuche.thisClass.setTerminTable((ArrayList) atermine.clone());
										atermine.clear();
									}
								}
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
						
					}catch(SQLException ev){
						//System.out.println("SQLException: " + ev.getMessage());
						//System.out.println("SQLState: " + ev.getSQLState());
						//System.out.println("VendorError: " + ev.getErrorCode());
					}
					anzeigedatum = DatFunk.sDatPlusTage(anzeigedatum, 1);
					if(DatFunk.TageDifferenz(startdat, anzeigedatum) > 90){
						muststop = true;
					}else{
						this.lbaktuell.setText(anzeigedatum);	
					}
					
				}

				
				
				//SchnellSuche.thisClass.setTerminTable((Vector) treadVect.clone());
			}catch(SQLException ex) {
				//System.out.println("von stmt -SQLState: " + ex.getSQLState());
			}

			finally {
				if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { // ignore }
					rs = null;
				}
				if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { // ignore }
					stmt = null;
				}
				}
				}
			}
			 
		}
		
	}
	
/*****************************************************/
/*****************************************************/
}

