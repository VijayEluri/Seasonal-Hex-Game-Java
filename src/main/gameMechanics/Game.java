package gameMechanics;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import players.Player;
import players.PointAndClickPlayer;
import players.AdjPlayer;
import players.randomTurn.R_Path;
import players.randomTurn.R_Single;
import hexBoards.GameBoard;
import hexBoards.Board;
import players.AdjSeasonPlayer;

public class Game extends Thread implements Runner {

  private GameBoard board;
  private Player red;
  private Player blue;
  private int currentPlayer = Board.RED;
  private boolean finished = false;
  private boolean pause = false;
  private volatile boolean stop = false;
  private SeasonMechanics seasonPicker;
  private int gameType;
  private String commentary = "";

  public Game(int size, int type, int seasoncount, int redPlayer, String[] redArgs, int bluePlayer, String[] blueArgs) {
    this.seasonPicker = new SeasonMechanics(seasoncount);
    this.board = new GameBoard(size, seasonPicker);
    this.gameType = type;
    this.red = createPlayer(redPlayer, Board.RED, redArgs);
    this.blue = createPlayer(bluePlayer, Board.BLUE, blueArgs);
  }

  @Override
  public GameBoard getBoard() {
    return board;
  }

  @Override
  public void run() {

    Random coinflip = new Random();

    /*
     * Main running loop
     */
    while (!finished && !stop) {

      if (this.gameType == Runner.RANDOM_TURN)
        if (coinflip.nextBoolean() == true)
          this.currentPlayer = Board.BLUE;
        else
          this.currentPlayer = Board.RED;

      boolean moveAccepted = false;

      Move move = null;
      switch (currentPlayer) {
        case Board.RED:
          seasonPicker.thinkingPlayer(Board.RED);
          move = red.getMove();
          break;
        case Board.BLUE:
          seasonPicker.thinkingPlayer(Board.BLUE);
          move = blue.getMove();
          break;
        default:
          System.err.println("invoking mystery player");
          System.exit(1);
          break;
      }
      try {
        moveAccepted = board.makeMove(move);
      } catch (InvalidMoveException ex) {
        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
      }
      if (!moveAccepted)
        System.out.println("Move was not accepted, passing on...");


      /*
       * move has been accepted
       */

      if (board.checkwin(currentPlayer)) {
        notifyWin(currentPlayer);
        finished = true;
      }


      switch (currentPlayer) {
        case Board.RED:
          seasonPicker.increment(Board.RED);
          this.currentPlayer = Board.BLUE;
          break;
        case Board.BLUE:
          seasonPicker.increment(Board.BLUE);
          this.currentPlayer = Board.RED;
          break;
        default:
          System.err.println("invoking mystery player");
          System.exit(1);
          break;
      }
    }
  }

  public void notifyWin(int player) {
    this.finished = true;
    java.awt.Toolkit.getDefaultToolkit().beep();
    switch (player) {
      case Board.RED:
        System.out.println("Red wins!");
        announce("Red Wins!");
        break;
      case Board.BLUE:
        System.out.println("Blue wins!");
        break;
    }
  }

  @Override
  public void stopGame() {
    stop = true;
    System.out.println("Stopped!");
  }

  @Override
  public void pauseSwitch() {
    if (pause)
      pause = false;
    else
      pause = true;
    System.out.println("pause = " + pause);
    red.setPause(pause);
    blue.setPause(pause);
  }

  @Override
  public SeasonMechanics getSeasonPicker() {
    return seasonPicker;
  }

  private Player createPlayer(int type, int colour, String[] args) {
    Player player = null;
    switch (type) {
      case Player.R_PATH:
        player = new R_Path(this, colour, args);
        break;
      case Player.CLICK_PLAYER:
        player = new PointAndClickPlayer(this, colour);
        break;
      case Player.R_POINT:
        player = new R_Single(this, colour, args);
        break;
      case Player.ALL_PATH:
        player = new AdjPlayer(this, colour, args);
        break;
      case Player.SEASON_PATH:
        player = new AdjSeasonPlayer(this, colour, args);
        break;
      default:
        System.out.println("ERROR - no player or exception");
        break;
    }
    return player;
  }

  @Override
  public Player getPlayerBlue() {
    return blue;
  }

  @Override
  public Player getPlayerRed() {
    return red;
  }

  private void announce(String announcement) {
    this.commentary = announcement;
  }

  @Override
  public String getCommentary() {
    return commentary;
  }
}
