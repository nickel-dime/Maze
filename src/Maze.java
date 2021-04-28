import javalib.impworld.*;
import javalib.worldimages.*;

import java.awt.Color;

import tester.Tester;

import java.util.*;

// either a maze can be normal, or have a bias in the horizontal or vertical direction
enum MazeType {
  VERTICAL, HORIZONTAL, NORMAL
}

// represents a maze
class Maze extends World {

  // width and height of our scene
  private final int width;
  private final int height;

  // all the cells in our maze
  // not final because we allow the user to design a new random maze
  private ArrayList<ArrayList<Cell>> grid;

  // user's current location in the maze
  // not final because the user can change the current location
  private Posn curLoc;

  // size of our cells
  private final int cellSize;

  // random for generating path weights
  private final Random rand;

  // an object storing our depth and breadth first search
  // not final because we create a new search if we reset the maze
  private Search search;

  // not final because we can reset the maze and create a new arraylist
  private ArrayList<Cell> cellsPlayerVisited;

  // not final because of resetting maze
  // if rendering the search is completed
  private boolean searchedMaze;

  // not final because the user can toggle if we want to see paths or not
  // toggles if the user is viewing the explored paths
  private boolean isViewingPaths;

  // not final because it is a toggle
  // toggles if the paths display at once or animate
  private boolean isImmediate;

  // our mininum spanning tree
  // not final since we allow the user to design a new random maze
  private ArrayList<Path> mst;

  Maze(int height, int width) {
    this(height, width, new Random());
  }

  Maze(int height, int width, Random seed) {
    // throughout the program we know the maze will always be valid
    if (height <= 0 || width <= 0) {
      throw new IllegalArgumentException("Illegal width/height for maze");
    }
    this.rand = seed;
    this.width = width;
    this.height = height;

    // our bigbang will be 720 pixels wide
    int minDimension = Math.max(height, width);
    this.cellSize = 720 / minDimension;

    // initalizes maze normally
    this.resetMaze(MazeType.NORMAL);

    // doesn't matter which search we use here because we are only using it for the solution,
    // which is same for both bfs/dfs. If the user selects bfs/dfs, this field is mutated
    // to be the correct search
    this.search = new Search(new Queue<Cell>(),
            this.grid.get(0).get(0), this.grid.get(height - 1).get(width - 1));

    // we threw an exception, so the grid needs to be at least 1x1
    this.curLoc = new Posn(0, 0);
    this.isViewingPaths = true;
    this.isImmediate = false;
  }

  // EFFECT: resets the maze to reconstruct and connect a grid, and then creates the mst
  // and uses it to construct a well-made maze
  // We did not test this since it is private (and mutates a lot - so we didn't want it to
  // be public). However, it is a pretty basic method since it just
  private void resetMaze(MazeType type) {
    this.searchedMaze = false;

    // construct grid
    ArrayList<ArrayList<Cell>> tempGrid = this.constructGrid(height, width);
    ArrayList<Path> allPaths = this.connectGrid(tempGrid, type);
    this.mst = new Kruskal(allPaths, tempGrid).createMST();

    this.grid = tempGrid;

    // EXTRA CREDIT (WHISTLE 1)
    // If you want to reconstruct walls on tick, comment this out
    // I didn't make it an onKeyEvent because wall construction is slow.
    while (!this.mst.isEmpty()) {
      Path path = this.mst.remove(0);
      path.removeWall();
    }

    this.resetColor();
    this.curLoc = new Posn(0, 0);
    this.cellsPlayerVisited = new ArrayList<Cell>();
    this.search = new Search(new Queue<Cell>(),
            this.grid.get(0).get(0), this.grid.get(height - 1).get(width - 1));

  }

  // creates a grid with each cell having a posn of its location
  // we made this public because it doesn't mutate or effect any of our code
  ArrayList<ArrayList<Cell>> constructGrid(int height, int width) {
    ArrayList<ArrayList<Cell>> temp = new ArrayList<ArrayList<Cell>>();

    // adds each row to the grid
    for (int i = 0; i < height; i += 1) {
      ArrayList<Cell> row = new ArrayList<Cell>();

      // adds a cell with a position to a row
      for (int j = 0; j < width; j += 1) {
        row.add(new Cell(new Posn(j, i), this.cellSize));
      }
      temp.add(row);
    }
    return temp;
  }

  // EFFECT: updates the grid's cells to have the correct paths with random weights
  // and returns all paths created
  // Not private because no matter what grid the user passes, this method should
  // always return the same list of edges and connect the same nodes.
  ArrayList<Path> connectGrid(ArrayList<ArrayList<Cell>> grid, MazeType type) {
    ArrayList<Path> paths = new ArrayList<Path>();

    // loops through the rows of a grid
    for (int i = 0; i < grid.size(); i += 1) {
      ArrayList<Cell> row = grid.get(i);

      // loops through the cells in a row
      for (int j = 0; j < row.size(); j += 1) {
        Cell currCell = row.get(j);

        // extra credit (bell 4)
        // if we wanted to only have a bias in a certain direction, we would just change the
        // bound to be something like 200, instead of adding 100 directly after the random
        // number was generated. Currently, if we chose a vertical maze it would be impossible for
        // a horizontal edge to remain, but if we make a bound of 200, it would just be weighted
        // differently, since a random number below 100 could still appear.

        // if there is a cell on the right adds it to the path and updates the cell
        if (j + 1 < row.size()) {
          int valToAdd = 0;
          if (type == MazeType.VERTICAL) {
            valToAdd = 101;
          }
          Path newPath = new Path(currCell, row.get(j + 1), rand.nextInt(100) + valToAdd);
          paths.add(newPath);
        }

        // if there is a cell below adds it to the path and updates the cell
        if (i + 1 < grid.size()) {
          int valToAdd = 0;
          if (type == MazeType.HORIZONTAL) {
            valToAdd = 101;
          }
          Path newPath = new Path(currCell, grid.get(i + 1).get(j), rand.nextInt(100) + valToAdd);
          paths.add(newPath);
        }
      }
    }
    return paths;
  }

