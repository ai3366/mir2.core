package com.github.jootnet.imageviewer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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

        initListView(root);

        primaryStage.show();

    }

    public void initListView(Parent root) {
        Pane imagePane = (Pane) root.lookup("#imagePane");

        int x = 0, y = 0;
        for (int i = 1; i <= 50; i++) {
            ImageView imageView = new ImageView();
            imageView.setImage(new Image("http://e.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=0197b59000087bf47db95fedc7e37b1a/38dbb6fd5266d016152614f3952bd40735fa3529.jpg"));
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setX(x);
            imageView.setY(y);
            imagePane.getChildren().add(imageView);
            if (i % 5 == 0) {
                y += 80;
                x = 0;
            } else {
                x += 80;
            }
        }

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
