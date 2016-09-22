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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    private String addedRecord = "*";
    private Text tMessageProperty;

    private static int numOfWrittenLines = 0;
    int iHelp = 0;

    @FXML
    private void handlebLoadCSVButton(ActionEvent event) {
        if (!openFileDialog()) {
            new Alert(AlertType.WARNING, "Nepodařilo se načíst vstupní soubor.", ButtonType.OK);
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
        tableView.getItems().clear();
        if (sPathToLoad != null && sPathToSave != null) {
            isirOperator = new ISIROperator(sPathToLoad, sPathToSave, ISIROperator.CSV_SEPARATOR, ISIROperator.MAKE_ALL_CSV);
        } else if (sPathToLoad != null && (sPathToSave == null || sPathToSave == "")) {
            isirOperator = new ISIROperator(sPathToLoad);
            tSavePath2.setText(ISIROperator.getSavePath(sPathToLoad));
        } else if (sPathToLoad == null) {
            if (openFileDialog()) {
                handleRunRequestsButton(new ActionEvent());
            } else {
                bRunRequests.setDisable(false);
            }
        }

        pbRequestsDone.progressProperty().bind(isirOperator.progressProperty());


        isirOperator.messageProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
               
                String[] saUpdatedMessage = newValue.toString().split(";");
                if (saUpdatedMessage.length == 3 
                        && addedRecord != newValue.toString() 
                        && !saUpdatedMessage[2].matches("WS")) {
                    addRecord(saUpdatedMessage[0],saUpdatedMessage[1],saUpdatedMessage[2]);
                    addedRecord = newValue.toString();
                }
                if (newValue == "Done") {
                    tSavePath2.setStyle("-fx-fill: blue;");
                    bRunRequests.setDisable(false);
                    tSavePath2.setOnMouseClicked(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {
                            try {
                                Desktop.getDesktop().open(new File(tSavePath2.getText()));
                            } catch (IOException ex) {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.isPresent() && result.get() == ButtonType.OK) {
                                    handle(event);
                                }
                            }

                        }
                    });
                }
            }

        });
        th = new Thread(isirOperator);
        th.start();

    }


    private void addRecord(String name, String icrc, String found) {
        ObservableList<Record> data = tableView.getItems();
        data.add(new Record(name, icrc, found));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pbRequestsDone.setProgress(0);

    }

    private boolean openFileDialog() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vyberte CSV soubor k načtení.");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fOpen = fc.showOpenDialog(new Stage());
        if (fOpen != null) {
            sPathToLoad = fOpen.getPath();
            tLoadPath.setText(" Vstupní CSV soubor: " + sPathToLoad);
            return true;
        } else {
            return false;
        }

    }

}