  // creates our scene

  /**
   * For javadoc autograder
   * @return our worldscene
   */
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    // display grid on worldscene
    for (int i = 0; i < this.grid.size(); i += 1) {
      ArrayList<Cell> row = this.grid.get(i);
      for (int j = 0; j < row.size(); j += 1) {
        Cell curr = row.get(j);
        if (i == 0 && j == 0) {
          curr.clr = new Color(0, 100, 0);
        } else if (i == this.grid.size() - 1 && j == row.size() - 1) {
          curr.clr = new Color(128, 0, 128);
        } else if (j == this.curLoc.x && i == this.curLoc.y) {
          curr.clr = new Color(0, 255,0);
        }
        scene.placeImageXY(curr.renderCell(), j * this.cellSize, i * this.cellSize);
      }
    }
    RectangleImage border = new RectangleImage(this.width * this.cellSize,
            this.height * this.cellSize, OutlineMode.OUTLINE, Color.GRAY);
    scene.placeImageXY(border, this.width * this.cellSize / 2,
            this.height * this.cellSize / 2);

    if ((this.curLoc.x == this.width - 1) && (this.curLoc.y == this.height - 1)) {
      TextImage t = new TextImage("You solved the maze with "
              + this.playerWrongMoves()
              + " wrong moves", Color.BLACK);
      if (this.height * this.cellSize > 680) {
        scene.placeImageXY(t, 550, 690);
      } else {
        scene.placeImageXY(t, 550, this.height * this.cellSize + 10);
      }
    }

    if (searchedMaze) {
      TextImage t = new TextImage("The search algorithm had: "
              + this.search.numWrongMoves()
              + " wrong moves", Color.BLACK);
      if (this.height * this.cellSize > 700) {
        scene.placeImageXY(t, 550, 710);
      } else {
        scene.placeImageXY(t, 550, this.height * this.cellSize + 30);
      }
    }


