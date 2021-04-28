import javalib.worldimages.Posn;

import java.awt.*;
import java.util.HashMap;
import java.util.ArrayList;


// searches a graph to find a path between the start and end cells
class Search {

  // not private because when we're animating our maze, we need the solution which is
  // contained in cameFromEdge
  final HashMap<Posn, Path> cameFromEdge;

  private final ICollection<Cell> worklist;

  // not private because when we're animating our maze, we need the visited cells
  // which are contained in seen
  final ArrayList<Cell> seen;

  // not final because we increment index when we render a seen cell
  private int seenIndex;

  private final Cell start;
  private final Cell end;

  // not private so maze can see the solution
  final ArrayList<Cell> solution;

  Search(ICollection<Cell> worklist, Cell start, Cell end) {
    this.cameFromEdge = new HashMap<Posn, Path>();
    this.worklist = worklist;
    this.seen = new ArrayList<Cell>();
    this.seenIndex = 0;
    this.start = start;
    this.end = end;
    this.solution = new ArrayList<Cell>();
    this.search();
  }

  // searches with either bfs or dfs, depending on collection
  // public because we want to search in the maze
  // EFFECT: updates our search paths and visited cells
  void search() {
    this.worklist.add(start);
    while (!this.worklist.isEmpty()) {
      Cell next = this.worklist.remove();
      // we are using intentional equality
      if (next.equals(end)) {
        this.createSolution();
        return;
      }
      else if (this.seen.contains(next)) {
        // discarding it
      } else {
        // all of next's neighboring paths
        for (Path n : next.outPaths) {
          if (!this.seen.contains(n.to)) {
            this.worklist.add(n.to);
            this.cameFromEdge.put(n.to.pos, new Path(n.to, next));
          }
        }
        this.seen.add(next);
      }
    }
  }

  // EFFECT: renders each cell to show how far away from the "away cell" it is
  // public so our maze can see how far cells are from the away ycell
  void howFarFromCell(double size, Cell away) {
    double diff = 255.0 / size;
    double red = 0;
    double blue = 255;
    ICollection<Cell> newWorklist = new Queue<>();
    ArrayList<Cell> newSeen = new ArrayList<Cell>();
    newWorklist.add(away);
    while (!newWorklist.isEmpty()) {
      Cell next = newWorklist.remove();

      if (newSeen.contains(next)) {
        // discarding it
      } else {
        // all of next's neighboring paths
        for (Path n : next.outPaths) {
          if (!newSeen.contains(n.to)) {
            newWorklist.add(n.to);
            // casting makes sense because it's essentially getting the floor of the value
            // and we know that red/blue is a double, so it is a safe cast
            // we did not use Math.floor because it returns a double, thus it is useless
            n.to.clr = new Color((int) red, 0, (int) blue);
          }
        }
        newSeen.add(next);
      }
      red = red + diff;
      blue = blue - diff;
    }
  }

  // public because we want to centrally solve the maze using the fields from this class
  // EFFECT: Changes the color of the cells to produce the solution
  void renderExploration(ArrayList<Cell> seenCells) {
    // fields of search will contain the values we want
    // updates our seen nodes with a light blue color
    for (Cell c : seenCells) {
      c.clr = new Color(176,224,230);
    }
  }


  // searches the first in seen and removes it, for ontick (one at a time)
  // and returns true if it's still rendering exploration
  // public because we need to use it in maze for ontick
  // EFFECT: removes cell from seen list
  boolean renderExplorationSingle() {
    if (this.seenIndex < this.seen.size()) {
      Cell c = this.seen.get(this.seenIndex);
      c.clr = new Color(176, 224, 230);
      this.seenIndex += 1;
      return true;
    }
    return false;
  }

  // creates the solution from our search's hashmap
  // public because we render solution during ontick
  void createSolution() {
    this.createSolutionHelp(end);
  }

  // creates the solution from our search's hashmap
  // EFFECT: makes the solution visible through the node's colors️
  private void createSolutionHelp(Cell solved) {
    if (!solved.equals(this.start)) {
      Path curPath = this.cameFromEdge.get(solved.pos);
      this.solution.add(0, curPath.to);
      this.createSolutionHelp(curPath.to);
    }
  }

  // creates the solution from our search's hashmap
  // public because we render solution during ontick
  void renderSolution() {
    for (Cell solCell : this.solution) {
      solCell.clr = new Color(0, 51, 102);
    }
  }

  // counts number of wrong moves so far for a search
  // public because we need to use this to display the number of wrong moves of a search
  int numWrongMoves() {
    int numWrongMoves = 0;
    for (Cell c : this.seen) {
      if (!this.solution.contains(c)) {
        numWrongMoves += 1;
      }
    }
    return numWrongMoves;
  }

}



