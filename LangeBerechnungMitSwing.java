import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;


/**
 * Demo-Programm mit grafischer Oberflaeche (Swing), zeigt Unterschied langlaufender Operation 
 * (Berechnung dritter Potenz mit bewusst sehr ineffizientem Algorithmus) im MainThread 
 * (eigentlich nicht erlaubt) und in einem HintergrundThread ("Worker Thread").
 * <br><br>
 *
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
@SuppressWarnings("serial")
public class LangeBerechnungMitSwing extends JFrame {
        
    /** UI-Element zur Eingabe der Zahl, für die die dritte Potenz berechnet werden soll */
    protected JFormattedTextField _zahlEingabefeld = null;
    
    /** Button zum Start der Berechnung im Main-Thread */
    protected JButton _button1 = null;
    
    /** Button zum Start der Berechnung in einem Background-Thread */
    protected JButton _button2 = null;
    
    /** UI-Element zur Darstellung Ergebnis nach Berechnung */
    protected JLabel _ergebnisLabel = null;
    
    /** Zeitstempel (Beginn der Berechnung mit System.currentTimeMillis() ) für Ermittelung der Berechnungsdauer. */
    protected static long sStartTimestamp = -1;
    
    /** NumberFormat-Objekt, wird fuer Formatierung und Bestimmung zulässiger Eingabezahlen benötigt. */
    protected static NumberFormat sNumberFormat = NumberFormat.getInstance();
    
    
    /**
     * Aufbau der UI mit GridLayout.
     */
    protected LangeBerechnungMitSwing() {
        
       super("Lange Berechnung"); // Titel des Fensters setzen

       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
       // *** Layout-Manager definieren ***
       Container contentPane = getContentPane();
       contentPane.setLayout(new GridLayout(0, 1, 0, 15)); // 0=AnzZeilen(beliebig viele), 1=AnzSpalten, 5=Rand 
                               
        
       // *** UI-Elemente erzeugen ***
       
       // Grosse Schrift, damit am Beamer gut lesbar
       Font bigFont = new Font("SansSerif", Font.BOLD, 35);
 
       _zahlEingabefeld = new JFormattedTextField(sNumberFormat);
       _zahlEingabefeld.setText("123");
       _zahlEingabefeld.setFont(bigFont);
       _zahlEingabefeld.setHorizontalAlignment(SwingConstants.CENTER);
       _zahlEingabefeld.setToolTipText("Hier Zahl eingeben, von der die dritte Potenz berechnet werden soll");
       NumberFormatter formatter = (NumberFormatter) _zahlEingabefeld.getFormatter();
       formatter.setAllowsInvalid(false);
       formatter.setMinimum(0); // keine negativen Zahlen
       formatter.setMaximum(Integer.MAX_VALUE);
       contentPane.add(_zahlEingabefeld);
                
       _button1 = new JButton("Berechnen im Main-Thread");   
       _button1.setFont(bigFont);     
       _button1.addActionListener(new ActionListener() {           
            @Override
            public void actionPerformed(ActionEvent e) {
                starteBerechnungInMainThread();             
            }
       });         
       contentPane.add(_button1);
        
       // Ab Java8 kann der gerade definierte Event-Handler für den Button auch mit einem 
       // Lambda-Ausdruck definiert werden:
       //ActionListener lambda1 = (ActionEvent evt) -> { starteBerechnungInMainThread(); };
       //_button1.addActionListener( lambda1 );        
        
        
       _button2 = new JButton("Berechnen in eigenem Thread");
       _button2.setFont(bigFont);  
       _button2.addActionListener(new ActionListener() {           
            @Override
            public void actionPerformed(ActionEvent e) {
                starteBerechnungInHintergrundThread();              
            }
       });                 
       contentPane.add(_button2);
        
       _ergebnisLabel = new JLabel("<Ergebnis>", SwingConstants.CENTER);
       _ergebnisLabel.setFont(bigFont);  
       _ergebnisLabel.setBackground(Color.YELLOW);
       _ergebnisLabel.setOpaque(true); // damit die Hintergrundfarbe auch sichtbar wird
       contentPane.add(_ergebnisLabel);
        
        
       // *** Größe/Postion des Fenster bestimmen ***
       setSize    (700, 500);
       setLocation(100, 100); // damit Fenster nicht in der Ecke links oben haengt
       //pack(); // alternativ: bevorzugte Groesse einnehmen, ist aber etwas klein
        
       setVisible(true);
    }

    
    /**
     * Abfrage der in das <i>JFormattedTextField</i> eingegeben Zahlen.
     * 
     * @return  Zahl, von der die dritte Potenz berechnet werden soll.
     */
    protected int holeEingabeZahl() {
                
        try {
            String zahlStr = _zahlEingabefeld.getText();
        
            // zahlStr kann Punkte enthalten (z.B. "3.000" fuer Dreitausend), 
            // deshalb koennen wir nicht direkt "Integer.parse(zahlStr)" verwenden.
            Number number = sNumberFormat.parse(zahlStr);
                            
            return number.intValue();
        }
        catch (ParseException ex) {
            ex.printStackTrace();
            return 0;
        }           
    }
    
    
    /**
     * Event-Handler für Button zur Berechnung im Main-Thread.
     */
    protected void starteBerechnungInMainThread() {
                
        //_ergebnisLabel.setText("Berechnung gestartet ..."); // wuerde nicht angezeigt, weil der MainThread vor Ende der Berechnung nicht freigegeben wird
        
        int zahl = holeEingabeZahl();
        
        zeitmessungStart();
        long ergebnis = berechnung(zahl); // funktioniert nicht für negative Zahlen
        String zeitStr = zeitmessungStopp();
                
        String ergZahlStr = sNumberFormat.format(ergebnis); 
        _ergebnisLabel.setText("Ergebnis: " + ergZahlStr + " " + zeitStr);
    }

    
    /**
     * Event-Handler für Button zur Berechnung im Hintergrund-Thread.
     */ 
    protected void starteBerechnungInHintergrundThread() {
        
        _button1.setEnabled(false);
        _button2.setEnabled(false);     
        
        int zahl = holeEingabeZahl();
        
        RechenThread rt = new RechenThread(zahl);
        rt.start();
        
        _ergebnisLabel.setText("Berechnung laeuft ...");
    }
    
        
    /**
     * Berechnet "inputParameter hoch drei" auf sehr ineffiziente Wiese
     * (nämlich mit einer dreifach gestaffelten Schleife).
     * Je größer der Wert <i>inputParameter</i> ist, desto länger dauert sie.
     * Achtung: Laufzeit wächst kubisch mit Wert von <i>inputParameter</i>!
     * Diese Methode wird so auch in der App "Android_LangeBerechnung" verwendet.
     */
    protected long berechnung(int inputParameter) {
        long result = 0;
        
        for (int i = 0; i < inputParameter; i++)
            for (int j = 0; j < inputParameter; j++)
                for (int k = 0; k < inputParameter; k++) {
                    result += 1;
                }
        
        return result;          
    }   
    
    
    /**
     * Start der "Stoppuhr" für die Messung der Berechnungsdauer.
     */
    protected void zeitmessungStart() {
        sStartTimestamp = System.currentTimeMillis();
    }
    
    
    /**
     * Stopp der "Stoppuhr", die mit <i>zeitmessungStart()</i> gestartet wurde.
     * 
     * @return  String mit Beschreibung der Zeitdauer, z.B. "Dauer: 5s".
     */
    protected String zeitmessungStopp() {
        long endeTimestamp = System.currentTimeMillis();
        
        long diffSekunden = (endeTimestamp - sStartTimestamp)/1000;
        
        return " (Dauer: " + diffSekunden + "s)";
    }
    
    
    /**
     * Eintritts-Methode
     * 
     * @param args  Wird nicht ausgewertet
     */
    public static void main(String[] args) {

        new LangeBerechnungMitSwing();
    }

    
    /* *************************** */
    /* *** Start innere Klasse *** */
    /* *************************** */   
    public class RechenThread extends Thread {

        protected int __inputParameter = -1;
        
        public RechenThread(int inputParameter) {
            __inputParameter = inputParameter;
        }
        
        /**
         * Inhalt der Methode "run()" wird in Hintergrund-Thread
         * ausgeführt.
         */
        @Override
        public void run() {
        
            zeitmessungStart();
            
            long ergebnis = berechnung(__inputParameter);
            
            String zeitStr = zeitmessungStopp();
                        
            String ergZahlStr   = sNumberFormat.format(ergebnis); 
            final String ergStr = "Ergebnis: " + ergZahlStr + " " + zeitStr;

            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _button1.setEnabled(true);
                    _button2.setEnabled(true);
                    _ergebnisLabel.setText(ergStr);
                }
            });
        }
                
    };
    /* *************************** */
    /* *** Ende innere Klasse  *** */
    /* *************************** */       
    
};
