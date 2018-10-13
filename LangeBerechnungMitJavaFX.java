import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * Demo-Programm mit JavaFX (=Nachfolger von Swing).
 * Eine langlaufende Berechnung kann entweder im Main-Thread oder in einem Hintergrund-Thread
 * ausgef�hrt werden.
 * <br><br>
 *
 * F�r Ausf�hrung mit Java7 muss evtl. die Bibliothek "jfxrt" (JavaFX Runtime) 
 * manuell in den Classpath (z.B. als external Library im Eclipse-Projekt)
 * aufgenommen werden. Diese Library befindet sich im Ordner mit der JDK-Installation
 * unter <i>jre\lib\jfxrt.jar</i>.
 * <br><br>
 *
 * API-Doc zu JavaFX: <a href="https://docs.oracle.com/javafx/2/api/index.html">https://docs.oracle.com/javafx/2/api/index.html</a>
 * <br><br>
 * 
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
public class LangeBerechnungMitJavaFX extends Application {

	/** UI-Element zur Anzeige des Berechnungsergebnisses. */
	protected Label _ergLabel = null;
	
	/** UI-Element zur Eingabe der Zahl, f�r die die dritte Potenz berechnet werden soll. */
	protected TextField _textField = null;
	
	/** UI-Element zum Start der Berechnung im Main-Thread. */
	protected Button _button1 = null;
	
	/** UI-Element zum Start der Berechnung in einem Hintergrund-Thread. */
	protected Button _button2 = null;
	
