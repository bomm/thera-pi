package offenePosten;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

import Tools.DatFunk;

import com.jgoodies.looks.windows.WindowsTabbedPaneUI;

public class OffenepostenTab extends JXPanel implements ChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6012301447745950357L;
	
	private Vector<String> vectitel = new Vector<String>();
	private Vector<String> vecdescript = new Vector<String>();
	private Vector<ImageIcon> vecimg = new Vector<ImageIcon>();

	JTabbedPane offenPostenTab = null;
	public JXTitledPanel jxTitel;
	public JTabbedPane jtb;
	public JXHeader jxh;

	OffenepostenPanel oppanel = null;
	OffenepostenMahnungen omahnpanel = null;
	OffenepostenEinstellungen oeinstellungpanel = null;
	
	public OffenepostenTab(){
		super();
		setOpaque(false);
		setLayout(new BorderLayout());
		jtb = new JTabbedPane();
		jtb.setUI(new WindowsTabbedPaneUI());
		
		oppanel = new OffenepostenPanel(this);
		jtb.addTab("Rechnungen ausbuchen", oppanel);

		omahnpanel = new OffenepostenMahnungen(this);
		jtb.addTab("Mahnungen erstellen", omahnpanel);
		
		oeinstellungpanel = new OffenepostenEinstellungen(this);
		jtb.addTab("Einstellungen", oeinstellungpanel);

        jtb.addChangeListener(this);
		doHeader();
        jxh = new JXHeader();
        ((JLabel)jxh.getComponent(1)).setVerticalAlignment(JLabel.NORTH);
        add(jxh, BorderLayout.NORTH);
        add(jtb, BorderLayout.CENTER);

        jxh.validate();
        jtb.validate();
		validate();
		
	}
	
	public void setHeader(int header){
        jxh.setTitle(vectitel.get(header));
        jxh.setDescription(vecdescript.get(header));
        jxh.setIcon(vecimg.get(header));
        jxh.validate();
	}

	
	private void doHeader(){
		ImageIcon ico;
        String ss = System.getProperty("user.dir")+File.separator+"icons"+File.separator+"nebraska_scale.jpg";
        ico = new ImageIcon(ss);
		vectitel.add("Bezahlte Rechnungen ausbuchen / Teilzahlungen buchen");
		vecdescript.add("<html>Hier haben Sie die Möglichkeit Rechnungen nach verschiedenen Kriterien zu suchen.<br>" +
                "Wenn Sie die Rechnung die Sie suchen gefunden haben und die Rechnung <b>vollständig</b> bezahlt wurde,<br>" +
                "genügt es völlig über Alt+A den Vorgang ausbuchen zu aktivieren.<br><br>"+
                "Wurde lediglich eine Teilzahlung geleistet, muß zuvor die noch bestehende Restforderung im Textfeld <b>noch offen</b> eingetragen werden.</html>");
		vecimg.add(ico);
		
		
		vectitel.add("Zertifikate auswerten");
		vecdescript.add("....Hier können Sie die Zertifikatskette einsehen\n" +
                "Neue Zertifikate einlesen (neue Datenannahmestellen)\n" +
                "und schlußendlich überpüfen wie lange welches Zertifikat gültig ist");
		vecimg.add(ico);
		
		vectitel.add("Manuell verschlüsseln");
		vecdescript.add("....Hier können Sie Dateien manuell verschlüsseln\n" +
                "Weshalb auch immer....");
		vecimg.add(ico);

		vectitel.add("Test- und Experimentierpanel");
		vecdescript.add("....Diese Seite ist bislang noch Bodo und Jürgen vorbehalten (leider).\n" +
                "Hier werden die Funktionen die später Nebraska zu dem machen was Nebraske ist\n"+
                "entwickelt und getestet");
		vecimg.add(ico);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		JTabbedPane pane = (JTabbedPane)arg0.getSource();
        int sel = pane.getSelectedIndex();
        jxh.setTitle(vectitel.get(sel));
        jxh.setDescription(vecdescript.get(sel));
        jxh.setIcon(vecimg.get(sel));   
	}
	
	public String getNotBefore(){
		try{
			return DatFunk.sDatInSQL(oeinstellungpanel.tfs[4].getText());
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Fehler beim Bezug des Startdatums, nehme 01.01.1995");
		}
		return "1995-01-01";
	}
	public int getFrist(int frist){
		if(frist == 1){
			try{
				return Integer.parseInt(oeinstellungpanel.tfs[0].getText());
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null,"Fehler beim Bezug der Frist Tage für Mahnstufe 1, nehme 31 Tage");
			}
			return 31;
		}
		if(frist == 2){
			try{
				return Integer.parseInt(oeinstellungpanel.tfs[1].getText());
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null,"Fehler beim Bezug der Frist Tage für Mahnstufe 1, nehme 11 Tage");
			}
			return 11;
		}
		if(frist == 3){
			try{
				return Integer.parseInt(oeinstellungpanel.tfs[2].getText());
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null,"Fehler beim Bezug der Frist Tage für Mahnstufe 3, nehme 11 Tage");
			}
			return 11;
		}
		
		return -1;
	}


}