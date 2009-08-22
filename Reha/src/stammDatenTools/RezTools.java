package stammDatenTools;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JOptionPane;

import patientenFenster.AktuelleRezepte;
import patientenFenster.PatGrundPanel;

import sqlTools.SqlInfo;
import systemEinstellungen.SystemConfig;
import systemTools.StringTools;
import terminKalender.ParameterLaden;
import terminKalender.datFunk;

public class RezTools {
	
	public static Vector<String> holeEinzelTermineAusRezept(String xreznr,String termine){
		Vector<String> xvec = null;
		Vector retvec = new Vector();
		String terms = null;
		if(termine.equals("")){
			xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='"+xreznr+"'", Arrays.asList(new String[] {}));			
			if(xvec.size()==0){
				return (Vector)retvec.clone();
			}else{
				terms = (String) xvec.get(0);	
			}
		}else{
			terms = termine;
		}
		if(terms==null){
			return (Vector)retvec.clone();
		}
		if(terms.equals("")){
			return (Vector)retvec.clone();
		}
		String[] tlines = terms.split("\n");
		int lines = tlines.length;

		for(int i = 0;i<lines;i++){
			String[] terdat = tlines[i].split("@");
			int ieinzel = terdat.length;
			retvec.add(new String((terdat[0].trim().equals("") ? "  .  .    " : terdat[0])));
		}
		Comparator comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
		        String strings1 = datFunk.sDatInSQL(s1);
		        String strings2 = datFunk.sDatInSQL(s2);
		        return strings1.compareTo(strings2);
		    }
		};	
		Collections.sort(retvec,comparator);
		return (Vector)retvec.clone();
	}
	/************************************************************/
