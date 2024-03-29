package metuse.ui;

import java.sql.SQLException;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import metuse.dao.SQLUserDao;
import metuse.dao.Database;
import metuse.dao.SQLExpenseDao;
import metuse.dao.SQLIncomeDao;
import metuse.domain.Expense;
import metuse.domain.Income;
import metuse.domain.MetuseService;

public class MetuseUi extends Application {

    private Scene registerScene;
    private Scene loginScene;
    private Scene mainScene;
    private Scene createExpenseScene;
    private Scene createIncomeScene;
    private Label menuLabel = new Label();
    private VBox expenseNodes;
    private VBox incomeNodes;
    private Label expensesSum = new Label();
    private Label incomesSum = new Label();
    private MetuseService metuseService;

    @Override
    public void init() throws Exception {
        Database db = new Database("jdbc:sqlite:database.db");
        SQLUserDao uDao = new SQLUserDao(db);
        SQLExpenseDao eDao = new SQLExpenseDao(db);
        SQLIncomeDao iDao = new SQLIncomeDao(db);
        metuseService = new MetuseService(uDao, eDao, iDao);
    }

    public Node createExpenseNode(Expense expense) {
        HBox box = new HBox(10);
        Label expName  = new Label(expense.getName());
        expName.setMinHeight(28);
        
        Label expAmount  = new Label(Double.toString(expense.getAmount()));
        expAmount.setMinHeight(28);
                
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.setPadding(new Insets(0,5,0,5));
        
        box.getChildren().addAll(expName, spacer, expAmount);
        return box;
    }
    
    public Node createIncomeNode(Income income) {
        HBox box = new HBox(10);
        Label incName  = new Label(income.getName());
        incName.setMinHeight(28);
        
        Label incAmount  = new Label(Double.toString(income.getAmount()));
        incAmount.setMinHeight(28);
                
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.setPadding(new Insets(0,5,0,5));
        
        box.getChildren().addAll(incName, spacer, incAmount);
        return box;
    }
    
    public void expenseList() throws SQLException {
        expenseNodes.getChildren().clear();     

        List<Expense> expenses = metuseService.getExpenses();
        for (Expense e : expenses) {
            expenseNodes.getChildren().add(createExpenseNode(e));
        }    
    }
    
    public void incomeList() throws SQLException {
        incomeNodes.getChildren().clear();     

        List<Income> incomes = metuseService.getIncomes();
        for (Income i : incomes) {
            incomeNodes.getChildren().add(createIncomeNode(i));
        }    
    }
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
        
        //login
        Label loginMessage = new Label();
        Label registerMessage = new Label();
        VBox loginPane = new VBox(20);
        HBox inputPane = new HBox(20);
        loginPane.setPadding(new Insets(20));
        Label loginLabel = new Label("username");
        TextField usernameInput = new TextField();

        inputPane.getChildren().addAll(loginLabel, usernameInput);

        Button loginButton = new Button("login");
        Button createButton = new Button("create new user");
        loginButton.setOnAction(e -> {
            String username = usernameInput.getText();
            menuLabel.setText(username + " logged in");
            if (metuseService.login(username)) {
                try {
                    expenseList();
                    incomeList();
                    expensesSum.setText(Double.toString(metuseService.getExpensesSum()));
                    incomesSum.setText(Double.toString(metuseService.getIncomesSum()));
                } catch (SQLException ex) {}
                loginMessage.setText("");
                primaryStage.setScene(mainScene);
                usernameInput.setText("");
            } else {
                loginMessage.setText("user does not exist");
                loginMessage.setTextFill(Color.RED);
            }
        });

        createButton.setOnAction(e -> {
            usernameInput.setText("");
            loginMessage.setText("");
            registerMessage.setText("");
            primaryStage.setScene(registerScene);
        });

        loginPane.getChildren()
                .addAll(loginMessage, inputPane, loginButton, createButton);

        loginScene = new Scene(loginPane, 500, 350);

        //register
        
        HBox registerButtonPane = new HBox(20);
        registerButtonPane.setPadding(new Insets(20));
        Button registerBackButton = new Button("Back");
        
        
        registerBackButton.setOnAction(e -> {
            primaryStage.setScene(loginScene);
        });
        
        VBox newUserPane = new VBox(20);

        HBox newUsernamePane = new HBox(20);
        newUsernamePane.setPadding(new Insets(20));
        TextField newUsernameInput = new TextField();
        Label newUsernameLabel = new Label("username");
        newUsernameLabel.setPrefWidth(100);
        newUsernamePane.getChildren().addAll(newUsernameLabel, newUsernameInput);

        HBox newNamePane = new HBox(20);
        newNamePane.setPadding(new Insets(20));
        TextField newNameInput = new TextField();
        Label newNameLabel = new Label("name");
        newNameLabel.setPrefWidth(100);
        newNamePane.getChildren().addAll(newNameLabel, newNameInput);

