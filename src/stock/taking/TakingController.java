package stock.taking;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import stock.base.BaseController;
import stock.utils.ExcelUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by cbjiang on 2018/10/17.
 */
public class TakingController extends BaseController {

    @FXML
    private Button importStocks;

    @FXML
    private Button startTaking;

    @FXML
    private Button importPiece;

    @FXML
    private Button importBox;

    @FXML
    private Button undo;

    @FXML
    private Button saveTaking;

    @FXML
    private Button giveUpTaking;

    @FXML
    private Button exportStocks;

    @FXML
    private TextField perBoxCount;

    @FXML
    private CheckBox useNameSpec;

    @FXML
    private Label totalLabel;

    @FXML
    private TableView<TakingModel> takingTable;
    @FXML
    private TableColumn<TakingModel, String> barColumn;
    @FXML
    private TableColumn<TakingModel, String> codeColumn;
    @FXML
    private TableColumn<TakingModel, String> nameColumn;
    @FXML
    private TableColumn<TakingModel, String> specColumn;
    @FXML
    private TableColumn<TakingModel, String> brandColumn;
    @FXML
    private TableColumn<TakingModel, String> typeColumn;
    @FXML
    private TableColumn<TakingModel, Integer> realityColumn;
    @FXML
    private TableColumn<TakingModel, Integer> lockedColumn;
    @FXML
    private TableColumn<TakingModel, Integer> availableColumn;
    @FXML
    private TableColumn<TakingModel, Integer> wayColumn;
    @FXML
    private TableColumn<TakingModel, Integer> takingColumn;
    @FXML
    private TableColumn<TakingModel, Integer> diffColumn;

    private List<TakingModel> stockData;

    private String TOTAL_LABEL_TEXT_PRE="总条数：";

    @FXML
    private void initialize() {
        startTaking.setDisable(true);
        importPiece.setDisable(true);
        importBox.setDisable(true);
        undo.setDisable(true);
        saveTaking.setDisable(true);
        giveUpTaking.setDisable(true);
        exportStocks.setDisable(true);
    }

