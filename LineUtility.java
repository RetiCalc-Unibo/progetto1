/*
 * LineUtility.java
 *
 * Classe usata per definire alcuni metodi utili nella selezione delle linee di
 * un file
 *
 */

import java.io.*;

public class LineUtility {

	/**
	 * metodo per recuperare una certa linea di un certo file
	 * @param nomeFile
	 * @param numLinea
	 * @return linea letta o "Linea non trovata..."
	 */
	static String getLine(String nomeFile, int numLinea) throws IOException{
		String linea = null;
		BufferedReader in = null;

	if( numLinea <= 0 ) 
		return linea = "Linea non trovata: numero linea maggiore di 0.";
	// associazione di uno stream di input al file da cui estrarre le linee
	try {
		in = new BufferedReader(new FileReader(nomeFile));
		System.out.println("File aperto: " + nomeFile);
	} catch (FileNotFoundException e) {
		System.out.println("File non trovato: ");
		e.printStackTrace();
		return linea = "File non trovato";
	}
	for (int i = 1; i <= numLinea; i++) {
		linea = in.readLine();
		if (linea == null) {
			linea = "Linea non trovata";
			in.close();
			throw new IOException("Linea non trovata");
		}
	}
	
	System.out.println("Linea selezionata: " + linea);
	
	try {
		in.close();
	}
	catch (IOException e) {
		System.out.println("Errore nella chiusura del reader");
		e.printStackTrace();
	}
    return linea;
  } // getLine
}