import java.util.ArrayList;
import java.util.Random;
import javalib.impworld.*;
import javalib.worldimages.*;
import tester.Tester;
import java.awt.Color;

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;

  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;

  }

  // Returns a WorldImage representation of this cell
  WorldImage drawCell(int cellSize) {
    return new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
  }
}

class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board = new ArrayList<Cell>();
  int boardSize;
  int numColors;
  int steps;
  int maxSteps;
  ArrayList<Cell> adjacentCells;
  int time;


  // constructor that takes in boardSize numColors
  FloodItWorld(int boardSize, int numColors) {
    this.boardSize = boardSize;
    this.numColors = numColors;
    this.board = createBoard();
    this.steps = 0;
    this.maxSteps = maxSteps(boardSize, numColors);
    this.adjacentCells =  new ArrayList<>();
    this.time = 0;
  }


  // constructor that takes in boardSize numColors and board
  FloodItWorld(int boardSize, int numColors, ArrayList<Cell> board) {
    this.boardSize = boardSize;
    this.numColors = numColors;
    this.board = board;
    this.time = 0;
  }

  // Creates the board
  ArrayList<Cell> createBoard() {
    ArrayList<Cell> board = new ArrayList<Cell>();
    Random rand = new Random();
    ArrayList<Color> colors = randColorGenerator(numColors);
    for (int y = 0; y < boardSize; y++) {
      for (int x = 0; x < boardSize; x++) {
        Color color = colors.get(rand.nextInt(numColors));
        boolean flooded = (x == 0 && y == 0);
        Cell cell = new Cell(x, y, color, flooded);
        board.add(cell);
      }
    }
    for (int i = 0; i < board.size(); i++) {
      Cell cell = board.get(i);
      if (cell.x > 0) {
        cell.left = board.get(i - 1);
      }
      if (cell.x < boardSize - 1) {
        cell.right = board.get(i + 1);
      }
      if (cell.y > 0) {
        cell.top = board.get(i - boardSize);
      }
      if (cell.y < boardSize - 1) {
        cell.bottom = board.get(i + boardSize);
      }
    }
    return board;
  }

  // Returns a randomly selected color
  ArrayList<Color> randColorGenerator(int numColors) {
    ArrayList<Color> colors = new ArrayList<Color>(); 
    Random rand = new Random();
    for (int x = 0; x < numColors; x++) {
      Color color = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
      colors.add(color);
    }
    return colors;
  }



  // Returns the amount of steps you have 
  public int maxSteps(int boardSize, int numColors) { 
    return (boardSize + numColors + 10); 
  }

  // Returns a WorldScene representation of the game state
  public WorldScene makeScene() {
    int size = 20;
    WorldScene scene = new WorldScene(boardSize * size, boardSize * size);

    // Draw the game board
    for (Cell cell : board) {
      int x = cell.x * size + size / 2;
      int y = cell.y * size + size / 2;
      WorldImage cellImage = new RectangleImage(size, size, OutlineMode.SOLID, cell.color);
      scene.placeImageXY(cellImage, x, y);
    }

    // Display the current number of steps and the maximum number of steps allowed
    WorldImage stepsText = new TextImage("Steps: " + steps + " / " + maxSteps, 16, 
        FontStyle.BOLD, Color.BLACK);
    scene.placeImageXY(stepsText, (int)(boardSize * size * 0.25), boardSize * size + 20);

    // Display the current time
    WorldImage timeText = new TextImage("Time: " + time, 16, FontStyle.BOLD, Color.BLACK);
    scene.placeImageXY(timeText, (int)(boardSize * size * 0.25), boardSize * size + 40);

    // Check the game status (win or lose) and display the appropriate message
    if (gameOver()) {
      String message = hasWon() ? "You won!" : "You lost!";
      WorldImage endText = new TextImage(message, 32, FontStyle.BOLD, Color.RED);
      scene.placeImageXY(endText, boardSize * size / 2, boardSize * size / 2);
    }

    return scene;
  }

  // Check if the game is over (either the player has won or lost)
  public boolean gameOver() {
    return hasWon() || steps >= maxSteps; 
  }

  // Check if the player has won the game
  public boolean hasWon() {
    Color firstCellColor = board.get(0).color;

    // Iterate through all cells in the board
    for (Cell cell : board) {
      // If any cell has a different color than the first cell, the player hasn't won yet
      if (!cell.color.equals(firstCellColor)) {
        return false;
      }
    }

    // If all cells have the same color, the player has won
    return true;
  }


  // Handle player clicks on cells
  public void onMouseClicked(Posn pos) {
    int cellSize = 20;
    if (!gameOver()) {
      int x = pos.x / cellSize;
      int y = pos.y / cellSize;
      int index = y * boardSize + x;
      Cell clickedCell = board.get(index);

      // Get the first cell (top-left corner)
      Cell firstCell = board.get(0);

      // If the clicked cell's color is different from the first cell's color, flood the area
      if (!clickedCell.color.equals(firstCell.color)) {
        flood(firstCell, clickedCell.color);
        steps++;
      }
    }
  }

  // Reset the game when the 'r' key is pressed
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      board = createBoard();
      steps = 0;
      time = 0;
    }
  }

  // Flood adjacent cells
  public void onTick() {
    if (!gameOver()) {
      time = time + 0;
      ArrayList<Cell> floodedCells = new ArrayList<>();

      // Get all flooded cells
      for (Cell cell : board) {
        if (cell.flooded) {
          floodedCells.add(cell);
        }
      }

      // Flood adjacent cells of each flooded cell
      for (Cell cell : floodedCells) {
        flood(cell, cell.color);
      }
    }
    time++;
  }

  public void flood(Cell cell, Color newColor) {
    Color oldColor = cell.color;
    cell.color = newColor;
    cell.flooded = true;
    floodAdjacent(cell, oldColor, newColor);
  }

  // Flood adjacent cells of the given cell with the new color 
  // if their current color matches the old color
  public void floodAdjacent(Cell cell, Color oldColor, Color newColor) {
    if (cell.left != null) {
      adjacentCells.add(cell.left);
    }
    if (cell.top != null) {
      adjacentCells.add(cell.top);
    }
    if (cell.right != null) {
      adjacentCells.add(cell.right);
    }
    if (cell.bottom != null) {
      adjacentCells.add(cell.bottom);
    }

    for (Cell adjacentCell : adjacentCells) {
      if (adjacentCell.color.equals(oldColor)) {
        adjacentCell.color = newColor;
        adjacentCell.flooded = true;
      }
    }
  }

}

