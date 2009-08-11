package systemEinstellungen;

import hauptFenster.Reha;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;

import patientenFenster.Dokumentation;
import patientenFenster.MyAccessory;

import systemEinstellungen.SysUtilKrankenkasse.MyVorlagenTableModel;
import systemEinstellungen.SysUtilKrankenkasse.TitelEditor;
import systemEinstellungen.SysUtilPatient.MyDefaultTableModel;
import systemTools.JCompTools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SysUtilFremdprogramme extends JXPanel implements KeyListener, ActionListener {
	
	
	
	JXTable progtab = null;
	MyProgTableModel modprog = new MyProgTableModel();


	JButton[] button = {null,null,null, null,null,null,null, null,null,null,null, null, null};
	JTextField oopfad = null;
	JTextField adobepfad = null;
	JTextField grafpfad = null;
	String lpfad = null;
	
	
	public SysUtilFremdprogramme(){
		
		super(new BorderLayout());
		System.out.println("Aufruf SysUtilFremdprogramme");
		this.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));
		/****/
	     Point2D start = new Point2D.Float(0, 0);
	     Point2D end = new Point2D.Float(400,500);
	     float[] dist = {0.0f, 0.5f};
	     Color[] colors = {Color.WHITE,getBackground()};
	     LinearGradientPaint p =
	         new LinearGradientPaint(start, end, dist, colors);
	     MattePainter mp = new MattePainter(p);
	     setBackgroundPainter(new CompoundPainter(mp));
		/****/
	     add(getVorlagenSeite(),BorderLayout.CENTER);
	     add(getKnopfPanel(),BorderLayout.SOUTH);
	     validate();
	     return;
	}
	
	
		JButton abbruch = null;
		JButton speichern = null;
		
		private JPanel getKnopfPanel(){
			
			
			abbruch = new JButton("abbrechen");
			abbruch.setActionCommand("abbrechen");
			abbruch.addActionListener(this);
			speichern = new JButton("speichern");
			speichern.setActionCommand("speichern");
			speichern.addActionListener(this);
			
										//      1.                      2.    3.    4.     5.     6.    7.      8.     9.
			FormLayout jpanlay = new FormLayout("right:max(150dlu;p), 60dlu, 60dlu, 4dlu, 60dlu",
	       //1.    2. 3.   4.   5.   6.     7.    8. 9.  10.  11. 12. 13.  14.  15. 16.  17. 18.  19.   20.    21.   22.   23.
			"p, 10dlu, p");
			
			PanelBuilder jpan = new PanelBuilder(jpanlay);
			jpan.getPanel().setOpaque(false);		
			CellConstraints jpancc = new CellConstraints();
			
			jpan.addSeparator("", jpancc.xyw(1,1,5));
			jpan.add(abbruch, jpancc.xy(3,3));
			jpan.add(speichern, jpancc.xy(5,3));
			jpan.addLabel("�nderungen �bernehmen?", jpancc.xy(1,3));
			
			jpan.getPanel().validate();
			return jpan.getPanel();
		}
	
	/************** Beginn der Methode f�r die Objekterstellung und -platzierung *********/
	private JPanel getVorlagenSeite(){
		
		
		button[0] = new JButton("entfernen");
		button[0].setActionCommand("entfernen");
		button[0].addActionListener(this);
		button[1] = new JButton("hinzuf�gen");
		button[1].setActionCommand("hinzuf�gen");
		button[1].addActionListener(this);
		button[2] = new JButton("ausw�hlen");
		button[2].setActionCommand("oopfad");
		button[2].addActionListener(this);
		button[3] = new JButton("ausw�hlen");
		button[3].setActionCommand("adobepfad");
		button[3].addActionListener(this);
		button[4] = new JButton("ausw�hlen");
		button[4].setActionCommand("grafpfad");
		button[4].addActionListener(this);
		
		progtab = new JXTable();
		oopfad = new JTextField();
		adobepfad = new JTextField();
		grafpfad = new JTextField();
		
		oopfad.setEditable(false);
		adobepfad.setEditable(false);
		grafpfad.setEditable(false);
		//FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 180dlu, 4dlu, 60dlu",				
        //                                      1.            2.    3.    4.     5.     6.    7.      8.     9.
		FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 160dlu, 4dlu, 60dlu",
       //1.    2.      3.   4.   5. 6.   7.   8. 9.    10.  11. 12. 13.  14.  15. 16.  17. 18.  19.   20.    21.   22.   23.
		"120dlu, 2dlu,p,2dlu, p, 10dlu, p, 10dlu, p, 2dlu, p, 2dlu,p, 10dlu");
		
		PanelBuilder builder = new PanelBuilder(lay);
		builder.setDefaultDialogBorder();
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();
		
		modprog.setColumnIdentifiers(new String[] {"Name d. Programmes","Kompletter Pfad"});
		progtab = new JXTable(modprog);
		//progtab.getColumn(0).setCellEditor(new TitelEditor());
		progtab.setSortable(false);
		JScrollPane jscrProg = JCompTools.getTransparentScrollPane(progtab);
		jscrProg.validate();
		builder.add(jscrProg, cc.xyw(1,1,5));
		
		builder.addLabel("markiertes Programm aus Liste entfernen", cc.xyw(1,3,3, CellConstraints.RIGHT, CellConstraints.BOTTOM));
		builder.add(button[0], cc.xy(5, 3));
		builder.addLabel("Programm zu Liste hinzuf�gen", cc.xyw(1, 5, 3, CellConstraints.RIGHT, CellConstraints.BOTTOM));
		builder.add(button[1], cc.xy(5, 5));
		builder.addSeparator("systemrelevante Programme / Pfade", cc.xyw(1, 7, 5));
		builder.addLabel("Pfad zu OpenOffice", cc.xy(1, 9));
		builder.add(oopfad, cc.xy(3, 9));
		builder.add(button[2], cc.xy(5, 9));
		builder.addLabel("AdobeReader ausw�hlen", cc.xy(1,11));
		builder.add(adobepfad, cc.xy(3, 11));
		builder.add(button[3],cc.xy(5, 11));
		builder.addLabel("Graphikbearbeitung ausw�hlen", cc.xy(1,13));
		builder.add(grafpfad, cc.xy(3, 13));
		builder.add(button[4],cc.xy(5, 13));
		builder.getPanel().validate();
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
		// TODO Auto-generated method stub
		String cmd = e.getActionCommand();
		if(cmd.equals("oopfad")){
        	setCursor(new Cursor(Cursor.WAIT_CURSOR));
			String pfad = progWaehlen(0);
			if(!pfad.equals("")){
				oopfad.setText(pfad);
			}
		}
		if(cmd.equals("adobepfad")){
			String pfad = progWaehlen(1);
			if(!pfad.equals("")){
				adobepfad.setText(pfad);
			}
		}
		if(cmd.equals("grafpfad")){
			String pfad = progWaehlen(1);
			if(!pfad.equals("")){
				grafpfad.setText(pfad);
			}
		}
	}

	public String progWaehlen(int welchesProg){
	String sret = "";	
	JFileChooser chooser = new JFileChooser("Verzeichnis w�hlen");
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    //
    if(lpfad == null){
    	lpfad = Reha.proghome;
    }
   	File file = new File(lpfad);	
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
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    final int result = chooser.showOpenDialog(null);

    if (result == JFileChooser.APPROVE_OPTION) {
        File inputVerzFile = chooser.getSelectedFile();
        String inputVerzStr = inputVerzFile.getPath();
        lpfad = inputVerzStr;

        if(inputVerzFile.getName().trim().equals("")){
        	sret = inputVerzStr;
        }else{
        	sret = inputVerzFile.getAbsolutePath();
        }
    }else{
    	sret = "";
    }
    chooser.setVisible(false); 
    chooser.removeAll();
    chooser = null;
    return sret;
	}
	
	
}
class MyProgTableModel extends DefaultTableModel{
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Class getColumnClass(int columnIndex) {
		 return String.class;
    }
	

 public boolean isCellEditable(int row, int col) {
 	
 	return true;
 }
	   
}
