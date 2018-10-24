package stock.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import stock.Main;
import stock.base.BaseController;
import stock.taking.TakingController;

import java.io.IOException;

/**
 * Created by cbjiang on 2018/10/17.
 */
public class MainController extends BaseController {

    @FXML
    private Button stockTaking;

    public MainController() {
    }

    @FXML
    private void initialize() {

    }

    @FXML
    private void handleStockTaking(){
        System.out.println("show stock-taking view");
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("taking/taking.fxml"));
            Parent root = fxmlLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("库存盘点");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            TakingController controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setScene(scene);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
