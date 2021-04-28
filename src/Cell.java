import javalib.worldimages.*;

import java.awt.*;
import java.util.ArrayList;

// represents a cell in a maze

class Cell {

  // the paths this cell has
  // not private because outside classes should know where a cell is in a maze (e.g. bfs and dfs)
  final ArrayList<Path> outPaths;

  // the position in the maze of this cell
  // not private because we use it as a unique identifier and outside classes
  // should be able to know a cells id (e.g. kruskal)
  final Posn pos;

  // side length of this cell
  private final int size;

  // color of this cell
  // not final because we change it based on if it this cell is used in bfs/dfs
  // not private so the maze can access the cells color
  Color clr;

  // not final because the state of showing walls is being changed throughout creating
  // the maze, and not final because other classes need access to change them based
  // on external circumstances
  boolean showTop;
  boolean showLeft;

  private final LineImage top;
  private final LineImage left;

  // cell with path to other cells yet to be created
  Cell(Posn pos, int size) {
    this.pos = pos;
    new Posn(2, 3);
    this.outPaths = new ArrayList<Path>();
    this.size = size;
    this.top = new LineImage(new Posn(this.size, 0), Color.BLACK);
    this.left = new LineImage(new Posn(0, this.size), Color.BLACK);
    this.showTop = true;
    this.showLeft = true;
    this.clr = Color.LIGHT_GRAY;
  }

  // EFFECT: adds a path to this cell
  public void addPath(Path out) {
    this.outPaths.add(out);
  }

  // renders this cell
  WorldImage renderCell() {
    RectangleImage base = new RectangleImage(this.size, this.size,
            OutlineMode.SOLID,
            this.clr);
    if (this.showTop && this.showLeft) {
      return new OverlayOffsetImage(this.top, 0, this.size / 2.0, new OverlayOffsetImage(this.left,
              this.size / 2.0, 0, base)).movePinhole(this.size / -2.0, this.size / -2.0);
    } else if (this.showTop) {
      return new OverlayOffsetImage(top, 0, this.size / 2.0, base).movePinhole(
              this.size / -2.0, this.size / -2.0);
    } else if (this.showLeft) {
      return new OverlayOffsetImage(this.left, this.size / 2.0, 0, base).movePinhole(
              this.size / -2.0, this.size / -2.0);
    } else {
      return base.movePinhole(this.size / -2.0, this.size / -2.0);
    }
  }

  // determines which direction the passed cell is in relation to this cell
  // "r" is right / "u" is up / "d" is down / "l" is left
  // and throws an exception if the cells are not directly adjacent
  String direction(Posn other) {
    int xDiff = this.pos.x - other.x;
    int yDiff = this.pos.y - other.y;
    if (xDiff != 0 && yDiff != 0) {
      throw new RuntimeException("cannot get direction between two non-adjacent cells");
    }
    if (xDiff == 1) {
      return "l";
    } else if (xDiff == -1) {
      return "r";
    } else if (yDiff == 1) {
      return "u";
    } else if (yDiff == -1) {
      return "d";
    } else { // the case where its in the same row/column, but more than 1 space away
      throw new RuntimeException("the passed in cell is two far in one direction");
    }
  }

  // can the player move from this cell to the other
  boolean canMove(Cell other) {
    // for each path in the other's cell
    for (Path p : this.outPaths) {
      // cell equality is intentional
      if (p.to.equals(other)) {
        return true;
      }
    }
    return false;
  }

}
