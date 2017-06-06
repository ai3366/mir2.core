package com.github.jootnet.imageviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by wangertiao on 2017/6/6.
 */
public class Luanch extends Application {

    //region 私有属性
    int width = 800;    //  窗体宽度
    int height = 600;   //  窗体高度
    String title = "ImageViewer - JootMir"; //  窗体标题
    //endregion

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("view/main.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
