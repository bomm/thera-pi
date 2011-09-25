package pdftest2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import Tools.DatFunk;
import Tools.SqlInfo;

public class BFAAHBAufnahme {
	String xfdfFile = "";
	HashMap<String,String> hashMap = null;
	String formularpfad = null;
	String patid = null;
	String reader = null;
	public BFAAHBAufnahme(String bid,String pfad,String xpatid){
		formularpfad = pfad;
		patid = xpatid;
		doSuche(bid);
	}
	private void initHashMap(){
		hashMap = new HashMap<String,String>();	
		hashMap.put("VERS_VSNR1_1","");
		hashMap.put("ANSCHRIFT_KRA_KA","");
		hashMap.put("NAME_VORNAME_PAT","");
		hashMap.put("PAT_GEBDAT","");
		hashMap.put("ANSCHRIFT_PAT","");
		hashMap.put("AUFNAHME_DAT",PDFTools.sechserDatum(DatFunk.sHeute()));
		hashMap.put("NAME_AHB_EINRICHTUNG","Reutlinger Therapie- &amp; Analysezentrum GmbH");
		hashMap.put("ORT_DATUM","Reutlingen, den "+DatFunk.sHeute());
		
	}
	private void auswertenVector(Vector<Vector<String>> ergebnis,Vector<Vector<String>> kasse){
		hashMap.put("VERS_VSNR1_1",ergebnis.get(0).get(0));
		if(kasse.size()>0){
			hashMap.put("ANSCHRIFT_KRA_KA",kasse.get(0).get(0)+"\nFAX-# "+kasse.get(0).get(1));
		}
		hashMap.put("NAME_VORNAME_PAT",ergebnis.get(0).get(1));
		if(ergebnis.get(0).get(2).trim().length()==10){
			hashMap.put("PAT_GEBDAT",PDFTools.sechserDatum(ergebnis.get(0).get(2)) );
		}
		hashMap.put("ANSCHRIFT_PAT",ergebnis.get(0).get(3)+", "+ergebnis.get(0).get(4)+" "+ergebnis.get(0).get(5));
	}
	private void doSuche(String bid){
 
		initHashMap();
		Vector<Vector<String>> vec = SqlInfo.holeFelder("select vnummer,namevor,geboren,strasse,plz,ort from bericht2 where berichtid='"+bid+"' LIMIT 1");
		String kassenid = SqlInfo.holePatFeld("kassenid", "pat_intern='"+patid+"'");
		Vector<Vector<String>> vec2 = SqlInfo.holeFelder("select kassen_nam1,fax from kass_adr where id='"+kassenid+"' LIMIT 1");
		if(vec == null){
			return;
		}
		auswertenVector(vec,vec2);
		doStart();
	}
	private void macheKopf(FileWriter fw){
	    try {
			fw.write(
					"<?xml version='1.0' encoding='iso-8859-1'?>"+System.getProperty("line.separator")+
					"<xfdf xmlns='http://ns.adobe.com/xfdf/' xml:space='preserve'>"+System.getProperty("line.separator")+
					"<fields>"+System.getProperty("line.separator")
					);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private void macheFuss(FileWriter fw){
		try {
			fw.write(
					"</fields>"+System.getProperty("line.separator")+
					"<f href='"+formularpfad+"\\BfA-Aufnahmemitteilung_NoRestriction.pdf'/>"+System.getProperty("line.separator")+
					"</xfdf>"
					);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	private void doStart(){
		Set entries = hashMap.entrySet();
	    Iterator it = entries.iterator();
	    FileWriter fw = null;
		try {
			xfdfFile = java.lang.System.getProperty("user.dir")+"/test.xfdf";
			System.out.println(xfdfFile);
			fw = new FileWriter(new File(xfdfFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    macheKopf(fw);
	    String whileBlock = "";
	    while(it.hasNext()){
	    	Map.Entry entry = (Map.Entry) it.next();
	    	whileBlock = "<field name=\"";
	    	whileBlock = whileBlock+entry.getKey().toString()+"\">"+System.getProperty("line.separator");
	    	whileBlock = whileBlock+"<value>";
	    	whileBlock = whileBlock+entry.getValue().toString();
	    	whileBlock = whileBlock+"</value>"+System.getProperty("line.separator");
	    	whileBlock = whileBlock+"</field>"+System.getProperty("line.separator");
	    	try {
				fw.write(whileBlock);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		macheFuss(fw);
		new LadeProg(xfdfFile);
		try {
			System.out.println("PDFLoader wird beendet in HauptProgramm");
			Rahmen.thisClass.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.exit(0);
		
		

	}
}
