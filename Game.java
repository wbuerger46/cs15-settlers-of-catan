package myindy.settlersOfCatan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * This is the Game class! This class contains all the logic for how to facilitate the actual gameplay: mainly
 * rotating through the players and giving the players options of what actions to take. At the initialization of the
 * game, we set up organization of the whole screen, creating the board and the main aspects of the SettingsPane. Once
 * gameplay actually begins, the SettingsPane is completed with the action buttons, and this class manages rotation
 * between each player's turn. The action buttons are toggled (enabled/disabled) based on what actions the player
 * is actually able to take, and when they choose to roll the dice, this class makes sure resources are distributed
 * to players as necessary based on the randomly-generated dice roll. Much of this class deals with calling other
 * classes (namely the board and the players) and telling them how to respond to the gameplay OR it deals with
 * responding after other classes (namely the board and the players) tell it that an action has been taken.
 */
public class Game {

    private Pane _gamePane;
    private BorderPane _settingsPane;
    private VBox _buttons;
    private Board _board;
    private ArrayDeque<Player> _players;
    private HashMap<Integer, Tile[]> _diceRollToTiles;
    private ArrayList<String> _devCards;
    private Player _currPlayer;
    //All of the following are graphical elements that are updated throughout the game
    private Label _instructions;
    private Label _diceRoll;
    private ImageView _dieA;
    private ImageView _dieB;
    private Button _buyRoad;
    private Button _buySettlement;
    private Button _buyCity;
    private Button _rollDice;
    private Button _nextPlayer;
    private Button _buyDevCard;
    private Button _useKnight;
    private VBox _notificationPane;
    private Label _notificationLabel;

