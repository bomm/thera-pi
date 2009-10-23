package RehaInternalFrame;

import hauptFenster.AktiveFenster;
import hauptFenster.Reha;
import hauptFenster.SuchenDialog;

import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import patientenFenster.AktuelleRezepte;
import patientenFenster.Dokumentation;
import patientenFenster.Gutachten;
import patientenFenster.Historie;
import patientenFenster.PatGrundPanel;
import patientenFenster.TherapieBerichte;

import events.PatStammEvent;
import events.PatStammEventClass;
import events.RehaEvent;
import events.RehaEventClass;
import events.RehaEventListener;

import terminKalender.TerminFenster;

public class JPatientInternal extends JRehaInternal implements FocusListener, RehaEventListener{
	RehaEventClass rEvent = null;
	public JPatientInternal(String titel, ImageIcon img, int desktop) {
		super(titel, img, desktop);
		rEvent = new RehaEventClass();
		rEvent.addRehaEventListener((RehaEventListener) this);


		//addInternalFrameListener(this);
		// TODO Auto-generated constructor stub
	}
	/*
	@Override
	public void internalFrameActivated(InternalFrameEvent arg0) {
		isActive = true;
		requestFocus();
		toFront();
		super.repaint();
		frameAktivieren(super.getName());
	}
	*/
	@Override
	public void internalFrameClosing(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Internal-Pat-Frame in schliessen***************");
	}
	@Override
	public void internalFrameClosed(InternalFrameEvent arg0) {
		Reha.thisClass.desktops[this.desktop].remove(this);
		AktiveFenster.loescheFenster(this.getName());
		this.removeInternalFrameListener(this);
		Reha.thisFrame.requestFocus();
		System.out.println("L�sche PatientInternal von Desktop-Pane = "+Reha.thisClass.desktops[this.desktop]);
		String s1 = new String("#CLOSING");
		String s2 = "";
		PatStammEvent pEvt = new PatStammEvent(this);
		pEvt.setPatStammEvent("PatSuchen");
		pEvt.setDetails(s1,s2,"") ;
		PatStammEventClass.firePatStammEvent(pEvt);	
		System.out.println("Internal-Pat-Frame in geschlossen***************");
		Reha.thisClass.aktiviereNaechsten(this.desktop);
		rEvent.removeRehaEventListener((RehaEventListener) this);
		PatGrundPanel.thisClass = null;
		Gutachten.gutachten = null;
		Historie.historie = null;
		Dokumentation.doku = null;
		AktuelleRezepte.aktRez = null;
		TherapieBerichte.aktBericht = null;
		
		this.dispose();
		Runtime r = Runtime.getRuntime();
	    r.gc();
	    long freeMem = r.freeMemory();
	    System.out.println("Freier Speicher nach  gc():    " + freeMem);

		
	}
	public void setzeSuche(){
		PatGrundPanel.thisClass.setzeFocus();
	}
	public void setzeTitel(String stitel){
		super.setzeTitel(stitel);
		repaint();
		
	}
	public boolean isActivated(){
		if(! this.isActive){
			return false;
		}else{
			return true;
		}
	}
	public void activateInternal(){
		this.isActive = true;
		this.aktiviereDiesenFrame(this.getName());
		repaint();
	}
	@Override
	public void RehaEventOccurred(RehaEvent evt) {
		if(evt.getRehaEvent().equals("REHAINTERNAL")){
			System.out.println("es ist ein Reha-Internal-Event");
		}
		if(evt.getDetails()[0].equals(this.getName())){
			if(evt.getDetails()[1].equals("#ICONIFIED")){
				try {
					this.setIcon(true);
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.setActive(false);
			}
		}
	}
	
}