    @FXML
    private void handleImportStocks(){
        System.out.println("import stocks");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择Excel文件");
            Stage selectFile = new Stage();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XLS", "*.xls"));
            File file = fileChooser.showOpenDialog(selectFile);
            if (file != null) {
                try {
                    HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
                    JSONArray data = ExcelUtil.importExcel(wb);

                    System.out.println(data);

                    if (TakingUtils.checkHeader(data.getJSONArray(0).getJSONObject(0), TakingUtils.ACTUALITY_TABLE_HEADER)) {
                        String[] keys = new String[]{
                                "0", "1", "2", "3", "5", "6", "7", "8", "9", "10", "$", "$"
                        };
                        data.getJSONArray(0).remove(0);
                        JSONArray dataArr = data.getJSONArray(0);
                        stockData = TakingUtils.jsonArray2List(dataArr, keys);
                        loadData(stockData);
                    } else {
                        throw new Exception("表头不匹配！");
                    }

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            importPiece.setDisable(true);
            importBox.setDisable(true);
            undo.setDisable(true);
            saveTaking.setDisable(true);
            giveUpTaking.setDisable(true);

            importStocks.setDisable(false);
            startTaking.setDisable(false);
            exportStocks.setDisable(false);
        }catch (Exception e){
            e.printStackTrace();
            TakingUtils.alert("错误", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleStartTaking(){
        System.out.println("start taking");

        loadData(new LinkedList<>());

        importPiece.setDisable(false);
        importBox.setDisable(false);
        undo.setDisable(true);
        saveTaking.setDisable(false);
        giveUpTaking.setDisable(false);

        importStocks.setDisable(true);
        startTaking.setDisable(true);
        exportStocks.setDisable(true);
    }

    @FXML
    private void handleImportPiece(){
        System.out.println("import taking piece");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择Excel文件");
            Stage selectFile = new Stage();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XLS", "*.xls"));
            File file = fileChooser.showOpenDialog(selectFile);
            if (file != null) {
                try {
                    HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
                    JSONArray data = ExcelUtil.importExcel(wb);

                    System.out.println(data);

                    String[] keys = new String[]{
                            "$", "0", "1", "2", "$", "$", "$", "$", "$", "$", "3", "$"
                    };
                    data.getJSONArray(0).remove(0);
                    JSONArray dataArr = data.getJSONArray(0);
                    List<TakingModel> takingData = TakingUtils.jsonArray2List(dataArr, keys);

                    List<TakingModel> takingResult = new LinkedList<>();

                    if(!useNameSpec.isSelected()){
                        Set<String> codeConditions = TakingUtils.generateCodeConditions(takingData);
                        if (codeConditions.size() > 0) {
                            stockData.forEach(item -> {
                                for (String condition : codeConditions) {
                                    if (TakingUtils.compareCodeCondition(item.getCode(),condition)) {
                                        TakingModel itemCopy = item.copy();
                                        for (int i = 0; i < takingData.size(); i++) {
                                            if (takingData.get(i).getCode().equals(item.getCode())) {
                                                itemCopy.setTaking(takingData.get(i).getTaking());
                                                takingData.remove(i);
                                                break;
                                            }
                                        }
                                        takingResult.add(itemCopy);
                                    }
                                }
                            });
                        }
                    }else{
                        stockData.forEach(item -> {
                            TakingModel itemCopy = item.copy();
                            for (int i = 0; i < takingData.size(); i++) {
                                if(takingData.get(i).getName()!=null && takingData.get(i).getSpec()!=null){
                                    if (TakingUtils.handleNameOrSpec(item.getName()).contains(TakingUtils.handleNameOrSpec(takingData.get(i).getName())) &&
                                            TakingUtils.handleNameOrSpec(item.getSpec()).contains(TakingUtils.handleNameOrSpec(takingData.get(i).getSpec()))) {
                                        itemCopy.setTaking(takingData.get(i).getTaking());
                                        takingData.remove(i);
                                        takingResult.add(itemCopy);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                    checkRemainTakingData(takingData);
                    loadTakingData(takingResult);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            importPiece.setDisable(false);
            importBox.setDisable(false);
            undo.setDisable(false);
            saveTaking.setDisable(false);
            giveUpTaking.setDisable(false);

            importStocks.setDisable(true);
            startTaking.setDisable(true);
            exportStocks.setDisable(true);
        }catch (Exception e){
            e.printStackTrace();
            TakingUtils.alert("错误", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void checkRemainTakingData(List<TakingModel> takingData){
        if (takingData.size() > 0) {
            Set<String> codes=new HashSet<>();
            for(TakingModel model:takingData){
                codes.add(model.getCode());
            }
            TakingUtils.alert("警告", "盘点表中有" + takingData.size() + "个商品无法与库存表匹配！商品编号如下："+codes.toString(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleImportBox(){
        System.out.println("import taking box");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择Excel文件");
            Stage selectFile = new Stage();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XLS", "*.xls"));
            File file = fileChooser.showOpenDialog(selectFile);
            if (file != null) {
                try {
                    HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
                    JSONArray data = ExcelUtil.importExcel(wb);

                    System.out.println(data);

                    String[] keys = new String[]{
                            "$", "0", "$", "$", "$", "$", "$", "$", "$", "$", "1", "$"
                    };
                    JSONArray dataArr = data.getJSONArray(0);
                    List<TakingModel> takingData = TakingUtils.jsonArray2List(dataArr, keys);

                    Set<String> codeConditions = TakingUtils.generateCodeConditions(takingData);

                    if (codeConditions.size() > 0) {
                        Integer perCount;
                        try {
                            perCount = Integer.parseInt(perBoxCount.getText());
                        } catch (Exception e) {
                            throw new Exception("每箱个数请输入数字!");
                        }

                        List<TakingModel> takingResult = new LinkedList<>();
                        stockData.forEach(item -> {
                            for (String condition : codeConditions) {
                                if (TakingUtils.compareCodeCondition(item.getCode(),condition)) {
                                    TakingModel itemCopy = item.copy();
                                    for (int i = 0; i < takingData.size(); i++) {
                                        if (takingData.get(i).getCode().equals(item.getCode())) {
                                            itemCopy.setTaking(takingData.get(i).getTaking() * perCount);
                                            takingData.remove(i);
                                            break;
                                        }
                                    }
                                    takingResult.add(itemCopy);
                                }
                            }
                        });
                        checkRemainTakingData(takingData);
                        loadTakingData(takingResult);
                    }

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            importPiece.setDisable(false);
            importBox.setDisable(false);
            undo.setDisable(false);
            saveTaking.setDisable(false);
            giveUpTaking.setDisable(false);

            importStocks.setDisable(true);
            startTaking.setDisable(true);
            exportStocks.setDisable(true);
        }catch (Exception e){
            e.printStackTrace();
            TakingUtils.alert("错误", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Stack<List> undoStack = new Stack<>();

    @FXML
    private void handleUndo(){
        if(undoStack.size()>0){
            List<TakingModel> his=undoStack.pop();
            loadData(his);
        }

        if(undoStack.size()==0){
            undo.setDisable(true);
        }
    }

    @FXML
    private void handleSaveTaking(){
        System.out.println("save taking");

        List<TakingModel> takingData = takingTable.getItems();

        stockData.forEach(item -> {
            for(TakingModel model:takingData){
                if(item.getCode().equals(model.getCode())){
                    item.setTaking(model.getTaking());
                    item.setDiff(model.getDiff());
                }
            }
        });

        loadData(stockData);

        importPiece.setDisable(true);
        importBox.setDisable(true);
        undo.setDisable(true);
        saveTaking.setDisable(true);
        giveUpTaking.setDisable(true);

        importStocks.setDisable(false);
        startTaking.setDisable(false);
        exportStocks.setDisable(false);
    }

    @FXML
    private void handleGiveUpTaking(){
        System.out.println("give up taking");

        loadData(stockData);

        importPiece.setDisable(true);
        importBox.setDisable(true);
        undo.setDisable(true);
        saveTaking.setDisable(true);
        giveUpTaking.setDisable(true);

        importStocks.setDisable(false);
        startTaking.setDisable(false);
        exportStocks.setDisable(false);
    }

    @FXML
    private void handleExportStocks(){
        System.out.println("export stocks");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出库存");
        Stage exportFile = new Stage();
        fileChooser.setInitialFileName("库存盘点.xls");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLS Files", "*.xls"));
        File file = fileChooser.showSaveDialog(exportFile);
        if(file != null){
            JSONArray data=new JSONArray();
            for(TakingModel model:takingTable.getItems()){
                if(model.getTaking()!=null){
                    JSONArray rowData=new JSONArray();
                    rowData.add(model.getBar());
                    rowData.add(model.getCode());
                    rowData.add(model.getName());
                    rowData.add(model.getSpec());
                    rowData.add(model.getTaking());
                    rowData.add("");
                    data.add(rowData);
                }
            }
            Boolean result=ExcelUtil.exportExcel(file.getAbsolutePath(),"库存盘点",TakingUtils.TEMP_TABLE_HEADER,data);
            if(result){
                TakingUtils.alert("导出库存","导出成功！", Alert.AlertType.INFORMATION);
            }else{
                TakingUtils.alert("导出库存","导出失败！",Alert.AlertType.ERROR);
            }
        }
    }

    private void pushUndoStack(List<TakingModel> tableData){
        List<TakingModel> result=new LinkedList<>();

        for(TakingModel model:tableData){
            result.add(model.copy());
        }

        undoStack.push(result);
    }

    private void loadTakingData(List<TakingModel> data){
        List<TakingModel> tableData = takingTable.getItems();
        pushUndoStack(takingTable.getItems());
        List<TakingModel> result=new LinkedList<>();

        for(TakingModel model:tableData){
            result.add(model.copy());
        }



        for(TakingModel dataModel:data){
            Boolean doAdd = true;
            for(TakingModel model:result){
                if(model.getCode().equals(dataModel.getCode())){
                    model.setTaking(
                            (model.getTaking()==null?0:model.getTaking())+
                                    (dataModel.getTaking()==null?0:dataModel.getTaking()));
                    doAdd = false;
                    break;
                }
            }
            if(doAdd){
                result.add(dataModel.copy());
            }
        }

        loadData(result);
    }

    private void loadData(List<TakingModel> data){

        ObservableList<TakingModel> takingData = FXCollections.observableArrayList();
        takingData.addAll(data);

        takingTable.setItems(takingData);
        barColumn.setCellValueFactory(cellData -> cellData.getValue().barProperty());
        codeColumn.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        specColumn.setCellValueFactory(cellData -> cellData.getValue().specProperty());
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        realityColumn.setCellValueFactory(cellData -> cellData.getValue().realityProperty());
        lockedColumn.setCellValueFactory(cellData -> cellData.getValue().lockedProperty());
        availableColumn.setCellValueFactory(cellData -> cellData.getValue().availableProperty());
        wayColumn.setCellValueFactory(cellData -> cellData.getValue().wayProperty());
        takingColumn.setCellValueFactory(cellData -> cellData.getValue().takingProperty());
        takingColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        takingColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TakingModel, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<TakingModel, Integer> event) {
                if(!undo.isDisable()){
                    pushUndoStack(takingTable.getItems());
                }
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setTaking(event.getNewValue());
                event.getTableView().refresh();
            }
        });
        diffColumn.setCellValueFactory(cellData -> {
            if(cellData.getValue().getTaking()!=null){
                cellData.getValue().setDiff(cellData.getValue().getTaking()-cellData.getValue().getAvailable());
            }else{
                cellData.getValue().setDiff(null);
            }
            return cellData.getValue().diffProperty();
        });
        Callback diffFactory = new Callback<TableColumn<TakingModel, Integer>, TableCell<TakingModel, Integer>>() {

            @Override
            public TableCell<TakingModel, Integer> call(TableColumn<TakingModel, Integer> param) {
                return new TableCell<TakingModel, Integer>() {

                    private int columnIndex = param.getTableView().getColumns().indexOf(param);

                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.toString());
                        }

                        if(item !=null && item != 0){
                            if(item > 0){
                                this.setStyle("-fx-background-color: green;");
                            }else {
                                this.setStyle("-fx-background-color: red;");
                            }
                        }else{
                            this.setStyle("");
                        }
                    }

                };
            }

        };
        diffColumn.setCellFactory(diffFactory);

        totalLabel.setText(TOTAL_LABEL_TEXT_PRE+takingTable.getItems().size());
    }



}