    /**
     * The Constructor for this class creates the GamePane, sets up the ArrayDeque with the order of the Players,
     * initializes the board, makes the deck of development cards, and calls methods to finish the graphical setup.
     * It accepts parameter of how many players are used in this game so it knows how many players to add to the
     * ArrayDeque.
     */
    public Game(int numPlayers) {
        _gamePane = new Pane();
        _gamePane.setPrefSize(Constants.GAME_PANE_WIDTH, Constants.SCENE_HEIGHT);
        _players = new ArrayDeque<Player>();
        _players.addLast(new Player(Color.BLUE));
        _players.addLast(new Player(Color.WHITE));
        _players.addLast(new Player(Color.ORANGE));
        if (numPlayers == 4) {
            _players.addLast(new Player(Color.RED));
        }
        _instructions = new Label();
        _board = new Board(this);
        _diceRollToTiles = _board.getDiceRollToTiles();
        this.makeDevCards();
        this.makeSettingsPane();
        this.makeNotificationPane();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                    **THESE METHODS ARE ONLY USED IN THE INITIALIZATION OF THE GAME AND OF GAMEPLAY**              //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method sets up the SettingsPane, which (from top to bottom) contains the Settlers of Catan logo,
     * the ButtonPane (with Action buttons), and the Player Panes.
     */
    private void makeSettingsPane() {
        _settingsPane = new BorderPane();
        //Adds fancy textured background
        _settingsPane.setBackground(new Background(new BackgroundImage(
                new Image(this.getClass().getResourceAsStream("BackgroundTexture.png")), BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        _settingsPane.setPrefWidth(Constants.SCENE_WIDTH - Constants.GAME_PANE_WIDTH);
        ImageView logo = new ImageView(new Image(this.getClass().getResourceAsStream("CatanLogo")));
        logo.setPreserveRatio(true);
        logo.setFitWidth(250);
        BorderPane.setAlignment(logo, Pos.CENTER);
        _settingsPane.setTop(logo);
        _buttons = new VBox();
        _buttons.setAlignment(Pos.CENTER);
        _buttons.setSpacing(10);
        _instructions.setFont(new Font(20));
        _instructions.setWrapText(true);
        _instructions.setTextAlignment(TextAlignment.CENTER);
        _buttons.getChildren().add(_instructions);
        _settingsPane.setCenter(_buttons);
        _settingsPane.setBottom(this.addPlayerPanes());
    }

    /**
     * This method creates the Player Pane (an HBox), which contains two VBoxes, which each contain one or two
     * separate Player Panes from each player.
     */
    private HBox addPlayerPanes() {
        HBox playerPane = new HBox();
        VBox firstTwoPlayers = new VBox();
        VBox nextPlayers = new VBox();
        int count = 0;
        for (Player player : _players) {
            if (count < 2) {
                firstTwoPlayers.getChildren().add(player.getPlayerPane());
            } else {
                nextPlayers.getChildren().add(player.getPlayerPane());
            }
            count++;
        }
        firstTwoPlayers.setSpacing(5);
        nextPlayers.setSpacing(5);
        nextPlayers.setAlignment(Pos.CENTER);
        playerPane.getChildren().addAll(firstTwoPlayers, nextPlayers);
        playerPane.setSpacing(5);
        return playerPane;
    }

    /**
     * This method makes the list of Development Cards (14 knights and 5 victory points), randomizes the list,
     * and converts it to an ArrayList.
     */
    private void makeDevCards() {
        List<String> devCards = Arrays.asList("Knight", "Knight", "Knight", "Knight", "Knight", "Knight", "Knight",
                "Knight", "Knight", "Knight", "Knight", "Knight", "Knight", "Knight", "Victory Point", "Victory Point",
                "Victory Point", "Victory Point", "Victory Point");
        Collections.shuffle(devCards);
        _devCards = new ArrayList<String>(devCards);
    }

    /**
     * This method uses the ArrayDeque _players to make the order in which players will place settlements/roads and
     * choose initial resources in pre-gameplay setup. It takes a Boolean parameter to indicate whether it should go
     * twice through the order (placing settlements/roads) or just once (choosing resources). If false, he order
     * should go once through the ArrayDeque, then return backwards (e.g. A-B-C-D-D-C-B-A). If true, it just goes once
     * through the order (e.g. A-B-C-D). This method is called three times in the Board class; the first two times it
     * will return the same Stack, and the third time it returns half that stack.
     */
    public Stack<Player> setupOrder(Boolean halfOrder) {
        Stack<Player> setupOrder = new Stack<Player>();
        if (!halfOrder) {
            for (Player player : _players) {
                setupOrder.push(player);
            }
        }
        for (int i = 0; i < _players.size(); i++) {
            Player player = _players.removeLast();
            setupOrder.push(player);
            _players.addFirst(player);
        }
        return setupOrder;
    }

    /**
     * This method is called once all of the initial settlements and roads have been placed. It finishes setting up
     * the Settings Pane by making all the action buttons, finds the first Player, and toggles the buttons as
     * necessary.
     */
    public void startGameplay() {
        this.makeButtons();
        _currPlayer = _players.peekFirst();
        _instructions.setText(_currPlayer.getName() + "'s Turn");
        this.allButtonsOff();
        _rollDice.setDisable(false);
    }

    /**
     * This method is called when the Gameplay starts, and it creates all the buttons! So that the buttons aren't
     * all vertically aligned into a huge stack, I have them separated into HBoxes with 2 buttons each. Those HBoxes
     * are put into the overall VBox. It also makes the dicePane, which contains two images of die and a Label that
     * displays the total of the dice.
     */
    private void makeButtons() {
        _buyRoad = new Button("Buy a Road");
        _buyRoad.setOnAction(new ButtonHandler("BuyRoad"));
        _buySettlement = new Button("Buy a Settlement");
        _buySettlement.setOnAction(new ButtonHandler("BuySettlement"));
        _buyCity = new Button("Buy a City");
        _buyCity.setOnAction(new ButtonHandler("BuyCity"));
        _buyDevCard = new Button("Buy Development Card");
        _buyDevCard.setOnAction(new ButtonHandler("BuyDevCard"));
        _useKnight = new Button("Use Knight");
        _useKnight.setOnAction(new ButtonHandler("UseKnight"));
        _nextPlayer = new Button("Next Player");
        _nextPlayer.setOnAction(new ButtonHandler("NextPlayer"));
        _rollDice = new Button("Roll Dice");
        _rollDice.setOnAction(new ButtonHandler("RollDice"));
        _dieA = new ImageView(new Image(this.getClass().getResourceAsStream("Dice1.png")));
        _dieA.setFitHeight(40);
        _dieA.setFitWidth(40);
        _dieB = new ImageView(new Image(this.getClass().getResourceAsStream("Dice1.png")));
        _dieB.setFitHeight(40);
        _dieB.setFitWidth(40);
        _diceRoll = new Label("Total: 2");
        HBox dicePane = new HBox();
        dicePane.setAlignment(Pos.CENTER);
        dicePane.setSpacing(15);
        dicePane.getChildren().addAll(_dieA, _dieB, _diceRoll);
        HBox box1 = new HBox(_buyRoad, _buySettlement);
        box1.setAlignment(Pos.CENTER);
        box1.setSpacing(10);
        HBox box2 = new HBox(_buyCity, _buyDevCard);
        box2.setAlignment(Pos.CENTER);
        box2.setSpacing(10);
        HBox box3 = new HBox(_useKnight, _nextPlayer);
        box3.setAlignment(Pos.CENTER);
        box3.setSpacing(10);
        _buttons.getChildren().addAll(box1, box2, box3, _rollDice, dicePane);
    }

    /**
     * This method creates the "Notification Pane" that is used throughout the game to convey information
     * about gameplay to the players. It includes a _notificationLabel, which is changed each time. That way,
     * this Pane just has to be added and removed from the overall gamePane.
     */
    private void makeNotificationPane() {
        _notificationPane = new VBox();
        _notificationPane.setPrefSize(500, 300);
        _notificationPane.setLayoutX(Constants.NOTIFICATION_PANE_X);
        _notificationPane.setLayoutY(Constants.NOTIFICATION_PANE_Y);
        _notificationPane.setStyle("-fx-background-color: White");
        _notificationPane.setOpacity(0.9);
        _notificationLabel = new Label();
        _notificationLabel.setFont(new Font(24));
        _notificationLabel.setWrapText(true);
        _notificationLabel.setTextAlignment(TextAlignment.CENTER);
        _notificationPane.setAlignment(Pos.CENTER);
        _notificationPane.getChildren().add(_notificationLabel);
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                                  **THESE METHODS ARE USED TO FACILITATE GAMEPLAY**                                //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method rotates through the ArrayDeque _players whenever it is the next player's turn. I use peekFirst()
     * to get the current player instead of pop() so that the ArrayDeque will always reflect having 4 players in
     * the game. Lastly, it toggles the buttons since the Player must roll the dice first.
     */
    private void nextTurn() {
        _players.addLast(_players.pop());
        _currPlayer = _players.peekFirst();
        this.allButtonsOff();
        _rollDice.setDisable(false);
        _instructions.setText(_currPlayer.getName() + "'s Turn");
    }

    /**
     * This method is called when the "Roll Dice" button is pushed! It generates two random values between 1-6 and
     * sets the dice to reflect that value. Then it distributes the proper resources, by checking the _diceRollsToTiles
     * HashMap to check which tiles' resources should be distributed and telling each player to take resources of that
     * tile if applicable. If a 7 is rolled, it triggers a robber placement. It also toggles the buttons to reflect
     * which actions are available next.
     */
    private void rollDice() {
        int dieA = (int) (Math.random() * 6) + 1;
        int dieB = (int) (Math.random() * 6) + 1;
        _diceRoll.setText("Total: " + (dieA + dieB));
        _dieA.setImage(new Image(this.getClass().getResourceAsStream("Dice" + dieA + ".png")));
        _dieB.setImage(new Image(this.getClass().getResourceAsStream("Dice" + dieB + ".png")));
        if (dieA + dieB != 7) {
            for (Tile tile : _diceRollToTiles.get(dieA + dieB)) {
                if (!tile.hasRobber()) {
                    for (Player player : _players) {
                        player.distributeResource(tile);
                    }
                }
            }
            this.toggleButtons();
        } else {
            _board.placeRobber(_currPlayer, false);
            this.changeInstructions(_currPlayer.getName() + ": Place the Robber");
            this.allButtonsOff();
        }
    }

    /**
     * This method is called whenever a player wants to buy a development card. It takes the first card from
     * the ArrayList of development cards and uses a switch statement to trigger its effect.
     */
    private void buyDevCard() {
        String card = _devCards.remove(0);
        switch (card) {
            case "Knight":
                _notificationLabel.setText("  " + _currPlayer.getName() + " got a Knight Development Card!  ");
                _gamePane.getChildren().add(_notificationPane);
                _currPlayer.oneKnightCard();
                break;
            case "Victory Point":
                _notificationLabel.setText(_currPlayer.getName() + " got a Victory Point Development Card!");
                _gamePane.getChildren().add(_notificationPane);
                _currPlayer.addVictoryPoints(1);
                break;
        }
        _currPlayer.boughtDevCard();
        this.toggleButtons();
    }

    /**
     * This method checks which actions the player can do and toggles the respective buttons.
     */
    public void toggleButtons() {
        if (_currPlayer.canBuyRoad()) {
            _buyRoad.setDisable(false);
        } else {
            _buyRoad.setDisable(true);
        }
        if (_currPlayer.canBuySettlement() && _board.hasValidSettlementLoc(_currPlayer)) {
            _buySettlement.setDisable(false);
        } else {
            _buySettlement.setDisable(true);
        }
        if (_currPlayer.canBuyCity()) {
            _buyCity.setDisable(false);
        } else {
            _buyCity.setDisable(true);
        }
        if (_currPlayer.canBuyDevCard()) {
            _buyDevCard.setDisable(false);
        } else {
            _buyDevCard.setDisable(true);
        }
        if (_currPlayer.canUseKnight()) {
            _useKnight.setDisable(false);
        } else {
            _useKnight.setDisable(true);
        }
        _nextPlayer.setDisable(false);
        _rollDice.setDisable(true);
    }

    /**
     * There are many times in the game when none of the buttons should be available to the players. Thsi method
     * turns all of them off.
     */
    private void allButtonsOff() {
        _buyRoad.setDisable(true);
        _buySettlement.setDisable(true);
        _buyCity.setDisable(true);
        _buyDevCard.setDisable(true);
        _useKnight.setDisable(true);
        _nextPlayer.setDisable(true);
        _rollDice.setDisable(true);
    }

    /**
     * This method sets the Text of the instructions.
     */
    public void changeInstructions(String string) {
        _instructions.setText(string);
    }

    /**
     * This method is called whenever a player get a longest road. It sets the notification pane to pop up, and
     * gives the player 2 more victory points!
     */
    public void newLongestRoad(Player player) {
        player.addVictoryPoints(2);
        _notificationLabel.setText(player.getName() + " has the longest road!");
        _gamePane.getChildren().add(_notificationPane);
    }

    /**
     * This method is called whenever a player get a largest army. It sets the notification pane to pop up, and
     * gives the player 2 more victory points!
     */
    public void newLargestArmy(Player player) {
        player.addVictoryPoints(2);
        _notificationLabel.setText(player.getName() + " has the largest army!");
        _gamePane.getChildren().add(_notificationPane);
    }

    /**
     * This method is called whenever a player reaches 8 points to win the game. Not much has to happen. All the
     * buttons are invalidated and the notification pane pops up announcing the winner.
     */
    public void gameOver(Player player) {
        this.allButtonsOff();
        _notificationLabel.setText(player.getName() + " has won the game!");
        _gamePane.getChildren().add(_notificationPane);
        this.changeInstructions("Game Over");
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                        **THESE ACCESSOR METHODS AND EVENTHANDLERS ARE FAIRLY SELF-EXPLANATORY**                   //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Important accessor method that returns the game pane to be placed in the root BorderPane.
     */
    public Pane getGamePane() {
        return _gamePane;
    }

    /**
     * Important accessor method that returns the settings pane to be placed in the root BorderPane.
     */
    public BorderPane getSettingsPane() {
        return _settingsPane;
    }

    /**
     * Rather than having a bajillion EventHandlers for one purpose each, I consolidated them all into this one
     * ButtonHandler. It uses a switch statement to determine the effect of the button based on whatever the button
     * does. Before the switch statement, it removes the notification pane if needed (so that the pane isn't stuck
     * around forever).
     */
    private class ButtonHandler implements EventHandler<ActionEvent> {
        private String _command;

        public ButtonHandler(String command) {
            _command = command;
        }

        @Override
        public void handle(ActionEvent e) {
            if (_gamePane.getChildren().contains(_notificationPane)) {
                _gamePane.getChildren().remove(_notificationPane);
            }
            switch (_command) {
                case "NextPlayer":
                    Game.this.nextTurn();
                    break;
                case "BuyRoad":
                    Game.this.allButtonsOff();
                    _board.purchaseRoad(_currPlayer);
                    break;
                case "BuySettlement":
                    Game.this.allButtonsOff();
                    _board.purchaseSettlement(_currPlayer);
                    break;
                case "BuyCity":
                    Game.this.allButtonsOff();
                    _board.purchaseCity(_currPlayer);
                    break;
                case "BuyDevCard":
                    Game.this.buyDevCard();
                    break;
                case "UseKnight":
                    _board.placeRobber(_currPlayer, true);
                    break;
                case "RollDice":
                    Game.this.rollDice();
                    break;
            }
        }
    }
}
