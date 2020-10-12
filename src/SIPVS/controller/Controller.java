package SIPVS.controller;

import SIPVS.helper.XmlManipulator;
import SIPVS.model.Book;
import SIPVS.model.Borrow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    BorderPane mainBorderPane;

    @FXML
    Button addBorrowButton;

    @FXML
    Button saveXml;

    @FXML
    Button validateXml;
    @FXML
    Button generateHtml;

    @FXML
    DatePicker borrowDate;

    @FXML
    DatePicker returnDate;

    @FXML
    TextField studentName;

    @FXML
    TextField bookName;

    @FXML
    TextField authorName;

    @FXML
    TextField pages;

    @FXML
    TextField printingYear;

    @FXML
    TextField firstPublished;

    @FXML
    TextField isbn;

    @FXML
    TextArea bookDescription;

    @FXML
    TableView table;

    @FXML
    TableColumn nameColumn;
    @FXML
    TableColumn borrowColumn;
    @FXML
    TableColumn returnColumn;

    private ObservableList<Borrow> borrows = FXCollections.observableArrayList( );
    private XmlManipulator xmlManipulator = new XmlManipulator();

    public void initialize(URL arg0, ResourceBundle arg1) {
        Init();
    }

    private void Init() {
        addBorrowButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try{
                    addBorrow();
                }catch (Exception e){
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ERROR");
                    alert.setHeaderText("WRONG VALUE");
                    alert.setContentText("SOME OF THE FIELDS CONTAINS WRONG VALUE");
                    alert.showAndWait();
                }
            }
        });

        saveXml.setOnAction(new EventHandler<ActionEvent>() {

                try{
                    saveXml();
                }catch (Exception e){
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ERROR");
                    alert.setHeaderText("WRONG VALUE");
                    alert.setContentText("SOME OF THE FIELDS ARE EMPTY OR CONTAINS WRONG VALUE");
                    alert.showAndWait();
                }
            }
        });

        validateXml.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                xmlManipulator.validateAgainstXSD();

            }

        });

        generateHtml.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)",
                        "*.html");
                fileChooser.getExtensionFilters().add(extFilter);

                // Show save file dialog
                File file = fileChooser.showSaveDialog(mainBorderPane.getScene().getWindow());

                if (file != null) {
                    xmlManipulator.generateHtmlFile(file.getAbsolutePath());
                }

            }

        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<Borrow,String>("name"));
        borrowColumn.setCellValueFactory(new PropertyValueFactory<Borrow,String>("borrowDate"));
        returnColumn.setCellValueFactory(new PropertyValueFactory<Borrow,String>("returnedDate"));

        table.setItems(borrows);
    }

    private void addBorrow() {
        String name = studentName.getText();
        String borrowDate = this.borrowDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String returnDate = this.returnDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Borrow b=new Borrow(name,borrowDate,returnDate);

        borrows.add(b);
    }

    private void saveXml(){
        String name = bookName.getText();
        String author = authorName.getText();
        Integer pages = Integer.parseInt(this.pages.getText());
        Integer firstPublished = Integer.parseInt(this.firstPublished.getText());
        Integer printed = Integer.parseInt(this.printingYear.getText());
        String isbn = this.isbn.getText();

        Book b= new Book(name,bookDescription.getText(),author,pages,firstPublished,printed,isbn);

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)",
                "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(mainBorderPane.getScene().getWindow());

        if (file != null) {
            xmlManipulator.generateXml(b, borrows, file.getAbsolutePath());
        }
    }

}