    return scene;
  }


  /**
   * For javadoc autograder
   */

  // EFFECT: removes path every tick, adjusting the walls in the cell
  public void onTick() {
    // remove walls on tick
    if (!this.mst.isEmpty()) {
      Path path = this.mst.remove(0);
      path.removeWall();
    }

    if (!this.isViewingPaths && searchedMaze) {
      this.search.renderSolution();
      return;
    }

    // only render solution once we're visually done searching the maze
    if (searchedMaze) {
      if (isImmediate) {
        this.search.renderExploration(this.search.seen);
        this.search.renderSolution();
      } else {
        if (!this.search.renderExplorationSingle()) {
          this.search.renderSolution();
        }
      }
    }
  }

  /**
   * For javadoc autograder
   * @param s key input
   */
  // EFFECT: manages user input and runs the corresponding method (which could reset mazes/mutate)
  // Tested this method thru big bang
  public void onKeyEvent(String s) {
    // refer to readme for controls
    if (s.equals("r")) {
      this.resetMaze(MazeType.NORMAL);

    } else if (s.equals("h")) {
      this.resetMaze(MazeType.HORIZONTAL);

    } else if (s.equals("v")) {
      this.resetMaze(MazeType.VERTICAL);

    } else if (s.equals("d")) {
      // if we're still constructing the maze do nothing
      if (!this.mst.isEmpty()) {
        return;
      }

      this.resetColor();
      this.search = new Search(new Stack<Cell>(), this.grid.get(0).get(0),
              this.grid.get(height - 1).get(width - 1));
      this.searchedMaze = true;

    } else if (s.equals("b")) {
      // if we're still constructing the maze do nothing
      if (!this.mst.isEmpty()) {
        return;
      }

      this.resetColor();
      this.search = new Search(new Queue<Cell>(), this.grid.get(0).get(0),
              this.grid.get(this.height - 1).get(this.width - 1));
      this.searchedMaze = true;

    } else if (s.equals("up")) {
      this.movePlayer(new Posn(this.curLoc.x, this.curLoc.y - 1));

    } else if (s.equals("down")) {
      this.movePlayer(new Posn(this.curLoc.x, this.curLoc.y + 1));

    } else if (s.equals("left")) {
      this.movePlayer(new Posn(this.curLoc.x - 1, this.curLoc.y));

    } else if (s.equals("right")) {
      this.movePlayer(new Posn(this.curLoc.x + 1, this.curLoc.y));

    } else if (s.equals("p")) {
      this.isViewingPaths = !this.isViewingPaths;
      Cell curCell = this.grid.get(this.curLoc.y).get(this.curLoc.x);

      // either reset the color to gray or render the exploration again
      if (!this.isViewingPaths) {
        if (searchedMaze) {
          this.search.renderSolution();
        }
        this.resetColor();
        curCell.clr = new Color(0, 255,0);
      } else {
        if (searchedMaze) {
          this.search.renderExploration(this.search.seen);
        }
        this.cellsPlayerVisited.remove(curCell);
        this.search.renderExploration(this.cellsPlayerVisited);
        this.cellsPlayerVisited.add(curCell);
      }

    } else if (s.equals("c")) {
      this.resetColor();
      this.curLoc = new Posn(0, 0);
      this.cellsPlayerVisited = new ArrayList<Cell>();
      this.search = new Search(new Queue<Cell>(), this.grid.get(0).get(0),
              this.grid.get(height - 1).get(width - 1));
      this.searchedMaze = false;

    } else if (s.equals("t")) { // toggles if want to display on tick or immediately
      this.isImmediate = !this.isImmediate;

    } else if (s.equals("s")) {
      this.search.howFarFromCell(this.width * this.height, this.grid.get(0).get(0));

    } else if (s.equals("e")) {
      this.search.howFarFromCell(this.width * this.height,
              this.grid.get(height - 1).get(width - 1));
    }
  }

  // is the posn in the bounds of our maze's grid
  // public because it does not mutate anything and is a useful util method
  boolean inBounds(Posn pos) {
    return pos.x >= 0 && pos.x < this.width && pos.y >= 0 && pos.y < this.height;
  }

  // EFFECT: moves our player to a position, if possible
  private void movePlayer(Posn pos) {
    Cell curCell = this.grid.get(this.curLoc.y).get(this.curLoc.x);
    // if there's no wall and it's in bounds
    if (this.inBounds(pos) && curCell.canMove(this.grid.get(pos.y).get(pos.x))) {
      Cell newCell = this.grid.get(pos.y).get(pos.x);
      if (this.isViewingPaths) {
        curCell.clr = new Color(176,224,230);
      } else {
        curCell.clr = Color.LIGHT_GRAY;
      }
      newCell.clr = new Color(0, 255,0);

      this.curLoc = pos;
      if (!this.cellsPlayerVisited.contains(newCell)) {
        this.cellsPlayerVisited.add(newCell);
      }
    }
  }

  // the number of wrong moves in the maze
  // we cannot test this method because I can't change
  // cellsPlayerVisited without onKeyEvent or adding a backdoor
  // (which I don't want to do). I feel confident in this method because
  // it is very similar to the numWrongMoves in search, with slight modifications
  int playerWrongMoves() {
    int numWrongMoves = 0;
    // compare solution cells with the visited cells
    for (Cell c : this.cellsPlayerVisited) {
      // we don't want to count the solution itself
      if (!c.equals(this.grid.get(this.height - 1).get(this.width - 1))
              && !this.search.solution.contains(c)) {
        numWrongMoves += 1;
      }
    }
    return numWrongMoves;
  }

  // EFFECT: resets the grid back to gray, removing the visual search algorithm
  // I did not test this method because it was private and very simple
  // we made it private because it shouldn't be accessed by anyone else since it
  // mutates the grid
  private void resetColor() {
    // set all the cells in the grid back to default gray
    for (ArrayList<Cell> row : this.grid) {
      for (Cell cell : row) {
        cell.clr = Color.LIGHT_GRAY;
      }
    }
  }
}

class ExamplesMaze {

  Cell zeroZero2x2;
  Cell zeroOne2x2;
  Cell oneZero2x2;
  Cell oneOne2x2;

  Random rand;
  ArrayList<ArrayList<Cell>> grid1x1;
  Maze maze1x1;
  ArrayList<Path> paths1x1;

  ArrayList<ArrayList<Cell>> grid2x2;
  Maze maze2x2;
  ArrayList<Path> paths2x2;
  Path path2x21;
  Path path2x22;
  Path path2x23;
  Path path2x24;
  Search search2x2;

  ArrayList<ArrayList<Cell>> grid3x3;
  Maze maze3x3;
  ArrayList<Path> paths3x3;
  Path path3x31;
  Path path3x32;
  Path path3x33;
  Path path3x34;
  Path path3x35;
  Path path3x36;
  Path path3x37;
  Path path3x38;
  Path path3x39;
  Path path3x310;
  Path path3x311;
  Path path3x312;

  ArrayList<ArrayList<Cell>> grid2x4;
  Maze maze2x4;
  ArrayList<Path> paths2x4;
  Cell one2x4;
  Cell two2x4;
  Cell three2x4;
  Cell four2x4;
  Cell five2x4;
  Cell six2x4;
  Cell seven2x4;
  Cell eight2x4;

  Path path2x41;
  Path path2x42;
  Path path2x43;
  Path path2x44;
  Path path2x45;
  Path path2x46;
  Path path2x47;
  Path path2x48;
  Path path2x49;
  Path path2x410;
  Search search2x4;

  Search dfs2x2;
  Search bfs2x2;
  Search dfs2x4;
  Search bfs2x4;
  Search bfs1x1;
  Search dfs1x1;