	/** Zeitstempel (zu Beginn der Berechnung mit <i>System.currentTimeMillis()</i> bef�llt) 
	 * f�r Ermittelung der Berechnungsdauer. */
	protected static long sStartTimestamp = -1;
	
	
    /**
	 * Lifecycle-Methode, �berschreiben f�r Aufbau der Oberfl�che.
	 *
	 * @param stage Vergleichbar mit "JFrame"-Instanz bei Swing.
	 */
    @Override
    public void start(final Stage stage) throws Exception {
	
	    // "FlowPane" entspricht in etwa dem Flow-Layout in Swing
		//FlowPane flowPane = new FlowPane();
    	VBox vbox = new VBox(10); // spacing=10
    	vbox.setAlignment(Pos.CENTER);
		
		
    	Font grosserFont = new Font(35);
    	
		// Textfeld f�r Eingabe der Zahl hinzuf�gen
		_textField = new TextField("123");
		_textField.setPromptText("Hier Zahl eingeben"); // wird sichtbar, wenn nichts eingegeben ist.
		_textField.setFont(grosserFont);
		vbox.getChildren().add( _textField );
		
		
		// Button 1 hinzuf�gen
		_button1 = new Button("Berechnen im Main-Thread");
		_button1.setFont(grosserFont);
		//button1.setAlignment(Pos.CENTER);
		_button1.setOnAction(new EventHandler<ActionEvent>() {		
			@Override
			public void handle(ActionEvent evt) {
				starteBerechnungInMainThread();
			}
		});
		vbox.getChildren().add( _button1 );
		
		
		// Button 2 hinzuf�gen
		_button2 = new Button("Berechnen in eigenem Thread");
		_button2.setFont(grosserFont);
		_button2.setOnAction(new EventHandler<ActionEvent>() {		
			@Override
			public void handle(ActionEvent evt) {
				starteBerechnungInHintergrundThread();
			}
		});
		vbox.getChildren().add( _button2 );
		
		
		// Label f�r Anzeige des Ergebnissen hinzuf�gen
		_ergLabel = new Label("<Ergebnis>");
		_ergLabel.setFont(grosserFont);
		_ergLabel.setStyle("-fx-background-color: yellow;");
		vbox.getChildren().add( _ergLabel );
		
		
		// "Szene" entspricht etwa "ContentPane" bei Swing
		Scene scene = new Scene( vbox, 700, 400 ); // Breite=300, H�he=200
		
		// Stage konfigurieren & sichtbar machen
		stage.setScene( scene ); 
		stage.setTitle("Lange Berechnung (JavaFX)" );
		stage.centerOnScreen();
		stage.show();
	}

		
	/**
	 * Event-Handler f�r Button zur Durchf�hrung der Berechnung im Main-Thread.
	 * Das ist schlecht, weil dann w�hrend der Ausf�hrung der Berechnung die 
	 * UI blockiert ist.
	 */
	protected void starteBerechnungInMainThread() {
		
		int eingabeZahl = holeEingabeZahl();
		if (eingabeZahl == Integer.MIN_VALUE) return; 
 							
		zeitmessungStart();
		long ergebnis = berechnung(eingabeZahl);
		String zeitStr = zeitmessungStopp();
		
		_ergLabel.setText("Ergebnis: " + ergebnis + " " + zeitStr);
	}
	
	
	/**
	 * Event-Handler f�r Button zur Durchf�hrung der Berechnung in einem Hintergrund-Thread.
	 */	
	protected void starteBerechnungInHintergrundThread() {
		
		int eingabeZahl = holeEingabeZahl();
		if (eingabeZahl == Integer.MIN_VALUE) return;
		
		_button1.setDisable(true);
		_button2.setDisable(true);
		
		RechenTask task = new RechenTask(eingabeZahl);
        Thread thread = new Thread(task);
        thread.start();
		
		_ergLabel.setText("Berechnung gestartet f�r " + eingabeZahl + " ...");				
	}	
	
	
    /**
	 * Berechnet <i>"inputParameter hoch drei"</i> auf sehr ineffiziente Wiese
	 * (n�mlich mit einer dreifach gestaffelten Schleife).
	 * Je gr��er der Wert <i>inputParameter</i> ist, desto l�nger dauert sie.
	 * Achtung: Laufzeit w�chst kubisch mit <i>inputParameter</i>!
	 * Diese Methode wird so auch in der App "Android_LangeBerechnung" verwendet.
	 * 
	 * @param inputParameter Eingabe-Zahl, f�r die die dritte Potenz berechnet werden soll.
	 */
	public long berechnung(int inputParameter) {
		long result = 0;
		
		for (int i = 0; i < inputParameter; i++)
			for (int j = 0; j < inputParameter; j++)
				for (int k = 0; k < inputParameter; k++) {
					result += 1;
				}
		
		return result;			
	}	
	
	
	/**
	 * Start der "Stoppuhr" f�r die Messung der Berechnungsdauer.
	 */
	protected void zeitmessungStart() {
		sStartTimestamp = System.currentTimeMillis();
	}	
	
	
	/**
	 * Stopp der "Stoppuhr", die mit {@link LangeBerechnungMitJavaFX#zeitmessungStart()} 
	 * gestartet wurde. Gemessene Zeit wird als String zur�ckgeliefert.
	 * 
	 * @return String mit Beschreibung der Zeitdauer in Sekunden, z.B. "Dauer: 5s".
	 */
	protected String zeitmessungStopp() {
		long endeTimestamp = System.currentTimeMillis();
		
		long diffSekunden = (endeTimestamp - sStartTimestamp) / 1000;
		
		return " (Dauer: " + diffSekunden + "s)";
	}	
	
	
	/**
	 * Versucht die in das Eingabefeld eingetragene Zahl auszulesen 
	 * und als int-Wert zur�ckzugeben.
	 * Bei unzul�ssigen Nutzer-Eingaben werden entsprechende
	 * Fehlermeldungen im Ergebnis-Label angezeigt.
	 * 
	 * @return Zahl, von der die dritte Potenz berechnet werden soll;
	 *         gibt <i>Integer.MIN_VALUE</i> zur�ck, wenn keine zul�ssige
	 *         Zahl eingegeben war.         
	 */
	protected int holeEingabeZahl() {
		
		String zahlStr = null;
		
		try {
			
			zahlStr = _textField.getText().trim();
			if (zahlStr.length() == 0) {
				_ergLabel.setText("Bitte eine Zahl eingeben!");
				return Integer.MIN_VALUE;
			}
			
			return Integer.parseInt(zahlStr);		
		}
		catch (Exception ex) {
			System.out.println("Exception beim Versuch die Eingabezahl zu parsen: " + ex);
			_ergLabel.setText("Unzul�ssige Zahl \"" + zahlStr + "\" eingebeben.");
			return Integer.MIN_VALUE;
		}		
	}
	
	
	/**
	 * Eintritts-Methode 
	 *
	 * @param args  Wird nicht ausgewertet
	 */
	public static void main(final String[] args){
	
        launch(args); // Statische Methode aus Oberklasse Application aufrufen
    }
	
	
	/* *************************** */
	/* *** Start innere Klasse *** */
	/* *************************** */
	/**
	 * Task-Klasse zur Auslagerung der langlaufenden Berechnung
	 * in einen Hintergrund-Thread (die Klasse <i>Task</i>
	 * ist spezifisch f�r JavaFX).  
	 * Achtung: Parameter ist Platzhalter-Klasse <i>Void</i>, nicht Schl�sselwort <i>void</i>.
	 */
	public class RechenTask extends Task<Void> {
		
		protected int __inputParameter = -1;
		
		public RechenTask(int inputParameter) {
			__inputParameter = inputParameter;
		}

		/**
		 * Inhalt dieser Methode wird in einem Hintergrund-Thread ausgef�hrt.
		 */
		@Override
		protected Void call() throws Exception {
			
			zeitmessungStart();
			
			long ergebnis = berechnung(__inputParameter);
			
			String zeitStr = zeitmessungStopp();

			final String ergString = "Ergebnis: " + ergebnis + " " + zeitStr;
			
			
			// Ergebnis in Main-Thread darstellen
			Platform.runLater(new Runnable() { // Klasse <i>Platform</i> ist spezifisch f�r JavaFX
                @Override 
                public void run() {
                	_ergLabel.setText(ergString);
                	_button1.setDisable(false);
                	_button2.setDisable(false);
                }
            });
			

			return null;
		}		

	};
	/* *************************** */
	/* *** Ende innere Klasse  *** */
	/* *************************** */	
	
};