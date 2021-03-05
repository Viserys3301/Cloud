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

public class ClientFX {

    private static ObservableList<Files> files = FXCollections.observableArrayList();

    private   final Socket socket ;
      private final DataInputStream in;
      private  final DataOutputStream out;


    public ClientFX() throws IOException {
        socket = new Socket("localhost", 1235);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }



    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

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
    private Button UplownloadButton;

    @FXML
    private TextField findArea;

    @FXML
    private Button FindFileButton;

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
    //ГОТОВО
    @FXML
    void initialize() {



        OptionsMenuServer.setOnAction(event -> {
            UplownloadButton.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxmlFiles/Server.fxml"));
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

        UplownloadButton.setOnAction(event -> {
            String name = FilesView.getSelectionModel().getSelectedItem().getName();
            JOptionPane.showMessageDialog(null,sendFile(name));
        });

        FindFileButton.setOnAction(event -> {
            updateList();
        });

        DeleteButton.setOnAction(event -> {
            String fileName= FilesView.getSelectionModel().getSelectedItem().getName();
            File file = new File("client/" + fileName);
            file.delete();
            updateList();
        });
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

        File file = new File("client/" + fileName);
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

    public static void initData(Files fil) {
        files.add(fil);
    }


    private String sendFile(String filename) {
        try {
            File file = new File("client" + File.separator + filename);
            if (file.exists()) {
                out.writeUTF("upload");
                out.writeUTF(filename);
                long length = file.length();
                out.writeLong(length);
                FileInputStream fis = new FileInputStream(file);
                int read = 0;
                byte[] buffer = new byte[256];
                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                String status = in.readUTF();
                return status;
            } else {
                return "File is not exists";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }
}