  void initTestConditions() {

    // 1x1 maze
    ArrayList<Cell> row11x1 = new ArrayList<Cell>();
    row11x1.add(new Cell(new Posn(0, 0), 720));
    grid1x1 = new ArrayList<ArrayList<Cell>>(List.of(row11x1));
    rand = new Random(11);
    maze1x1 = new Maze(1, 1, rand);
    this.paths1x1 = new ArrayList<Path>(List.of());

    // 2x2 maze
    ArrayList<Cell> row12x2 = new ArrayList<Cell>();
    zeroZero2x2 = new Cell(new Posn(0, 0), 720 / 2);
    row12x2.add(zeroZero2x2);
    oneZero2x2 = new Cell(new Posn(1, 0), 720 / 2);
    row12x2.add(oneZero2x2);
    ArrayList<Cell> row22x2 = new ArrayList<Cell>();
    zeroOne2x2 = new Cell(new Posn(0, 1), 720 / 2);
    row22x2.add(zeroOne2x2);
    oneOne2x2 = new Cell(new Posn(1, 1), 720 / 2);
    row22x2.add(oneOne2x2);

    // create paths
    path2x21 = new Path(row12x2.get(0), row12x2.get(1), 21);
    path2x22 = new Path(row12x2.get(0), row22x2.get(0), 94);
    path2x23 = new Path(row12x2.get(1), row22x2.get(1), 54);
    path2x24 = new Path(row22x2.get(0), row22x2.get(1), 29);

    grid2x2 = new ArrayList<ArrayList<Cell>>(List.of(row12x2, row22x2));

    maze2x2 = new Maze(2, 2, rand);
    paths2x2 = new ArrayList<Path>(List.of(path2x21, path2x22, path2x23, path2x24));

    // 3x3 maze
    ArrayList<Cell> row13x3 = new ArrayList<Cell>();
    row13x3.add(new Cell(new Posn(0, 0), 720 / 3));
    row13x3.add(new Cell(new Posn(1, 0), 720 / 3));
    row13x3.add(new Cell(new Posn(2, 0), 720 / 3));
    ArrayList<Cell> row23x3 = new ArrayList<Cell>();
    row23x3.add(new Cell(new Posn(0, 1), 720 / 3));
    row23x3.add(new Cell(new Posn(1, 1), 720 / 3));
    row23x3.add(new Cell(new Posn(2, 1), 720 / 3));
    ArrayList<Cell> row33x3 = new ArrayList<Cell>();
    row33x3.add(new Cell(new Posn(0, 2), 720 / 3));
    row33x3.add(new Cell(new Posn(1, 2), 720 / 3));
    row33x3.add(new Cell(new Posn(2, 2), 720 / 3));

    grid3x3 = new ArrayList<ArrayList<Cell>>(List.of(row13x3, row23x3, row33x3));
    maze3x3 = new Maze(3, 3, rand);
    path3x31 = new Path(row13x3.get(0), row13x3.get(1), 8);
    path3x32 = new Path(row13x3.get(0), row23x3.get(0), 11);
    path3x33 = new Path(row13x3.get(1), row13x3.get(2), 36);
    path3x34 = new Path(row13x3.get(1), row23x3.get(1), 15);
    path3x35 = new Path(row13x3.get(2), row23x3.get(2), 79);
    path3x36 = new Path(row23x3.get(0), row23x3.get(1), 68);
    path3x37 = new Path(row23x3.get(0), row33x3.get(0), 74);
    path3x38 = new Path(row23x3.get(1), row23x3.get(2), 29);
    path3x39 = new Path(row23x3.get(1), row33x3.get(1), 92);
    path3x310 = new Path(row23x3.get(2), row33x3.get(2), 32);
    path3x311 = new Path(row33x3.get(0), row33x3.get(1), 42);
    path3x312 = new Path(row33x3.get(1), row33x3.get(2), 23);
    paths3x3 = new ArrayList<Path>(List.of(path3x31, path3x32, path3x33, path3x34,
            path3x35, path3x36, path3x37, path3x38, path3x39,
            path3x310, path3x311, path3x312));

    // 2x4 maze
    ArrayList<Cell> row12x4 = new ArrayList<Cell>();
    one2x4 = new Cell(new Posn(0, 0), 720 / 4);
    two2x4 = new Cell(new Posn(1, 0), 720 / 4);
    three2x4 = new Cell(new Posn(2, 0), 720 / 4);
    four2x4 = new Cell(new Posn(3, 0), 720 / 4);
    five2x4 = new Cell(new Posn(0, 1), 720 / 4);
    six2x4 = new Cell(new Posn(1, 1), 720 / 4);
    seven2x4 = new Cell(new Posn(2, 1), 720 / 4);
    eight2x4 = new Cell(new Posn(3, 1), 720 / 4);
    row12x4.add(one2x4);
    row12x4.add(two2x4);
    row12x4.add(three2x4);
    row12x4.add(four2x4);
    ArrayList<Cell> row22x4 = new ArrayList<Cell>();
    row22x4.add(five2x4);
    row22x4.add(six2x4);
    row22x4.add(seven2x4);
    row22x4.add(eight2x4);

    // create paths
    path2x41 = new Path(row12x4.get(0), row12x4.get(1), 14);
    path2x42 = new Path(row12x4.get(0), row22x4.get(0), 67);
    path2x43 = new Path(row12x4.get(1), row12x4.get(2), 96);
    path2x44 = new Path(row12x4.get(1), row22x4.get(1), 0);
    path2x45 = new Path(row12x4.get(2), row12x4.get(3), 44);
    path2x46 = new Path(row12x4.get(2), row22x4.get(2), 94);
    path2x47 = new Path(row12x4.get(3), row22x4.get(3), 29);
    path2x48 = new Path(row22x4.get(0), row22x4.get(1), 38);
    path2x49 = new Path(row22x4.get(1), row22x4.get(2), 85);
    path2x410 = new Path(row22x4.get(2), row22x4.get(3), 16);

    grid2x4 = new ArrayList<ArrayList<Cell>>(List.of(row12x4, row22x4));
    maze2x4 = new Maze(2, 4, rand);
    paths2x4 = new ArrayList<Path>(List.of(path2x41, path2x42, path2x43, path2x44, path2x45,
            path2x46, path2x47, path2x48, path2x49, path2x410));
  }

