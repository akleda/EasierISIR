/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ah.easierisir_1.pkg1fxml;

import easierisir.base.ISIROperator;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Adelka
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Button bLoadCSV;
    @FXML
    private Button bSaveCSV;
    @FXML
    private Button bRunRequests;
    @FXML
    private Text tLoadPath;
    @FXML
    private TextFlow tfSavePath;
    @FXML
    private Text tSavePath1;
    @FXML
    private Text tSavePath2;
    @FXML
    private ProgressBar pbRequestsDone;
    @FXML
    private TableView<Record> tableView;

    private File fOpen, fSave;
    private String sPathToLoad, sPathToSave;
    private ISIROperator isirOperator;
    private Thread th;

    @FXML
    private void handlebLoadCSVButton(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vyberte CSV soubor k načtení.");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fOpen = fc.showOpenDialog(new Stage());
        if (fOpen != null) {
            sPathToLoad = fOpen.getPath();

            tLoadPath.setText(" Vstupní CSV soubor: " + sPathToLoad);
        }
    }

    @FXML
    private void handleSaveCSVButton(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vyberte CSV soubor k uložení.");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fSave = fc.showSaveDialog(new Stage());
        if (fSave != null) {
            sPathToSave = fSave.getPath();
        } else {
            sPathToSave = ISIROperator.getSavePath(sPathToLoad);
        }
        tSavePath2.setText(sPathToSave);
    }

    @FXML
    private void handleRunRequestsButton(ActionEvent event) {
        bRunRequests.setDisable(true);
        tSavePath2.setStyle("-fx-fill: black;");
        if (sPathToLoad != null && sPathToSave != null) {
            isirOperator = new ISIROperator(sPathToLoad, sPathToSave, ISIROperator.CSV_SEPARATOR, ISIROperator.MAKE_ALL_CSV);
        }
        if (sPathToLoad != null && ("".equals(sPathToSave) || sPathToSave == null)) {
            isirOperator = new ISIROperator(sPathToLoad);
            tSavePath2.setText(ISIROperator.getSavePath(sPathToLoad));
        }
        if (sPathToLoad == null){
            handlebLoadCSVButton(new ActionEvent());
            handleRunRequestsButton(new ActionEvent());
        }
        pbRequestsDone.progressProperty().bind(isirOperator.progressProperty());
        th = new Thread(isirOperator);
        th.start();
        pbRequestsDone.progressProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() == 1) {
                    ArrayList<String[]> alFoundRecords = isirOperator.getAlAllFoundSubjects();
                    alFoundRecords.stream().forEach((String[] sf) -> {
                        addRecord(sf[0], sf[1], sf[2]);
                    });
                    th.interrupt();
                    isirOperator.cancel();
                    bRunRequests.setDisable(false);

                    tSavePath2.setOnMouseClicked(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {
                            try {
                                Desktop.getDesktop().open(new File(tSavePath2.getText()));
                            } catch (IOException ex) {
                                Logger.getLogger(EasierISIR_11FXML.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    });
                    tSavePath2.setStyle("-fx-fill: blue;");
                }
            }
        });
    }

    private void addRecord(String name, String icrc, String found) {
        ObservableList<Record> data = tableView.getItems();
        data.add(new Record(name, icrc, found));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pbRequestsDone.setProgress(0);

    }

}