class ExamplesFloodIt {
  FloodItWorld world;
  WorldScene scene;
  Cell cellFirst;
  Cell cellSec;
  Cell cellThird;
  Cell cellTop;
  Cell cellRight;
  Cell cellBottom;
  Cell cellLeft;

  void initData() {
    this.world = new FloodItWorld(14,6);
    this.cellFirst = new Cell(0, 0, Color.RED, true);
    this.cellSec = new Cell(12, 6, Color.CYAN, false);
    this.cellThird = new Cell(18, 8, Color.PINK, false);
    this.cellTop = new Cell(5, 5, Color.BLUE, false);
    this.cellRight = new Cell(3, 3, Color.MAGENTA, false);
    this.cellBottom = new Cell(4, 4, Color.RED, false);
    this.cellLeft = new Cell(2, 2, Color.GREEN, false);

  }

  void initData1() {
    this.world = new FloodItWorld(14,6);
    this.cellFirst = new Cell(0, 0, Color.RED, true);
    this.cellSec = new Cell(12, 6, Color.CYAN, true);
    this.cellThird = new Cell(18, 8, Color.PINK, false);
    this.cellTop = new Cell(5, 5, Color.BLUE, false);
    this.cellRight = new Cell(3, 3, Color.MAGENTA, false);
    this.cellBottom = new Cell(4, 4, Color.RED, false);
    this.cellLeft = new Cell(2, 2, Color.GREEN, false);
    this.cellFirst.bottom = cellBottom;
    this.cellFirst.top = cellTop;
    this.cellFirst.left = cellLeft;
    this.cellFirst.right = cellRight;
  }