  // runs our maze
  void testRun(Tester t) {
    this.initTestConditions();
    Maze test1 = new Maze(50, 80);
    test1.bigBang(720, 720, 0.001);
  }

  // tests constructing the grid
  void testConstructGrid(Tester t) {
    this.initTestConditions();
    t.checkExpect(maze1x1.constructGrid(1, 1), grid1x1);
    t.checkExpect(maze2x2.constructGrid(2, 2), grid2x2);
    t.checkExpect(maze3x3.constructGrid(3, 3), grid3x3);
    t.checkExpect(maze2x4.constructGrid(2, 4), grid2x4);
  }

  // tests making grid connections
  void testConnectGrid(Tester t) {
    this.initTestConditions();
    ArrayList<Path> generatedPaths1x1 = maze1x1.connectGrid(grid1x1, MazeType.NORMAL);
    ArrayList<Path> generatedPaths2x2 = maze2x2.connectGrid(grid2x2, MazeType.NORMAL);
    ArrayList<Path> generatedPaths3x3 = maze3x3.connectGrid(grid3x3, MazeType.NORMAL);
    ArrayList<Path> generatedPaths2x4 = maze2x4.connectGrid(grid2x4, MazeType.NORMAL);

    t.checkExpect(generatedPaths1x1, paths1x1);
    t.checkExpect(generatedPaths2x2, paths2x2);
    t.checkExpect(generatedPaths3x3, paths3x3);
    t.checkExpect(generatedPaths2x4, paths2x4);
  }

  // test passing in an illegal maze
  void testConstructorException(Tester t) {
    this.initTestConditions();
    t.checkConstructorException(new IllegalArgumentException(
            "Illegal width/height for maze"), "Maze", 0, 0);
    t.checkConstructorException(new IllegalArgumentException(
            "Illegal width/height for maze"), "Maze", -3, -3);
    t.checkExpect(new Maze(3, 3, new Random(12)),
            new Maze(3, 3, new Random(12)));
  }

  // tested in bounds
  void testInBounds(Tester t) {
    this.initTestConditions();
    t.checkExpect(maze2x2.inBounds(new Posn(1, 2)), false);
    t.checkExpect(maze2x2.inBounds(new Posn(2, 1)), false);
    t.checkExpect(maze2x2.inBounds(new Posn(-1, 1)), false);
    t.checkExpect(maze2x2.inBounds(new Posn(1, -1)), false);
    t.checkExpect(maze2x2.inBounds(new Posn(1, 1)), true);
    t.checkExpect(maze2x2.inBounds(new Posn(0, 1)), true);

  }

  // QUICKSORT FILE
  // tests sorting an arraylist
  // we were taught this in class, so we only tested it twice
  void testQuicksort(Tester t) {
    this.initTestConditions();

    ArrayList<Path> paths1x1Sorted = new ArrayList<Path>(List.of());
    new Quicksort<Path>(paths1x1).quicksort();
    t.checkExpect(paths1x1, paths1x1Sorted);

    ArrayList<Path> paths2x2Sorted = new ArrayList<Path>(List.of(
            new Path(zeroZero2x2, oneZero2x2, 21),
            new Path(zeroOne2x2, oneOne2x2, 29),
            new Path(oneZero2x2, oneOne2x2, 54),
            new Path(zeroZero2x2, zeroOne2x2, 94)));
    new Quicksort<Path>(paths2x2).quicksort();
    t.checkExpect(paths2x2, paths2x2Sorted);
  }

  // KRUSKAL FILE
  void testMST(Tester t) {
    this.initTestConditions();
    // 1x1
    ArrayList<Path> generatedMST1x1 = new Kruskal(paths1x1, grid1x1).createMST();
    t.checkExpect(generatedMST1x1, new ArrayList<Path>());

    // 2x4
    ArrayList<Path> generatedMST2x4 = new Kruskal(paths2x4, grid2x4).createMST();
    ArrayList<Path> MST2x4 = new ArrayList<Path>(List.of(path2x44, path2x41, path2x410, path2x47,
            path2x48, path2x45, path2x49));
    t.checkExpect(generatedMST2x4.get(6), MST2x4.get(6));

    // 3x3
    ArrayList<Path> generatedMST3x3 = new Kruskal(paths3x3, grid3x3).createMST();
    ArrayList<Path> MST3x3 = new ArrayList<Path>(List.of(path3x31, path3x32, path3x34, path3x312,
            path3x38, path3x310, path3x33, path3x311));
    t.checkExpect(generatedMST3x3, MST3x3);
  }

