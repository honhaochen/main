package seedu.address.ui;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.achievements.ui.AchievementsPage;
import seedu.address.address.logic.AddressBookLogic;
import seedu.address.address.ui.AddressBookPage;
import seedu.address.address.ui.PersonListPanel;
import seedu.address.calendar.ui.CalendarPage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.diaryfeature.ui.DiaryPage;
import seedu.address.financialtracker.ui.FinancialTrackerPage;
import seedu.address.itinerary.ui.ItineraryPage;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

//import seedu.address.address.ui.AddressBookPage;

/**
 * The Main Window. Provides the basic application layout containing a menu bar
 * and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> implements Page {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Scene mainScene;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CodeWindow codeWindow;
    private FinancialTrackerPage financialTrackerPage;
    private CalendarPage calendarPage;
    private ItineraryPage itineraryPage;
    private DiaryPage diaryPage;
    private AchievementsPage achievementsPage;
    private AddressBookPage addressBookPage;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private Scene commonScene;

    @FXML
    private VBox backgroundPlaceholder;

    public MainWindow(Stage primaryStage, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
        codeWindow = new CodeWindow();
        financialTrackerPage = new FinancialTrackerPage();
        calendarPage = new CalendarPage();
        itineraryPage = new ItineraryPage(logic.getItineraryLogic());
        diaryPage = new DiaryPage(logic.getDiaryLogic());
        achievementsPage = new AchievementsPage(primaryStage, logic.getAchievementsLogic());
        addressBookPage = new AddressBookPage(primaryStage, logic.getAddressBookLogic());

        mainScene = primaryStage.getScene();

        PageManager.getInstance(primaryStage, mainScene, calendarPage, itineraryPage,
                financialTrackerPage, diaryPage, achievementsPage, addressBookPage);

        setBackgroundImage();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets background image to make it resizable.
     */
    private void setBackgroundImage() {
        ImageView backgroundImage = new ImageView("/images/mainpage.png");
        backgroundImage.fitHeightProperty().bind(primaryStage.heightProperty().multiply(0.6));
        backgroundImage.fitWidthProperty().bind(primaryStage.widthProperty().multiply(0.9));
        backgroundImage.setPreserveRatio(true);
        backgroundPlaceholder.getChildren().add(backgroundImage);
    }

    /**
     * Sets the accelerator of a MenuItem.
     *
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666 is fixed in later version of
         * SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will not
         * work when the focus is in them because the key event is consumed by the
         * TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is in
         * CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Quit after letting user read the ByeResponse.
     *
     */

    public void exit() {
        TimerTask myDelay = new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
                helpWindow.hide();
                primaryStage.hide();
            }
        };
        Timer timer = new Timer();
        timer.schedule(myDelay, 350);
    }

    /**
     * Opens the code window or focuses on it if it's already opened.
     */
    @FXML
    public void handleCode() {
        if (!codeWindow.isShowing()) {
            codeWindow.show();
        } else {
            codeWindow.focus();
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        exit();
    }

    /**
     * Executes the command and returns the result.
     *
     * @see AddressBookLogic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            if (mainCheck(commandText.split(" ")[0])) {
                CommandResult commandResult = logic.getAddressBookLogic().execute(commandText);
                logger.info("Result: " + commandResult.getFeedbackToUser());
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

                if (commandResult.isShowHelp()) {
                    handleHelp();
                }

                if (commandResult.isExit()) {
                    handleExit();
                }

                return commandResult;
            } else {
                CommandResult commandResult = logic.getAddressBookLogic().execute("Wrong Command");
                logger.info("Result: " + commandResult.getFeedbackToUser());
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

                return commandResult;
            }
        } catch (CommandException | ParseException e) {
            logger.info("Invalid command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    /**
     * Checks whether the input command is valid in the main page.
     * @param command user input command.
     * @return the boolean whether the command is valid in the main page.
     */
    private boolean mainCheck(String command) {
        if (command.equals("goto") || command.equals("exit") || command.equals("help")) {
            return true;
        }

        return false;
    }

    @Override
    public Scene getScene() {
        return mainScene;
    }

    @Override
    public PageType getPageType() {
        return PageType.MAIN;
    }
}