/*	
	class ZuzahlModell{
		public int gesamtZahl;
		public boolean allefrei;
		public boolean allezuzahl;
		public boolean anfangfrei;
		public int teil1;
		public int teil2;
		public boolean hausbes;
		public boolean mithausbes;
		public ZuzahlModell(int gesamt,boolean allefrei){
			
		}
	}
*/
	public static int testeRezGebArt(boolean hintergrund,String srez,String termine){
		int iret = 0;
		Vector<String> vAktTermine = null;
		boolean bTermine = false;
		int iTermine = -1;
		boolean bMitJahresWechsel = false;
		ZuzahlModell zm = new ZuzahlModell();
		//Vector<String> patvec = SqlInfo.holeSatz("pat5", "geboren,jahrfrei", "pat_intern='"+xvec.get(1)+"'", Arrays.asList(new String[] {}));
		//String patGeboren = datFunk.sDatInDeutsch(patvec.get(0));
		//String patJahrfrei = datFunk.sDatInDeutsch(patvec.get(0));
		//
		
		
		//1. Schritt haben wir bereits Termineintr�ge die man auswerten kann
		if( (vAktTermine = holeEinzelTermineAusRezept("",termine)).size() > 0 ){
			// Es gibt Termine in der Tabelle
			bTermine = true;
			iTermine = vAktTermine.size();
			if( ((String)vAktTermine.get(0)).substring(6).equals(SystemConfig.vorJahr)){
				bMitJahresWechsel = true;
			}
		}

		System.out.println(vAktTermine);
		for(int i = 0;i < 1;i++){

			if(new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(63))) <= 0){
				// Kasse erfordert keine Zuzahlung
				zm.allefrei = true;
				iret = 0;
				break;
			}
			if(new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(39))) == 1){
				// Hat bereits bezahlt normal behandeln (zzstatus == 1)
				zm.allezuzahl = true;
				iret = 2;
				//break;
			}

			/************************ Jetzt der Ober-Schei�dreck f�r den Achtzehner-Test***********************/
			if((boolean) ((String)PatGrundPanel.thisClass.vecaktrez.get(60)).equals("T")){
				// Es ist ein unter 18 Jahre Test notwendig
				if(bTermine){
					
					int [] test = ZuzahlTools.terminNachAchtzehn(vAktTermine,datFunk.sDatInDeutsch((String)PatGrundPanel.thisClass.patDaten.get(4))); 
					if( test[0] > 0 ){
						//mu� zuzahlen
						zm.allefrei = false;
						if(test[1] > 0){
							zm.allefrei = false;
							zm.allezuzahl = false;
							zm.anfangfrei = true;
							zm.teil1 = test[1];
							System.out.println("Splitten frei f�r "+test[1]+" Tage, bezahlen f�r "+(maxAnzahl()-test[1]));
							iret = 1;
						}else{
							zm.allezuzahl = true;
							zm.teil1 = test[1];
							System.out.println("Jeden Termin bezahlen insgesamt bezahlen f�r "+(maxAnzahl()-test[1]));
							iret = 2;
						}
					}else{
						//Voll befreit
						System.out.println("Frei f�r "+test[1]+" Tage - also alle");
						zm.allefrei = true;
						iret = 0;
					}
				}else{
					//Es stehen keine Termine f�r Analyse zur Verf�gung also mu� das Fenster f�r manuelle Eingabe ge�ffnet werden!!
					String geburtstag = datFunk.sDatInDeutsch(PatGrundPanel.thisClass.patDaten.get(4));
					String stichtag = datFunk.sHeute().substring(0,6)+new Integer(new Integer(SystemConfig.aktJahr)-18).toString();
					if(datFunk.TageDifferenz(geburtstag ,stichtag) >= 0 ){
						System.out.println("Normale Zuzahlung....");
						zm.allefrei = false;
						zm.allezuzahl = true;
						iret = 2;
					}else{
						System.out.println("Alle Frei....");						
						zm.allefrei = true;
						zm.allezuzahl = false;
						iret = 0;
					}
				}
				//iret = 1;
				break;
			}

			/************************ Keine Befreiung Aktuell und keine Vorjahr (Normalfall************************/
			if((boolean) ((String)PatGrundPanel.thisClass.vecaktrez.get(12)).equals("F") && 
					(((String)PatGrundPanel.thisClass.vecaktrez.get(59)).trim().equals("")) ){
				// Es liegt weder eine Befreiung f�r dieses noch f�r letztes Jahr vor.
				// Standard
				iret = 2;
				break;
			}
			/************************ Aktuell Befreit und im Vorjahr auch befreit************************/			
			if((boolean) ((String)PatGrundPanel.thisClass.vecaktrez.get(12)).equals("T") && 
					(((String)PatGrundPanel.thisClass.vecaktrez.get(59)).equals(SystemConfig.vorJahr)) ){
				// Es liegt eine Befreiung vor und im Vorjahr ebenfenfalls befreit
				iret = 0;
				break;
			}
			/************************ aktuell Nicht frei, Vorjahr frei************************/
			if((boolean) ((String)PatGrundPanel.thisClass.vecaktrez.get(12)).equals("F") && 
					(((String)PatGrundPanel.thisClass.vecaktrez.get(59)).trim().equals(SystemConfig.vorJahr)) ){
				if(!bMitJahresWechsel){//Alle Termine aktuell
					iret = 2;
				}else{// es gibt Termine im Vorjahr
					Object[] obj = JahresWechsel(vAktTermine,SystemConfig.vorJahr);
					if(!(Boolean)obj[0]){// alle Termine waren im Vorjahr 
						iret = 0;
					}else{// gemischte Termine
						System.out.println("Termine aus dem Vorjahr(frei) = "+obj[1]+" Termine aus diesem Jahr(Zuzahlung) = "+obj[2]);
						zm.allefrei = false;
						zm.allezuzahl = false;
						zm.anfangfrei = true;
						zm.teil1 = (Integer)obj[1];
						zm.teil2 = (Integer)obj[2];
						iret = 1;
					}
				}
				break;
			}
			/************************Aktuelle Befreiung aber nicht im Vorjahr************************/			
			if((boolean) ((String)PatGrundPanel.thisClass.vecaktrez.get(12)).equals("T") && 
					(((String)PatGrundPanel.thisClass.vecaktrez.get(59)).trim().equals("")) ){
				if(!bMitJahresWechsel){//Alle Termine aktuell
					iret = 0;
				}else{// es gibt Termine im Vorjahr
					Object[] obj = JahresWechsel(vAktTermine,SystemConfig.vorJahr);
					if(!(Boolean)obj[0]){// alle Termine waren im Vorjahr 
						iret = 2;
					}else{// gemischte Termine
						System.out.println("Termine aus dem Vorjahr(Zuzahlung) = "+obj[1]+" Termine aus diesem Jahr(frei) = "+obj[2]);
						zm.allefrei = false;
						zm.allezuzahl = false;
						zm.anfangfrei = false;
						zm.teil1 = (Integer)obj[1];
						zm.teil2 = (Integer)obj[2];
						iret = 3;
					}
				}
				break;
			}
		}
		

		zm.hausbesuch = ((String)PatGrundPanel.thisClass.vecaktrez.get(43)).equals("T");
		zm.hbvoll = ((String)PatGrundPanel.thisClass.vecaktrez.get(61)).equals("T");
		zm.hbheim = ((String)PatGrundPanel.thisClass.patDaten.get(44)).equals("T");
		zm.km = new Integer(StringTools.ZahlTest(((String)PatGrundPanel.thisClass.patDaten.get(48))));
		zm.preisgruppe = new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(41)));
		zm.gesamtZahl = new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(64)));
		//Hausbesuch als logischen wert

		if(iret==0){
			constructGanzFreiRezHMap(zm);
		}
		if(iret==1){
			constructAnfangFreiRezHMap(zm,true);
		}
		if(iret==2){
			constructNormalRezHMap(zm,false);
		}
		if(iret==3){
			constructEndeFreiRezHMap(zm,false);
		}

		System.out.println("ZZ-Variante = "+iret);
		return iret;
	}
	/************
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public static void constructNormalRezHMap(ZuzahlModell zm,boolean unregelmaessig){
		/************************************/
		System.out.println("*****In Normal HMap*********");
		Double rezgeb = new Double(0.000);
		BigDecimal[] preise = {null,null,null,null};
		BigDecimal xrezgeb = BigDecimal.valueOf(new Double(0.000));
		
		//System.out.println("nach nullzuweisung " +xrezgeb.toString());
		int[] anzahl = {0,0,0,0};
		int[] artdbeh = {0,0,0,0};
		int i;
		BigDecimal einzelpreis = null;
		BigDecimal poswert = null;
		BigDecimal rezwert = BigDecimal.valueOf(new Double(0.000));
		SystemConfig.hmAdrRDaten.put("<Rid>",(String)PatGrundPanel.thisClass.vecaktrez.get(35) );
		SystemConfig.hmAdrRDaten.put("<Rnummer>",(String)PatGrundPanel.thisClass.vecaktrez.get(1) );
		SystemConfig.hmAdrRDaten.put("<Rdatum>",(String)PatGrundPanel.thisClass.vecaktrez.get(2) );		
		for(i = 0;i < 4;i++){
			anzahl[i] = new Integer((String)PatGrundPanel.thisClass.vecaktrez.get(i+3));
			artdbeh[i] = new Integer((String)PatGrundPanel.thisClass.vecaktrez.get(i+8));
			preise[i] = BigDecimal.valueOf(new Double((String)PatGrundPanel.thisClass.vecaktrez.get(i+18)));
		}
		xrezgeb.add(BigDecimal.valueOf(new Double(10.00)));
		rezgeb = 10.00;
		//System.out.println("nach 10.00 zuweisung " +rezgeb.toString());		
		String runden;
		DecimalFormat dfx = new DecimalFormat( "0.00" );
		BigDecimal endpos;
		SystemConfig.hmAdrRDaten.put("<Rnummer>",(String)PatGrundPanel.thisClass.vecaktrez.get(1) );
		SystemConfig.hmAdrRDaten.put("<Rpatid>",(String)PatGrundPanel.thisClass.vecaktrez.get(0) );
		SystemConfig.hmAdrRDaten.put("<Rdatum>",datFunk.sDatInDeutsch( (String)PatGrundPanel.thisClass.vecaktrez.get(2) )  );		
		SystemConfig.hmAdrRDaten.put("<Rpauschale>",dfx.format(rezgeb) );
		
		for(i = 0; i < 4; i++){
			/*
			System.out.println(new Integer(anzahl[i]).toString()+" / "+ 
					new Integer(artdbeh[i]).toString()+" / "+
					preise[i].toString() );
			*/		
			if(artdbeh[i] > 0){
				SystemConfig.hmAdrRDaten.put("<Rposition"+(i+1)+">",(String)PatGrundPanel.thisClass.vecaktrez.get(48+i) );
				SystemConfig.hmAdrRDaten.put("<Rpreis"+(i+1)+">", dfx.format(preise[i]) );
				
				einzelpreis = preise[i].divide(BigDecimal.valueOf(new Double(10.000)));

				poswert = preise[i].multiply(BigDecimal.valueOf(new Double(anzahl[i]))); 
				rezwert = rezwert.add(poswert);
				//System.out.println("Einzelpreis "+i+" = "+einzelpreis);
				BigDecimal testpr = einzelpreis.setScale(2, BigDecimal.ROUND_HALF_UP);
				//System.out.println("test->Einzelpreis "+i+" = "+testpr);

				SystemConfig.hmAdrRDaten.put("<Rproz"+(i+1)+">", dfx.format(testpr) );
				SystemConfig.hmAdrRDaten.put("<Ranzahl"+(i+1)+">", new Integer(anzahl[i]).toString() );
				
				endpos = testpr.multiply(BigDecimal.valueOf(new Double(anzahl[i]))); 
				SystemConfig.hmAdrRDaten.put("<Rgesamt"+(i+1)+">", dfx.format(endpos) );
				rezgeb = rezgeb + endpos.doubleValue();
				//System.out.println(rezgeb.toString());

			}else{
				SystemConfig.hmAdrRDaten.put("<Rposition"+(i+1)+">","----");
				SystemConfig.hmAdrRDaten.put("<Rpreis"+(i+1)+">", "0,00" );
				SystemConfig.hmAdrRDaten.put("<Rproz"+(i+1)+">", "0,00");				
				SystemConfig.hmAdrRDaten.put("<Rgesamt"+(i+1)+">", "0,00" );
				SystemConfig.hmAdrRDaten.put("<Ranzahl"+(i+1)+">", "----" );
						
			}
		}
		/*****************************************************/
		if(zm.hausbesuch){ //Hausbesuch
			Object[] obi = hbNormal(zm,rezwert,rezgeb,new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(64))));
			rezwert = ((BigDecimal)obi[0]);
			rezgeb = (Double)obi[1];
		}
		
		
		/*****************************************************/		
		Double drezwert = rezwert.doubleValue();
		SystemConfig.hmAdrRDaten.put("<Rendbetrag>", dfx.format(rezgeb) );
		SystemConfig.hmAdrRDaten.put("<Rwert>", dfx.format(drezwert) );
		DecimalFormat df = new DecimalFormat( "0.00" );
		String s = df.format( rezgeb);
		//System.out.println("----------------------------------------------------");
		//System.out.println("Endg�ltige und geparste Rezeptgeb�hr = "+s+" EUR");
		//System.out.println(SystemConfig.hmAdrRDaten);
		/***********************/
		
		// Hier mu� noch Hausbesuchshandling eingebaut werden
		// Ebenso das Wegegeldhandling
	}
	
	public static void constructGanzFreiRezHMap(ZuzahlModell zm){
		SystemConfig.hmAdrRDaten.put("<Rid>",(String)PatGrundPanel.thisClass.vecaktrez.get(35) );
		SystemConfig.hmAdrRDaten.put("<Rnummer>",(String)PatGrundPanel.thisClass.vecaktrez.get(1) );
		SystemConfig.hmAdrRDaten.put("<Rdatum>",(String)PatGrundPanel.thisClass.vecaktrez.get(2) );		
		SystemConfig.hmAdrRDaten.put("<Rpatid>",(String)PatGrundPanel.thisClass.vecaktrez.get(0) );
		SystemConfig.hmAdrRDaten.put("<Rpauschale>","0,00");
		for(int i = 0;i<5;i++){
			SystemConfig.hmAdrRDaten.put("<Rposition"+(i+1)+">","----");
			SystemConfig.hmAdrRDaten.put("<Rpreis"+(i+1)+">", "0,00" );
			SystemConfig.hmAdrRDaten.put("<Rproz"+(i+1)+">", "0,00");				
			SystemConfig.hmAdrRDaten.put("<Rgesamt"+(i+1)+">", "0,00" );
			SystemConfig.hmAdrRDaten.put("<Ranzahl"+(i+1)+">", "----" );
		}
		SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
		SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
		SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rendbetrag>", "0,00" );
		SystemConfig.hmAdrRDaten.put("<Rwert>", "0,00" );
	}
	public static void constructAnfangFreiRezHMap(ZuzahlModell zm,boolean anfang){
		System.out.println("*****In Anfang-frei*********");
		if(anfang){
			zm.gesamtZahl = new Integer(zm.teil2);
			System.out.println("Restliche Behandlungen berechnen = "+zm.gesamtZahl);
		}else{
			zm.gesamtZahl = new Integer(zm.teil1);
			System.out.println("Beginn der Behandlung berechnen = "+zm.gesamtZahl);
		}

		Double rezgeb = new Double(0.000);
		BigDecimal[] preise = {null,null,null,null};
		BigDecimal xrezgeb = BigDecimal.valueOf(new Double(0.000));
		
		//System.out.println("nach nullzuweisung " +xrezgeb.toString());
		int[] anzahl = {0,0,0,0};
		int[] artdbeh = {0,0,0,0};
		/***************/ //Einbauen f�r Barcode
		int[] gesanzahl = {0,0,0,0};
		int i;
		BigDecimal einzelpreis = null;
		BigDecimal poswert = null;
		BigDecimal rezwert = BigDecimal.valueOf(new Double(0.000));
		SystemConfig.hmAdrRDaten.put("<Rid>",(String)PatGrundPanel.thisClass.vecaktrez.get(35) );
		SystemConfig.hmAdrRDaten.put("<Rnummer>",(String)PatGrundPanel.thisClass.vecaktrez.get(1) );
		SystemConfig.hmAdrRDaten.put("<Rdatum>",(String)PatGrundPanel.thisClass.vecaktrez.get(2) );		
		for(i = 0;i < 4;i++){
			gesanzahl[i] = new Integer((String)PatGrundPanel.thisClass.vecaktrez.get(i+3));
			anzahl[i] = new Integer((String)PatGrundPanel.thisClass.vecaktrez.get(i+3));
			if(! (anzahl[i] < zm.gesamtZahl)){
				anzahl[i] = new Integer(zm.gesamtZahl);
			}
			artdbeh[i] = new Integer((String)PatGrundPanel.thisClass.vecaktrez.get(i+8));
			preise[i] = BigDecimal.valueOf(new Double((String)PatGrundPanel.thisClass.vecaktrez.get(i+18)));
		}
		xrezgeb.add(BigDecimal.valueOf(new Double(10.00)));
		if(anfang){
			rezgeb = 00.00;			
		}else{
			rezgeb = 10.00;
		}
		

		//System.out.println("nach 10.00 zuweisung " +rezgeb.toString());		
		String runden;
		DecimalFormat dfx = new DecimalFormat( "0.00" );
		BigDecimal endpos;
		SystemConfig.hmAdrRDaten.put("<Rnummer>",(String)PatGrundPanel.thisClass.vecaktrez.get(1) );
		SystemConfig.hmAdrRDaten.put("<Rpatid>",(String)PatGrundPanel.thisClass.vecaktrez.get(0) );
		SystemConfig.hmAdrRDaten.put("<Rdatum>",datFunk.sDatInDeutsch( (String)PatGrundPanel.thisClass.vecaktrez.get(2) )  );		
		SystemConfig.hmAdrRDaten.put("<Rpauschale>",dfx.format(rezgeb) );
		
		for(i = 0; i < 4; i++){
			/*
			System.out.println(new Integer(anzahl[i]).toString()+" / "+ 
					new Integer(artdbeh[i]).toString()+" / "+
					preise[i].toString() );
			*/		
			if(artdbeh[i] > 0){
				SystemConfig.hmAdrRDaten.put("<Rposition"+(i+1)+">",(String)PatGrundPanel.thisClass.vecaktrez.get(48+i) );
				SystemConfig.hmAdrRDaten.put("<Rpreis"+(i+1)+">", dfx.format(preise[i]) );
				
				einzelpreis = preise[i].divide(BigDecimal.valueOf(new Double(10.000)));
				//***********vorher nur anzahl[]*****************/
				poswert = preise[i].multiply(BigDecimal.valueOf(new Double(gesanzahl[i]))); 
				rezwert = rezwert.add(poswert);
				//System.out.println("Einzelpreis "+i+" = "+einzelpreis);
				BigDecimal testpr = einzelpreis.setScale(2, BigDecimal.ROUND_HALF_UP);
				//System.out.println("test->Einzelpreis "+i+" = "+testpr);

				SystemConfig.hmAdrRDaten.put("<Rproz"+(i+1)+">", dfx.format(testpr) );
				SystemConfig.hmAdrRDaten.put("<Ranzahl"+(i+1)+">", new Integer(anzahl[i]).toString() );
				
				endpos = testpr.multiply(BigDecimal.valueOf(new Double(anzahl[i]))); 
				SystemConfig.hmAdrRDaten.put("<Rgesamt"+(i+1)+">", dfx.format(endpos) );
				rezgeb = rezgeb + endpos.doubleValue();
				//System.out.println(rezgeb.toString());

			}else{
				SystemConfig.hmAdrRDaten.put("<Rposition"+(i+1)+">","----");
				SystemConfig.hmAdrRDaten.put("<Rpreis"+(i+1)+">", "0,00" );
				SystemConfig.hmAdrRDaten.put("<Rproz"+(i+1)+">", "0,00");				
				SystemConfig.hmAdrRDaten.put("<Rgesamt"+(i+1)+">", "0,00" );
				SystemConfig.hmAdrRDaten.put("<Ranzahl"+(i+1)+">", "----" );
						
			}
		}
		/*****************************************************/
		if(zm.hausbesuch){ //Hausbesuch
			if(zm.gesamtZahl > new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(64)))){
				zm.gesamtZahl = new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(64))); 
			}
			Object[] obi = hbNormal(zm,rezwert,rezgeb,new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(64))));
			rezwert = ((BigDecimal)obi[0]);
			rezgeb = (Double)obi[1];
		}
		/*****************************************************/		
		Double drezwert = rezwert.doubleValue();
		SystemConfig.hmAdrRDaten.put("<Rendbetrag>", dfx.format(rezgeb) );
		SystemConfig.hmAdrRDaten.put("<Rwert>", dfx.format(drezwert) );
		DecimalFormat df = new DecimalFormat( "0.00" );
		String s = df.format( rezgeb);
		System.out.println("----------------------------------------------------");
		System.out.println("Endg�ltige und geparste Rezeptgeb�hr = "+s+" EUR");
		//System.out.println(SystemConfig.hmAdrRDaten);
		/***********************/
	}
		

	public static void constructEndeFreiRezHMap(ZuzahlModell zm,boolean anfang){
		System.out.println("*****�ber Ende Frei*********");
		constructAnfangFreiRezHMap(zm,anfang);
	}	
	public static Vector<Vector<String>>splitteTermine(String terms){
		Vector<Vector<String>> termine = new Vector<Vector<String>>();
		String[] tlines = terms.split("\n");
		int lines = tlines.length;
		//System.out.println("Anzahl Termine = "+lines);
		Vector<String> tvec = new Vector<String>();
		String[] terdat = null;
		for(int i = 0;i<lines;i++){
			terdat = tlines[i].split("@");
			int ieinzel = terdat.length;
			//System.out.println("Anzahl Splits = "+ieinzel);
			tvec.clear();
			for(int y = 0; y < ieinzel;y++){
					tvec.add(new String((terdat[y].trim().equals("") ? "  .  .    " : terdat[y])));
			}
			//System.out.println("Termivector = "+tvec);
			termine.add((Vector<String>)tvec.clone());
		}
		return (Vector<Vector<String>>) termine.clone();
	}
	
	public static Object[] JahrEnthalten(Vector<String>vtage,String jahr){
		Object[] ret = {new Boolean(false),new Integer(-1)};
		for(int i = 0; i < vtage.size();i++){
			if( ((String)vtage.get(i)).equals(jahr) ){
				ret[0] = true;
				ret[1] = new Integer(i);
				break;
			}
		}
		return ret;
	}
	public static Object[] JahresWechsel(Vector<String>vtage,String jahr){
		Object[] ret = {new Boolean(false),new Integer(-1),new Integer(-1)};
		for(int i = 0; i < vtage.size();i++){
			if(!((String)vtage.get(i)).substring(6).equals(jahr) ){
				ret[0] = true;
				ret[1] = new Integer(i);
				ret[2] = maxAnzahl()-(Integer)ret[1];
				break;
			}
		}
		return ret;
	}
	public static int maxAnzahl(){
		int ret = -1;
		int test;
		for(int i = 3; i < 7;i++){
			test = new Integer(((String)PatGrundPanel.thisClass.vecaktrez.get(i)));
			if(test > ret){
				ret = new Integer(test);
			}
		}
		return ret;
	}
	public static String PreisUeberPosition(String position,int preisgruppe,String disziplin,boolean neu ){
		String ret = null;
		Vector preisvec = null;
		if(disziplin.equals("KG")){
			preisvec = ParameterLaden.vKGPreise;			
		}
		if(disziplin.equals("MA")){
			preisvec = ParameterLaden.vMAPreise;
		}
		if(disziplin.equals("ER")){
			preisvec = ParameterLaden.vERPreise;			
		}
		if(disziplin.equals("LO")){
			preisvec = ParameterLaden.vLOPreise;			
		}
		//System.out.println("Beginne Suche nach dem Preis von Position ---> "+position);
		for(int i = 0; i < preisvec.size();i++){
			//System.out.println(""+i+" - "+((String)((Vector)preisvec.get(i)).get( (1+(preisgruppe*4)-3))) );
			if(  ((String)((Vector)preisvec.get(i)).get( (1+(preisgruppe*4)-3))).equals(position) ){
				ret =  ((String)((Vector)preisvec.get(i)).get( ( 1+(preisgruppe*4)-(neu ? 2 : 1))));
				System.out.println("Der Preis von "+position+" = "+ret);
				return ret;
			}
		}
		//System.out.println("Der Preis von "+position+" wurde nicht gefunden!!");
		return ret;
	}
	