  // tests initializing the hashmap
  void testInitializeHashMap(Tester t) {
    this.initTestConditions();

    Kruskal kruskal1x1 = new Kruskal(paths1x1, grid1x1);
    Kruskal kruskal3x3 = new Kruskal(paths3x3, grid3x3);

    HashMap<Posn, Posn> hash1x1 = new HashMap<Posn, Posn>();
    hash1x1.put(new Posn(0, 0), new Posn(0, 0));
    t.checkExpect(kruskal1x1.initializeHashMap(grid1x1), hash1x1);

    HashMap<Posn, Posn> hash3x3 = new HashMap<Posn, Posn>();
    hash3x3.put(new Posn(0, 0), new Posn(0, 0));
    hash3x3.put(new Posn(0, 1), new Posn(0, 1));
    hash3x3.put(new Posn(1, 0), new Posn(1, 0));
    hash3x3.put(new Posn(1, 1), new Posn(1, 1));
    hash3x3.put(new Posn(0, 2), new Posn(0, 2));
    hash3x3.put(new Posn(1, 2), new Posn(1, 2));
    hash3x3.put(new Posn(2, 2), new Posn(2, 2));
    hash3x3.put(new Posn(2, 0), new Posn(2, 0));
    hash3x3.put(new Posn(2, 1), new Posn(2, 1));

    t.checkExpect(kruskal3x3.initializeHashMap(grid3x3), hash3x3);

  }

  // PATH FILE
  // test remove wall
  void testRemoveWall(Tester t) {
    this.initTestConditions();

    Path right = path3x31;
    Path down = path3x32;
    Path up = new Path(zeroOne2x2, zeroZero2x2, 0);
    Path left = new Path(oneZero2x2, zeroZero2x2, 0);

    right.removeWall();
    t.checkExpect(right.to.showLeft, false);
    t.checkExpect(right.to.showTop, true);
    t.checkExpect(right.from.showLeft, true);
    t.checkExpect(right.from.showTop, true);

    down.removeWall();
    t.checkExpect(down.to.showTop, false);
    t.checkExpect(right.from.showLeft, true);
    t.checkExpect(right.from.showTop, true);

    up.removeWall();
    t.checkExpect(up.from.showTop, false);
    t.checkExpect(right.from.showLeft, true);

    left.removeWall();
    t.checkExpect(left.from.showLeft, false);
  }

  // CELL FILE
  // tests getting direction between two cells
  void testDirection(Tester t) {
    this.initTestConditions();
    Cell from1 = path3x31.from;
    Cell to1 = path3x31.to;
    Cell from2 = path2x46.from;
    Cell to2 = path2x46.to;
    Cell from3 = path3x312.from;
    Cell to3 = path3x312.to;

    t.checkExpect(from1.direction(to1.pos), "r");
    t.checkExpect(to1.direction(from1.pos), "l");
    t.checkExpect(from2.direction(to2.pos), "d");
    t.checkExpect(to2.direction(from2.pos), "u");
    t.checkExpect(from3.direction(to3.pos), "r");
    t.checkExpect(to3.direction(from3.pos), "l");
    t.checkException(new RuntimeException("cannot get direction between two non-adjacent cells"),
            new Cell(new Posn(0, 0), 1), "direction", new Posn(1, 1));
    t.checkException(new RuntimeException("the passed in cell is two far in one direction"),
            new Cell(new Posn(0, 0), 1), "direction", new Posn(0, 3));
  }

  // test canMove
  void testCanMove(Tester t) {
    this.initTestConditions();
    this.addPaths();

    t.checkExpect(zeroZero2x2.canMove(oneOne2x2), false);
    t.checkExpect(zeroZero2x2.canMove(zeroOne2x2), true);
    t.checkExpect(one2x4.canMove(two2x4), true);
    t.checkExpect(one2x4.canMove(four2x4), false);

  }

  // tests rendering a cell
  void testRenderCell(Tester t) {
    this.initTestConditions();
    // random selection of cells
    Cell c1 = path3x32.from;
    Cell c2 = path2x41.to;
    Cell c3 = path3x39.to;
    Cell c4 = path2x42.from;

    // c1 with all walls
    int size3x3 = 720 / 3;
    RectangleImage base3x3 = new RectangleImage(size3x3, size3x3,
            OutlineMode.SOLID,
            Color.LIGHT_GRAY);
    WorldImage allWall = new OverlayOffsetImage(new LineImage(new Posn(size3x3, 0),
            Color.BLACK), 0, size3x3 / 2.0, new OverlayOffsetImage(new LineImage(new Posn(0,
            size3x3), Color.BLACK), size3x3 / 2.0, 0, base3x3)).movePinhole(
            size3x3 / -2.0, size3x3 / -2.0);
    t.checkExpect(c1.renderCell(), allWall);

    // c2 with no left wall
    c2.showLeft = false;
    int size4x4 = 720 / 4;
    RectangleImage base4x4 = new RectangleImage(size4x4, size4x4,
            OutlineMode.SOLID,
            Color.LIGHT_GRAY);
    WorldImage noLeft = new OverlayOffsetImage(new LineImage(new Posn(size4x4, 0), Color.BLACK),
            0, size4x4 / 2.0, base4x4).movePinhole(
            size4x4 / -2.0, size4x4 / -2.0);
    t.checkExpect(c2.renderCell(), noLeft);

    // c3 with no right wall
    c3.showTop = false;
    WorldImage noRight = new OverlayOffsetImage(new LineImage(new Posn(0, size3x3),
            Color.BLACK), size3x3 / 2.0, 0, base3x3).movePinhole(
            size3x3 / -2.0, size3x3 / -2.0);
    t.checkExpect(c3.renderCell(), noRight);

    // c4 with no walls
    c4.showLeft = false;
    c4.showTop = false;
    WorldImage none = base4x4.movePinhole(size4x4 / -2.0, size4x4 / -2.0);
    t.checkExpect(c4.renderCell(), none);

  }

