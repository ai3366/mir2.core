package com.github.jootnet.imageviewer.controller;

import com.github.jootnet.mir2.core.image.ImageLibraries;
import com.github.jootnet.mir2.core.image.ImageLibrary;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by wangertiao on 2017/6/6.
 */
public class MainController {

    @FXML
    BorderPane borderPane;

    @FXML
    ScrollPane scrollPane;

    @FXML
    ImageView imageView;


    @FXML
    MenuItem openMenuItem;

    @FXML
    MenuItem closeMenuItem;

    @FXML
    protected void handlerOpenMenuItemAction(ActionEvent event) {
        FileChooser fileChooser = configureFileChooser();
        File file = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
        if (file != null) {
            ImageLibrary imageLibrary = ImageLibraries.get(file.getName(), file.getPath());
            if (imageLibrary != null) {
                loadTexture(imageLibrary);
            }
        }
    }

    @FXML
    protected void handlerCloseMenuItemAction(ActionEvent event) {
        System.exit(0);
    }


    private FileChooser configureFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开资源文件");
        fileChooser.setInitialDirectory(new File("D:\\Program Files\\盛大游戏\\Legend of mir\\data"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("wzl", "*.wzl"),
                new FileChooser.ExtensionFilter("wil", "*.wil"),
                new FileChooser.ExtensionFilter("wis", "*.wis")
        );
        return fileChooser;
    }


    private void loadTexture(ImageLibrary imageLibrary) {
        Pane imagePane = new Pane();
        int x = 0, y = 0;
        for (int i = 0; i <= 100; i++) {
            ImageView imageView = new ImageView();
            BufferedImage bufferedImage = imageLibrary.tex(i).toBufferedImage(false);
            imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setX(x);
            imageView.setY(y);
            imagePane.getChildren().add(imageView);


            if ((i + 1) % 5 == 0) {
                y += 80;
                x = 0;
            } else {
                x += 80;
            }
        }
        scrollPane.setContent(imagePane);
    }
}