public static Object[] hbNormal(ZuzahlModell zm, BigDecimal rezwert,Double rezgeb,int realhbAnz){
	
	//Object[] retobj = {new BigDecimal(new Double(0.00)),(Double)rezgeb};
	//((BigDecimal)retobj[0]).add(BigDecimal.valueOf(new Double(1.00)));
	//((BigDecimal) retobj[0]).add(new BigDecimal(rezwert));
	Object[] retobj = {(BigDecimal) rezwert,(Double)rezgeb};
	System.out.println("Die tats�chlich HB-Anzahl = "+realhbAnz);
	System.out.println("Der Rezeptwert zu Beginn = "+retobj[0]);
	if(zm.hausbesuch){ //Hausbesuch
		System.out.println("Hausbesuch ist angesagt");
		String[] praefix = {"1","2","5","3","MA","KG","ER","LO"};
		String rezid = SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2);
		String zz =  SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(4);
		String kmgeld = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(2);
		String kmpausch = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(3);			
		String hbpos = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(0);
		String hbmit = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(1);
		//f�r jede Disziplin eine anderes praefix
		String ersatz = praefix[Arrays.asList(praefix).indexOf(rezid)-4];

		kmgeld = kmgeld.replaceAll("x",ersatz); 
		kmpausch = kmpausch.replaceAll("x",ersatz); 
		hbpos = hbpos.replaceAll("x",ersatz); 
		hbmit = hbmit.replaceAll("x",ersatz);
		
		String preis = "";
		BigDecimal bdrezgeb;
		BigDecimal bdposwert;
		BigDecimal bdpreis;
		BigDecimal bdendrezgeb;
		BigDecimal testpr;
		SystemConfig.hmAdrRDaten.put("<Rwegkm>",new Integer(zm.km).toString());
		SystemConfig.hmAdrRDaten.put("<Rhbanzahl>",new Integer(zm.gesamtZahl).toString() );
		DecimalFormat dfx = new DecimalFormat( "0.00" );

		if(zm.hbheim){ // und zwar im Heim
			System.out.println("Der HB ist im Heim");
			if(zm.hbvoll){// Volle Ziffer abrechnen?
				System.out.println("Es kann der volle Hausbesuch abgerechnet werden");
				SystemConfig.hmAdrRDaten.put("<Rhbpos>",hbpos);
				preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rhbpos>"),
						zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
				//,"<Rhbpos>","<Rwegpos>","<Rhbpreis>","<Rwegpreis>","<Rhbproz>","<Rwegproz>","<Rhbanzahl>"
				//,"<Rhbgesamt>","<Rweggesamt>","<Rwegkm>"});
				bdpreis = new BigDecimal(new Double(preis));
				//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
				bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
				retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
		
				
				/*******************************/
				if(zz.equals("1")){// Zuzahlungspflichtig
					SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);			
					bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
					testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
					bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
					SystemConfig.hmAdrRDaten.put("<Rhbproz>", dfx.format(testpr.doubleValue()));
					SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", dfx.format(bdendrezgeb.doubleValue()));
					retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
				}else{
					SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
					SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
					SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
					SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
					SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
				}
				/*******************************/
				if(!kmgeld.equals("")){// Wenn Kilometer abgerechnet werden k�nnen
					System.out.println("Es k�nnten Kilometer abgerechnet werden");
					if(zm.km > 0 ){
						System.out.println("Es wurden auch Kilometer angegeben also wird nach km abgerechnet");

						preis = PreisUeberPosition(kmgeld,
								zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
						SystemConfig.hmAdrRDaten.put("<Rwegpos>",""+zm.km+"km*"+preis);
						//SystemConfig.hmAdrRDaten.put("<Rwegreis>",preis );
						/*******************************/
						bdpreis = new BigDecimal(new Double(preis)).multiply(new BigDecimal(new Double(zm.km)));
						//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
						bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
						retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
						SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
						if(zz.equals("1")){// Zuzahlungspflichtig
							bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
							testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
							bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
							SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
							SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
							SystemConfig.hmAdrRDaten.put("<Rweganzahl>",new Integer(zm.gesamtZahl).toString() );
							retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
						}else{
							SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
							SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
							SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
							SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
							SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
						}
						/*******************************/

						//hier zuerst die kilometer ermitteln mal Kilometerpreis = der Endpreis
					}else{// Keine Kilometer angegeben also pauschale verwenden
						System.out.println("Es wurden keine Kilometer angegeben also wird nach Ortspauschale abgerechnet");
						if(!kmpausch.equals("")){//Wenn die Kasse keine Pauschale zur Verf�gung stellt
							System.out.println("Die Kasse stellt eine Wegpauschale zur Verf�gung");
							SystemConfig.hmAdrRDaten.put("<Rwegpos>",kmpausch);
							preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rwegpos>"),
									zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
							//SystemConfig.hmAdrRDaten.put("<Rwegreis>", preis);
							/*******************************/
							bdpreis = new BigDecimal(new Double(preis));
							//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
							bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
							retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
							SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
							if(zz.equals("1")){// Zuzahlungspflichtig
								bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
								testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
								bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
								SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
								SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
								SystemConfig.hmAdrRDaten.put("<Rweganzahl>",new Integer(zm.gesamtZahl).toString() );								
								retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
							}else{
								SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
								SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
								SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
								SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
								SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
								SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
								SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
								SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
								SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
								SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
							}
							/*******************************/
							

						}else{
							JOptionPane.showMessageDialog(null, "Dieser Kostentr�ger kennt keine Weg-Pauschale, geben Sie im Patientenstamm die Anzahl Kilometer an" );
							SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
							SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
							SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
						}
						
					}
				}else{// es k�nnen keine Kilometer abgerechnet werden
					System.out.println("Die Kasse stellt keine Kilometerabrechnung Verf�gung");
					SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");	
					preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rwegpos>"),
							zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
					//SystemConfig.hmAdrRDaten.put("<Rwegpreis>", preis);
					if(preis != null){
						/*******************************/
						bdpreis = new BigDecimal(new Double(preis));
						bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
						//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
						retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
						SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
						if(zz.equals("1")){// Zuzahlungspflichtig
							bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
							testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
							bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
							SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
							SystemConfig.hmAdrRDaten.put("<Rweganzahl>",new Integer(zm.gesamtZahl).toString() );							
							SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
							retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
						}else{
							SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
							SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
							SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
							SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
							SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
							SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
						}
						
					}else{
						SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
						SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
						SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
					}
					

				}
			}else{//nur Mit-Hausbesuch
				System.out.println("Es ist keine volle HB-Ziffer abrechenbar deshalb -> Mithausbesuch");
				SystemConfig.hmAdrRDaten.put("<Rhbpos>",hbmit); 
				preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rhbpos>"),
						zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
				//SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);
				/*******************************/
				if(preis != null){
					bdpreis = new BigDecimal(new Double(preis));
					bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
					//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
					retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
					//SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);
					if(zz.equals("1")){// Zuzahlungspflichtig
						SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);
						bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
						testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
						bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
						SystemConfig.hmAdrRDaten.put("<Rhbproz>", dfx.format(testpr.doubleValue()));
						SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", dfx.format(bdendrezgeb.doubleValue()));
						//SystemConfig.hmAdrRDaten.put("<Rweganzahl>",new Integer(zm.gesamtZahl).toString() );					
						SystemConfig.hmAdrRDaten.put("<Rwegpos>","----" );
						SystemConfig.hmAdrRDaten.put("<Rwpreis>","0,00" );						
						SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----" );
						retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
					}else{
						SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
						SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
						SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
						SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
						SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
					}
				}else{
					SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
					SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
					SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
					SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
					SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
					SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
				}
				/*******************************/
			}
		}else{//nicht im Heim
			System.out.println("Der Hausbesuch ist nicht in einem Heim");
			SystemConfig.hmAdrRDaten.put("<Rhbpos>",hbpos);
			preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rhbpos>"),
					zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
			//SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);
			/*******************************/
			bdpreis = new BigDecimal(new Double(preis));
			bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
			//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
			//System.out.println("Der Positionswert-HB = "+bdposwert);
			retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
			//System.out.println("Der Rezeptwert nach Addition  = "+((BigDecimal)retobj[0]).doubleValue());
			SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);

			bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
			testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
			bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
			SystemConfig.hmAdrRDaten.put("<Rhbproz>", dfx.format(testpr.doubleValue()));
			SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", dfx.format(bdendrezgeb.doubleValue()));
			retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
			/*******************************/
			

			if(!kmgeld.equals("")){// Wenn Kilometer abgerechnet werden k�nnen
				System.out.println("Es k�nnten Kilometer abgerechnet werden");
				if(zm.km > 0 ){
					System.out.println("Es wurden auch Kilometer angegeben also wird nach km abgerechnet");
					
					preis = PreisUeberPosition(kmgeld,
							zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
					SystemConfig.hmAdrRDaten.put("<Rwegpos>",""+zm.km+"km*"+preis );
					//SystemConfig.hmAdrRDaten.put("<Rwegpreis>", preis);
					/*******************************/
					
					bdpreis = new BigDecimal(new Double(preis)).multiply(new BigDecimal(new Double(zm.km)));
					bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
					//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
					retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
					SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));

					bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
					testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
					bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
					SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
					SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
					SystemConfig.hmAdrRDaten.put("<Rweganzahl>",new Integer(zm.gesamtZahl).toString() );
					retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
					/*******************************/



				}else{
					System.out.println("Es wurden keine Kilometer angegeben also wird nach Ortspauschale abgerechnet");
					if(!kmpausch.equals("")){//Wenn die Kasse keine Pauschale zur Verf�gung stellt
						System.out.println("Die Kasse stellt eine Wegpauschale zur Verf�gung");
						SystemConfig.hmAdrRDaten.put("<Rwegpos>",kmpausch);	
						preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rwegpos>"),
								zm.preisgruppe,SystemConfig.hmAdrRDaten.get("<Rnummer>").substring(0,2),true);
						//SystemConfig.hmAdrRDaten.put("<Rwegpreis>",preis );
						/*******************************/
						bdpreis = new BigDecimal(new Double(preis));
						//bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
						bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(realhbAnz)));
						retobj[0] = ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
						SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));

						bdrezgeb = bdpreis.divide(BigDecimal.valueOf(new Double(10.000)));
						testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
						bdendrezgeb = testpr.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
						SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
						SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
						SystemConfig.hmAdrRDaten.put("<Rweganzahl>",new Integer(zm.gesamtZahl).toString() );
						retobj[1] = ((Double)retobj[1]) +bdendrezgeb.doubleValue();
						/*******************************/
					}else{
						JOptionPane.showMessageDialog(null, "Dieser Kostentr�ger kennt keine Weg-Pauschale, geben Sie im Patientenstamm die Anzahl Kilometer an" );
						SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
						SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");						
						SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
						SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");

					}
				}
			}else{// es k�nnen keine Kilometer abgerechnet werden
				SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");		
				SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");				
				SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
				SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
				SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
				

			}
		}
	}else{
		SystemConfig.hmAdrRDaten.put("<Rhbanzahl>","----");
		SystemConfig.hmAdrRDaten.put("<Rhbpos>","----");
		SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rweganzahl>","----");
		SystemConfig.hmAdrRDaten.put("<Rwegpos>","----");
		SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
		SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
	}
	System.out.println("Der Rezeptwert = "+retobj[0]);
	return retobj;
	/*****************************************************/		
	
}

}
class ZuzahlModell{
	public int gesamtZahl;
	public boolean allefrei;
	public boolean allezuzahl;
	public boolean anfangfrei;
	public int teil1;
	public int teil2;
	public int preisgruppe;
	public boolean hausbesuch;
	boolean hbvoll;
	boolean hbheim;
	int km;

	public ZuzahlModell(){
		
	}
}