  // SEARCH FILE

  // test search algorithm (both breadth and depth)
  void testSearch(Tester t) {
    this.initTestConditions();
    this.addPaths();

    // testing 1x1 maze search
    t.checkExpect(bfs1x1.seen, new ArrayList<Cell>(List.of(new Cell(new Posn(0, 0), 720))));
    t.checkExpect(dfs1x1.seen, new ArrayList<Cell>(List.of(new Cell(new Posn(0, 0), 720))));
    t.checkExpect(bfs1x1.cameFromEdge, new HashMap<Posn, Path>());
    t.checkExpect(dfs1x1.cameFromEdge, new HashMap<Posn, Path>());

    // test 2x2 maze (bfs/dfs)
    ArrayList<Cell> bfs2x2Seen = new ArrayList<Cell>(List.of(zeroZero2x2, oneZero2x2, zeroOne2x2));
    t.checkExpect(bfs2x2.seen, bfs2x2Seen);

    HashMap<Posn, Path> bfs2x2CameFromEdge = new HashMap<Posn, Path>();

    Cell cell1 = new Cell(new Posn(1, 0), 360);
    cell1.addPath(path2x23);
    Cell cell2 = new Cell(new Posn(0, 0), 360);
    cell2.addPath(path2x21);
    cell2.addPath(path2x22);
    Cell cell3 = new Cell(new Posn(0, 1), 360);
    cell3.addPath(path2x24);
    Cell cell4 = new Cell(new Posn(1, 1), 360);

    // 1,0 -> new Path(Cell:1, Cell:2)
    // 0,1 -> new Path(Cell:3, Cell:2)
    // 1,1 -> new Path(Cell:4, Cell:3)

    // Cell:1 -> 1,0 (outpath -> (1,0 -> 1,1 (54)))
    // Cell:2 -> 0,0 (outpath -> (0,0 -> 1,0 (21)))
    // Cell:3 -> 0,1 (outpath -> (0,1 -> 1,1 (29)))
    // Cell:4 -> 1,1 (outpath -> )

    bfs2x2CameFromEdge.put(new Posn(1, 0), new Path(cell1, cell2, 0));
    bfs2x2CameFromEdge.put(new Posn(0, 1), new Path(cell3, cell2, 0));
    bfs2x2CameFromEdge.put(new Posn(1, 1), new Path(cell4, cell3, 0));

    t.checkExpect(bfs2x2.cameFromEdge, bfs2x2CameFromEdge);

    t.checkExpect(dfs2x2.seen, List.of(zeroZero2x2, zeroOne2x2));

    t.checkExpect(dfs2x2.cameFromEdge, bfs2x2CameFromEdge);

    // testing 2x4 maze (if it visits the correct cells)
    t.checkExpect(bfs2x4.seen, List.of(one2x4, two2x4, five2x4, three2x4, six2x4,
            four2x4, seven2x4));
    t.checkExpect(dfs2x4.seen, List.of(one2x4, five2x4, six2x4, seven2x4));

  }

  // tests rendering light blue exploration path
  void testRenderExploration(Tester t) {
    this.initTestConditions();
    this.addPaths();

    ArrayList<Cell> search2x2Seen = search2x2.seen;
    ArrayList<Cell> search2x4Seen = search2x4.seen;

    // ensure cells are light gray
    for (Cell c : search2x2Seen) {
      t.checkExpect(c.clr, Color.LIGHT_GRAY);
    }
    for (Cell c : search2x4Seen) {
      t.checkExpect(c.clr, Color.LIGHT_GRAY);
    }

    search2x2.renderExploration(search2x2.seen);
    search2x4.renderExploration(search2x4.seen);

    // check color changed to light blue
    for (Cell c : search2x2Seen) {
      t.checkExpect(c.clr, new Color(176,224,230));
    }
    for (Cell c : search2x4Seen) {
      t.checkExpect(c.clr, new Color(176,224,230));
    }
  }

  // tests rendering light blue exploration path one at a time
  void testRenderExplorationSingle(Tester t) {
    this.initTestConditions();
    this.addPaths();
    // get seen lists
    ArrayList<Cell> search2x2seen = search2x2.seen;
    ArrayList<Cell> search2x4seen = search2x4.seen;

    Cell search2x2First = search2x2seen.get(0);
    Cell search2x4First = search2x4seen.get(0);

    // ensure cells are light gray
    t.checkExpect(search2x2First.clr, Color.LIGHT_GRAY);
    t.checkExpect(search2x4First.clr, Color.LIGHT_GRAY);

    t.checkExpect(search2x2.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), true);

