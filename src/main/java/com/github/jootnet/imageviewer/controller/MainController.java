package com.github.jootnet.imageviewer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Created by wangertiao on 2017/6/6.
 */
public class MainController {

    @FXML
    BorderPane borderPane;

    @FXML
    MenuItem openMenuItem;

    @FXML
    MenuItem closeMenuItem;

    @FXML
    protected void handlerOpenMenuItemAction(ActionEvent event) {
        FileChooser fileChooser = configureFileChooser();
        File file = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
        if (file != null) {

        }
    }

    @FXML
    protected void handlerCloseMenuItemAction(ActionEvent event) {
        System.exit(0);
    }


    private FileChooser configureFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开资源文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("wil", "*.wil"),
                new FileChooser.ExtensionFilter("wis", "*.wis"),
                new FileChooser.ExtensionFilter("wzl", "*.wzl")
        );
        return fileChooser;
    }

}
