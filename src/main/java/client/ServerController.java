package client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.*;

public class ServerController {

    @FXML
    private AnchorPane ChangeDoctorPane;

    @FXML
    private MenuBar MainMenuBar;

    @FXML
    private Menu QeryMenuID;

    @FXML
    private MenuItem AccountMenuItem;

    @FXML
    private Menu OptionsMenuId;

    @FXML
    private MenuItem OptionsMenuServer;

    @FXML
    private MenuItem OptionsMenuClient;

    @FXML
    private Button DeleteButton;

    @FXML
    private TableView<Files> FilesView;

    @FXML
    private TableColumn<Files, String> FilesTableNameColumn;

    @FXML
    private TableColumn<Files, String> FilesTableTypeColumn;

    @FXML
    private TableColumn<Files, String> FilesTableSizeColumn;

    @FXML
    private TableColumn<Files, String> FilesTableDateColumn;

    @FXML
    private TextField findArea;

    @FXML
    private Button FindFileButton;

    @FXML
    private Button DownloadButton;

    @FXML
    private MenuButton selectSort;

    @FXML
    private MenuItem sortNameButton;

    @FXML
    private MenuItem sortTypeButton;

    @FXML
    private MenuItem sortSizeButton;

    @FXML
    private MenuItem sortDateButton;

    private static ObservableList<Files> files = FXCollections.observableArrayList();

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ServerController() throws IOException {
        socket = new Socket("localhost", 1235);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }


    @FXML
    void initialize() {
        OptionsMenuServer.setOnAction(event -> {
            DeleteButton.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxmlFiles/Client.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        });

        DownloadButton.setOnAction(event -> {
            String fileName = FilesView.getSelectionModel().getSelectedItem().getName();
            JOptionPane.showMessageDialog(null, downloadFile(fileName));
        });

        FindFileButton.setOnAction(event -> {
            updateList();
        });

        DeleteButton.setOnAction(event -> {
            String fileName = "server/" + FilesView.getSelectionModel().getSelectedItem().getName();
            JOptionPane.showMessageDialog(null, removeFile(fileName));
            updateList();
        });
    }


    private String downloadFile(String filename) {
        try {
            File file = new File("client/" + File.separator + filename);
            out.writeUTF("download");
            out.writeUTF(filename);
            long size = in.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Done";
    }


    private static String getFileExtension(String mystr) {
        int index = mystr.indexOf('.');
        return index == -1? null : mystr.substring(index);
    }

    private void updateList(){
        for (int i = 0; i <FilesView.getItems().size() ; i++) {
            files.clear();
        }
        String fileName = findArea.getText();

        File file = new File("server/" + fileName);
        File[] arrFiles = file.listFiles();
        List<File> lst = Arrays.asList(arrFiles);

        for (int i = 0; i <arrFiles.length ; i++) {
            initData(new Files(lst.get(i).getName(),getFileExtension(lst.get(i).getName()), lst.get(i).getName().length()/(1024*1024)+" mb","Дата создания"));
        }
        FilesTableNameColumn.setCellValueFactory(new PropertyValueFactory<Files, String>("name"));
        FilesTableTypeColumn.setCellValueFactory(new PropertyValueFactory<Files, String>("type"));
        FilesTableSizeColumn.setCellValueFactory(new PropertyValueFactory<Files, String>("size"));
        FilesTableDateColumn.setCellValueFactory(new PropertyValueFactory<Files, String>("date"));
        FilesView.setItems(files);
    }

    private String removeFile(String filename) {
        try {
                out.writeUTF("remove");
                out.writeUTF(filename);
                out.flush();
                String status = in.readUTF();
                return status;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }


    public static void initData(Files fil) {
        files.add(fil);
    }
}