    // make sure we only changed and removed the first element
    t.checkExpect(search2x2First.clr, new Color(176, 224, 230));
    t.checkExpect(search2x4First.clr, new Color(176, 224, 230));

    t.checkExpect(search2x2seen.get(1).clr, Color.LIGHT_GRAY);
    t.checkExpect(search2x4seen.get(1).clr, Color.LIGHT_GRAY);

    // remove all elements until false
    t.checkExpect(search2x2.renderExplorationSingle(), true);
    t.checkExpect(search2x2.renderExplorationSingle(), true);
    t.checkExpect(search2x2.renderExplorationSingle(), false);
    t.checkExpect(search2x4.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), true);
    t.checkExpect(search2x4.renderExplorationSingle(), false);
  }

  // tests rendering our solution
  void testRenderSolution(Tester t) {
    this.initTestConditions();
    this.addPaths();

    this.search2x2.createSolution();
    this.search2x2.renderSolution();
    this.search2x4.renderSolution();

    // solution values are dark blue, others are gray
    // because we are just rendering solution, not exploration
    t.checkExpect(zeroZero2x2.clr, new Color(0, 51, 102));
    t.checkExpect(oneZero2x2.clr, Color.LIGHT_GRAY);
    t.checkExpect(zeroOne2x2.clr, new Color(0, 51, 102));
    t.checkExpect(oneOne2x2.clr, Color.LIGHT_GRAY);

    t.checkExpect(one2x4.clr, new Color(0, 51, 102));
    t.checkExpect(two2x4.clr, Color.LIGHT_GRAY);
    t.checkExpect(three2x4.clr, Color.LIGHT_GRAY);
    t.checkExpect(four2x4.clr, Color.LIGHT_GRAY);
    t.checkExpect(five2x4.clr, new Color(0, 51, 102));
    t.checkExpect(six2x4.clr, new Color(0, 51, 102));
    t.checkExpect(seven2x4.clr, new Color(0, 51, 102));
    t.checkExpect(eight2x4.clr, Color.LIGHT_GRAY);
  }

  void testNumWrongMoves(Tester t) {
    this.initTestConditions();
    this.addPaths();
    t.checkExpect(this.search2x2.numWrongMoves(), 1);
    t.checkExpect(this.dfs2x4.numWrongMoves(), 0);
    t.checkExpect(this.bfs2x4.numWrongMoves(), 3);

  }

  // testing helper that adds paths to the cells in our grid (since we only want to do this for
  // some tests)
  void addPaths() {
    // add paths to cells
    zeroZero2x2.addPath(path2x21);
    zeroZero2x2.addPath(path2x22);
    oneZero2x2.addPath(path2x23);
    zeroOne2x2.addPath(path2x24);

    one2x4.addPath(path2x41);
    one2x4.addPath(path2x42);
    two2x4.addPath(path2x43);
    two2x4.addPath(path2x44);
    three2x4.addPath(path2x45);
    three2x4.addPath(path2x46);
    four2x4.addPath(path2x47);
    five2x4.addPath(path2x48);
    six2x4.addPath(path2x49);
    seven2x4.addPath(path2x410);

    // search with our added paths
    search2x2 = new Search(new Queue<>(), zeroZero2x2, oneOne2x2);
    search2x4 = new Search(new Queue<>(), one2x4, eight2x4);
    dfs2x2 = new Search(new Stack<>(), zeroZero2x2, oneOne2x2);
    bfs2x2 = new Search(new Queue<>(), zeroZero2x2, oneOne2x2);
    dfs2x4 = new Search(new Stack<>(), one2x4, eight2x4);
    bfs2x4 = new Search(new Queue<>(), one2x4, eight2x4);
    bfs1x1 = new Search(new Queue<>(), new Cell(new Posn(0, 0), 720),
            new Cell(new Posn(0, 0), 720));
    dfs1x1 = new Search(new Stack<>(), new Cell(new Posn(0, 0), 720),
            new Cell(new Posn(0, 0), 720));
  }

  // tests if we can get how far away a cell is
  void testHowFarFromCell(Tester t) {
    this.initTestConditions();
    this.addPaths();

    this.search2x2.howFarFromCell(4, zeroZero2x2);
    t.checkExpect(zeroZero2x2.clr, Color.LIGHT_GRAY);
    t.checkExpect(zeroOne2x2.clr, new Color(0, 0, 255));
    t.checkExpect(oneZero2x2.clr, new Color(0, 0, 255));
    t.checkExpect(oneOne2x2.clr, new Color(127, 0, 127));

    this.search2x4.howFarFromCell(8, one2x4);
    t.checkExpect(one2x4.clr, Color.LIGHT_GRAY);
    t.checkExpect(two2x4.clr, new Color(0, 0, 255));
    t.checkExpect(three2x4.clr, new Color(31, 0, 223));
    t.checkExpect(four2x4.clr, new Color(95, 0, 159));
    t.checkExpect(five2x4.clr, new Color(0, 0, 255));
    t.checkExpect(six2x4.clr, new Color(63, 0, 191));
    t.checkExpect(seven2x4.clr, new Color(127, 0, 127));
    t.checkExpect(eight2x4.clr, new Color(223, 0, 31));
  }

}


