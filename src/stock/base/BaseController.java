package stock.base;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by cbjiang on 2018/10/17.
 */
public class BaseController {

    protected Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage=primaryStage;
    }

    protected Scene scene;

    public void setScene(Scene scene){
        this.scene=scene;
    }

}
