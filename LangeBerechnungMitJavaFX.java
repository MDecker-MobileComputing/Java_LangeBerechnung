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
 * ausgeführt werden.
 * <br><br>
 *
 * Für Ausführung mit Java7 muss evtl. die Bibliothek "jfxrt" (JavaFX Runtime)
 * manuell in den Classpath (z.B. als external Library im Eclipse-Projekt)
 * aufgenommen werden. Diese Library befindet sich im Ordner mit der JDK-Installation
 * unter <i>jre/lib/jfxrt.jar</i>.
 * <br><br>
 *
 * Die Kodierung dieser Quellcode-Datei ist UTF-8 (ohne BOM).
 * <br><br>
 *
 *
 * API-Doc zu JavaFX:
 * <a href="https://docs.oracle.com/javafx/2/api/index.html">https://docs.oracle.com/javafx/2/api/index.html</a>
 * <br><br>
 *
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
public class LangeBerechnungMitJavaFX extends Application {

    /** UI-Element zur Anzeige des Berechnungsergebnisses. */
    protected Label _ergLabel = null;

    /** UI-Element zur Eingabe der Zahl, für die die dritte Potenz berechnet werden soll. */
    protected TextField _textField = null;

    /** UI-Element zum Start der Berechnung im Main-Thread. */
    protected Button _button1 = null;

    /** UI-Element zum Start der Berechnung in einem Hintergrund-Thread. */
    protected Button _button2 = null;

    /** Zeitstempel (zu Beginn der Berechnung mit <i>System.currentTimeMillis()</i> befüllt)
     * für Ermittelung der Berechnungsdauer. */
    protected static long sStartTimestamp = -1;


    /**
     * Lifecycle-Methode, überschreiben für Aufbau der Oberfläche.
     *
     * @param stage  Vergleichbar mit "JFrame"-Instanz bei Swing.
     */
    @Override
    public void start(final Stage stage) throws Exception {

        // "FlowPane" entspricht in etwa dem Flow-Layout in Swing
        //FlowPane flowPane = new FlowPane();
        VBox vbox = new VBox(10); // spacing=10
        vbox.setAlignment(Pos.CENTER);


        Font grosserFont = new Font(35);

        // Textfeld für Eingabe der Zahl hinzufügen
        _textField = new TextField("123");
        _textField.setPromptText("Hier Zahl eingeben"); // wird sichtbar, wenn nichts eingegeben ist.
        _textField.setFont(grosserFont);
        vbox.getChildren().add( _textField );


        // Button 1 hinzufügen
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


        // Button 2 hinzufügen
        _button2 = new Button("Berechnen in eigenem Thread");
        _button2.setFont(grosserFont);
        _button2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                starteBerechnungInHintergrundThread();
            }
        });
        vbox.getChildren().add( _button2 );


        // Label für Anzeige des Ergebnissen hinzufügen
        _ergLabel = new Label("<Ergebnis>");
        _ergLabel.setFont(grosserFont);
        _ergLabel.setStyle("-fx-background-color: yellow;");
        vbox.getChildren().add( _ergLabel );


        // "Szene" entspricht etwa "ContentPane" bei Swing
        Scene scene = new Scene( vbox, 700, 400 ); // Breite=300, Höhe=200

        // Stage konfigurieren & sichtbar machen
        stage.setScene( scene );
        stage.setTitle("Lange Berechnung (JavaFX)" );
        stage.centerOnScreen();
        stage.show();
    }


    /**
     * Event-Handler für Button zur Durchführung der Berechnung im Main-Thread.
     * Das ist schlecht, weil dann während der Ausführung der Berechnung die
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
     * Event-Handler für Button zur Durchführung der Berechnung in einem Hintergrund-Thread.
     */
    protected void starteBerechnungInHintergrundThread() {

        int eingabeZahl = holeEingabeZahl();
        if (eingabeZahl == Integer.MIN_VALUE) return;

        _button1.setDisable(true);
        _button2.setDisable(true);

        RechenTask task = new RechenTask(eingabeZahl);
        Thread thread = new Thread(task);
        thread.start();

        _ergLabel.setText("Berechnung gestartet für " + eingabeZahl + " ...");
    }


    /**
     * Berechnet <i>"inputParameter hoch drei"</i> auf sehr ineffiziente Wiese
     * (nämlich mit einer dreifach gestaffelten Schleife).
     * Je größer der Wert <i>inputParameter</i> ist, desto länger dauert sie.
     * Achtung: Laufzeit wächst kubisch mit <i>inputParameter</i>!
     * Diese Methode wird so auch in der App "Android_LangeBerechnung" verwendet.
     *
     * @param inputParameter  Eingabe-Zahl, für die die dritte Potenz berechnet werden soll.
     */
    public long berechnung(int inputParameter) {
        long result = 0;

        for (int i = 0; i < inputParameter; i++) {

            for (int j = 0; j < inputParameter; j++) {

                for (int k = 0; k < inputParameter; k++) {

                    result += 1;
                }
            }
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
     * Stopp der "Stoppuhr", die mit {@link LangeBerechnungMitJavaFX#zeitmessungStart()}
     * gestartet wurde. Gemessene Zeit wird als String zurückgeliefert.
     *
     * @return  String mit Beschreibung der Zeitdauer in Sekunden, z.B. "Dauer: 5s".
     */
    protected String zeitmessungStopp() {

        long endeTimestamp = System.currentTimeMillis();

        long diffSekunden = (endeTimestamp - sStartTimestamp) / 1000;

        return " (Dauer: " + diffSekunden + "s)";
    }


    /**
     * Versucht die in das Eingabefeld eingetragene Zahl auszulesen
     * und als int-Wert zurückzugeben.
     * Bei unzulässigen Nutzer-Eingaben werden entsprechende
     * Fehlermeldungen im Ergebnis-Label angezeigt.
     *
     * @return  Zahl, von der die dritte Potenz berechnet werden soll;
     *          gibt <i>Integer.MIN_VALUE</i> zurück, wenn keine zulässige
     *          Zahl eingegeben war.
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
            _ergLabel.setText("Unzulässige Zahl \"" + zahlStr + "\" eingebeben.");
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
     * ist spezifisch für JavaFX).
     * Achtung: Parameter ist Platzhalter-Klasse <i>Void</i>, nicht Schlüsselwort <i>void</i>.
     */
    public class RechenTask extends Task<Void> {

        /** Input-Zahl, von der die dritte Potenz berechnet werden soll, */
        protected int __inputParameter = -1;

        /** Konstruktor, kopiert <i>inputParameter</i> in eine Member-Variable. */
        public RechenTask(int inputParameter) {

            __inputParameter = inputParameter;
        }

        /**
         * Inhalt dieser Methode wird in einem Hintergrund-Thread ausgeführt.
         */
        @Override
        protected Void call() throws Exception {

            zeitmessungStart();

            long ergebnis = berechnung(__inputParameter);

            String zeitStr = zeitmessungStopp();

            final String ergString = "Ergebnis: " + ergebnis + " " + zeitStr;


            // Ergebnis in Main-Thread darstellen
            Platform.runLater(new Runnable() { // Klasse <i>Platform</i> ist spezifisch für JavaFX
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