  // to represent a board
  ArrayList<Cell> board = new ArrayList<Cell>();

  // to test the drawCell method
  void testdrawCell(Tester t) { 
    initData();
    t.checkExpect(cellFirst.drawCell(20), new RectangleImage(20, 20, OutlineMode.SOLID, 
        Color.red));
    t.checkExpect(cellSec.drawCell(20), new RectangleImage(20, 20, OutlineMode.SOLID, 
        Color.cyan));
    t.checkExpect(cellThird.drawCell(20), new RectangleImage(20, 20, OutlineMode.SOLID, 
        Color.pink));
  }

  // Tests for CreateBoard
  void testCreateBoard(Tester t) {
    this.initData();
    t.checkExpect(world.createBoard().size(), world.boardSize * world.boardSize);
  }

  // test for max steps
  void testMaxSteps(Tester t) {
    this.initData();
    t.checkExpect(world.maxSteps(14, 6), 30);
    t.checkExpect(world.maxSteps(10, 5), 25);
  }

  // Tests for makeScene
  void testMakeScene(Tester t) {
    this.initData();
    scene = world.makeScene();
    t.checkExpect(scene.height, 280);
    t.checkExpect(scene.width, 280);
  }

  //Test for game over
  void testGameOver(Tester t) {
    this.initData();
    t.checkExpect(world.gameOver(), false); 
    for (Cell cell : world.board) {
      cell.color = Color.RED;
    }
    t.checkExpect(world.gameOver(), true); 
  }

  // Tests for gameWon
  void testWin(Tester t) {
    this.initData();
    t.checkExpect(world.hasWon(), false);
    for (Cell cell : world.board) {
      cell.color = Color.RED;
    }
    t.checkExpect(world.hasWon(), true);
  }

  // Tests for key event
  void testKeyEvent(Tester t) {
    this.initData();
    ArrayList<Cell> testBoard = world.board;
    FloodItWorld testWorld = this.world;
    testWorld.onTick();
    testWorld.onKeyEvent("r");
    t.checkExpect(testBoard.get(0) == world.board.get(0), false);
    t.checkExpect(testWorld.time == world.time, true);
  }

  // Tests for OnMouseClick
  void testOnMouseClick(Tester t) {
    this.initData();
    t.checkExpect(cellFirst.flooded, true);
    t.checkExpect(cellRight.flooded, false);
    world.onMouseClicked(new Posn(this.cellRight.x, this.cellRight.y));
    int x = (0 / 20);
    int y = (10 / 20);
    Cell current = world.board.get(((world.boardSize * x) + y));
    t.checkExpect(cellFirst.flooded, true);
    t.checkExpect(current.flooded, true);
  }


  // Tests for onTick
  void testOnTick(Tester t) {
    this.initData();
    t.checkExpect(world.time, 0);
    t.checkExpect(cellFirst.flooded, true);
    t.checkExpect(cellLeft.flooded, false);
    world.onTick();
    t.checkExpect(world.time, 1);
    t.checkExpect(cellFirst.flooded, true);
    t.checkExpect(cellLeft.flooded, false);
  }

  // Tests for floodAdjacent
  void testfloodAdjacent(Tester t) {
    this.initData1();
    world.floodAdjacent(cellFirst, Color.red, Color.blue);
    t.checkExpect(cellRight.color, Color.magenta);
    t.checkExpect(cellLeft.color, Color.green);
    t.checkExpect(cellBottom.color, Color.blue);
    t.checkExpect(cellTop.color, Color.blue);
  }

  // Tests for floodArea
  void testfloodArea(Tester t) {
    this.initData1();
    world.flood(cellFirst, Color.blue);
    t.checkExpect(cellFirst.color, Color.blue);
    t.checkExpect(cellRight.color, Color.magenta);
    t.checkExpect(cellLeft.color, Color.green);
    t.checkExpect(cellBottom.color, Color.blue);
    t.checkExpect(cellTop.color, Color.blue);
  }

  //runs the game by creating a world and calling bigBang
  void testWorld(Tester t) {
    FloodItWorld sWorld = new FloodItWorld(14, 6);
    sWorld.bigBang(280, 500, 1);
  }


}