        Button createNewUserButton = new Button("create");
        createNewUserButton.setPrefWidth(100);
        registerButtonPane.getChildren().addAll(createNewUserButton, registerBackButton);

        createNewUserButton.setOnAction(e -> {
            String username = newUsernameInput.getText();
            String name = newNameInput.getText();

            if (username.length() < 3 || name.length() < 3) {
                registerMessage.setText("username or name too short");
                registerMessage.setTextFill(Color.RED);
            } else try {
                if (metuseService.createUser(name, username)) {
                    loginMessage.setText("new user created");
                    loginMessage.setTextFill(Color.GREEN);
                    primaryStage.setScene(loginScene);
                } else {
                    registerMessage.setText("username has to be unique");
                    registerMessage.setTextFill(Color.RED);

                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        });
        newUserPane.getChildren().addAll(registerMessage, newUsernamePane, newNamePane, registerButtonPane);
        registerScene = new Scene(newUserPane, 500, 350);

        //expense
        Label expenseMessage = new Label();
        VBox newExpensePane = new VBox(20);
        
        HBox expenseTitlePane = new HBox(5);
        expenseTitlePane.setPadding(new Insets(0, 0, 0, 200));
        Label expenseTitle = new Label("Expense");
        expenseTitlePane.getChildren().add(expenseTitle);
        
        HBox newExpenseAmountPane = new HBox(20);
        newExpenseAmountPane.setPadding(new Insets(20));
        TextField newExpenseAmountInput = new TextField();
        Label newAmountLabel = new Label("amount");
        newAmountLabel.setPrefWidth(100);
        newExpenseAmountPane.getChildren().addAll(newAmountLabel, newExpenseAmountInput);
        
        HBox newExpenseNamePane = new HBox(20);
        newExpenseNamePane.setPadding(new Insets(20));
        TextField newExpenseNameInput = new TextField();
        Label newExpenseNameLabel = new Label("name");
        newExpenseNameLabel.setPrefWidth(100);
        newExpenseNamePane.getChildren().addAll(newExpenseNameLabel, newExpenseNameInput);
        
        HBox expenseButtonPane = new HBox(20);
        expenseButtonPane.setPadding(new Insets(20));
        Button expenseBackButton = new Button("Back");
        
        
        expenseBackButton.setOnAction(e -> {
            primaryStage.setScene(mainScene);
        });
          
        Button createExpenseButton = new Button("create");
        createExpenseButton.setPrefWidth(100);
        expenseButtonPane.getChildren().addAll(createExpenseButton, expenseBackButton);
        
        createExpenseButton.setOnAction(e -> {
            String amount = newExpenseAmountInput.getText();
            String name = newExpenseNameInput.getText();

            try {
                Double amountD = Double.parseDouble(amount);
                if (name.length() < 3) {
                    expenseMessage.setText("name is too short");
                    expenseMessage.setTextFill(Color.RED);
                } else if (amountD < 0) {
                    expenseMessage.setText("give a positive number");
                    expenseMessage.setTextFill(Color.RED);
                } else try {
                    if (metuseService.createExpense(name, amountD)) {
                        primaryStage.setScene(mainScene);
                        expensesSum.setText(Double.toString(metuseService.getExpensesSum()));
                        expenseList();
                    } else {
                        expenseMessage.setText("failed to create expense");
                        expenseMessage.setTextFill(Color.RED);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex);
                }
            } catch (NumberFormatException en) {
                expenseMessage.setText("amount is not a valid number");
                expenseMessage.setTextFill(Color.RED);
            }
        });
        newExpensePane.getChildren().addAll(expenseMessage, expenseTitlePane, newExpenseNamePane, newExpenseAmountPane, expenseButtonPane);
        createExpenseScene = new Scene(newExpensePane, 500, 350);

        //Income
        VBox newIncomePane = new VBox(20);
        Label incomeMessage = new Label();
        
        HBox incomeTitlePane = new HBox(5);
        incomeTitlePane.setPadding(new Insets(0, 0, 0, 200));
        Label incomeTitle = new Label("Income");
        incomeTitlePane.getChildren().add(incomeTitle);
        
        
        HBox newIncomeAmountPane = new HBox(20);
        newIncomeAmountPane.setPadding(new Insets(20));
        TextField newIncomeAmountInput = new TextField();
        Label newIncomeAmountLabel = new Label("amount");
        newIncomeAmountLabel.setPrefWidth(100);
        newIncomeAmountPane.getChildren().addAll(newIncomeAmountLabel, newIncomeAmountInput);
        
        HBox newIncomeNamePane = new HBox(20);
        newIncomeNamePane.setPadding(new Insets(20));
        TextField newIncomeNameInput = new TextField();
        Label newIncomeNameLabel = new Label("name");
        newIncomeNameLabel.setPrefWidth(100);
        newIncomeNamePane.getChildren().addAll(newIncomeNameLabel, newIncomeNameInput);
        
        HBox incomeButtonPane = new HBox(20);
        incomeButtonPane.setPadding(new Insets(20));
        Button incomeBackButton = new Button("Back");
         
        incomeBackButton.setOnAction(e -> {
            primaryStage.setScene(mainScene);
        });
             
        Button createIncomeButton = new Button("create");
        createIncomeButton.setPrefWidth(100);
        incomeButtonPane.getChildren().addAll(createIncomeButton, incomeBackButton);
        
        createIncomeButton.setOnAction(e -> {
            String amount = newIncomeAmountInput.getText();
            String name = newIncomeNameInput.getText();

            try {
                Double amountD = Double.parseDouble(amount);
                if (name.length() < 3) {
                    incomeMessage.setText("name is too short");
                    incomeMessage.setTextFill(Color.RED);
                } else if (amountD < 0) {
                    incomeMessage.setText("give a positive number");
                    incomeMessage.setTextFill(Color.RED);
                } else try {
                    if (metuseService.createIncome(name, amountD)) {
                        primaryStage.setScene(mainScene);
                        incomesSum.setText(Double.toString(metuseService.getIncomesSum()));
                        incomeList();
                    } else {
                        incomeMessage.setText("failed to create income");
                        incomeMessage.setTextFill(Color.RED);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex);
                }
            } catch (NumberFormatException en) {
                incomeMessage.setText("amount is not a valid number");
                incomeMessage.setTextFill(Color.RED);
            }
        });
        newIncomePane.getChildren().addAll(incomeMessage,incomeTitlePane, newIncomeNamePane, newIncomeAmountPane, incomeButtonPane);
        createIncomeScene = new Scene(newIncomePane, 500, 350);

        //main 
        BorderPane mainPane = new BorderPane();
        mainScene = new Scene(mainPane, 500, 350);

        HBox menuPane = new HBox(20);
        Region menuSpacer = new Region();
        HBox.setHgrow(menuSpacer, Priority.ALWAYS);
        Button logoutButton = new Button("logout");
        Button addExpenseButton = new Button("add expense");
        Button addIncomeButton = new Button("add income");
        menuPane.getChildren().addAll(menuLabel, menuSpacer, addExpenseButton, addIncomeButton, logoutButton);
        logoutButton.setOnAction(e -> {
            metuseService.logout();
            primaryStage.setScene(loginScene);
        });
        addExpenseButton.setOnAction(e -> {
            newExpenseAmountInput.setText("");
            newExpenseNameInput.setText("");
            expenseMessage.setText("");
            primaryStage.setScene(createExpenseScene);
        });
        addIncomeButton.setOnAction(e -> {
            newIncomeAmountInput.setText("");
            newIncomeNameInput.setText("");
            incomeMessage.setText("");
            primaryStage.setScene(createIncomeScene);
        });
        
        GridPane listPane = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        listPane.getColumnConstraints().addAll(col1, col2);
        
        ScrollPane expenses = new ScrollPane();
        expenses.setFitToWidth(true);
        
        ScrollPane incomes = new ScrollPane();
        incomes.setFitToWidth(true);
        
        expenseNodes = new VBox(8);
        expenseList();
        incomeNodes = new VBox(8);
        incomeList();
        
        HBox expSumBox = new HBox(10);
        expensesSum.setMinHeight(28);   
        Label expSumLabel  = new Label("sum");
        expSumLabel.setMinHeight(28);             
        Region expSpacer = new Region();
        HBox.setHgrow(expSpacer, Priority.ALWAYS);
        expSumBox.setPadding(new Insets(0,5,0,5));   
        expSumBox.getChildren().addAll(expSumLabel, expSpacer, expensesSum);
        
        HBox incSumBox = new HBox(10);
        incomesSum.setMinHeight(28);
        Label incSumLabel = new Label("sum");
        Region incSpacer = new Region();
        HBox.setHgrow(incSpacer, Priority.ALWAYS);
        incSumLabel.setMinHeight(28);
        incSumBox.setPadding(new Insets(0,5,0,5));     
        incSumBox.getChildren().addAll(incSumLabel, incSpacer, incomesSum);
        
        listPane.addRow(0, new Label(""), new Label(""));
        listPane.addRow(1, new Label(" expenses "), new Label(" incomes "));
        listPane.addRow(2, expenses, incomes);
        listPane.addRow(3, expSumBox, incSumBox);
        
        expenses.setContent(expenseNodes);
        incomes.setContent(incomeNodes);
        mainPane.setCenter(listPane);
        mainPane.setTop(menuPane);

        //setup primary
        primaryStage.setTitle("MeTuSe");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("closing");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
