import javalib.worldimages.Posn;

import java.util.ArrayList;
import java.util.HashMap;

// takes an arraylist and gets a minimum spanning tree
class Kruskal {

  private final HashMap<Posn, Posn> representatives;
  private final ArrayList<Path> worklist;

  // not final because we need to update the size of the graph once we've processed it
  private int sizeOfGraph;

  // EFFECT: mutates the list of paths to be in sorted weight order
  Kruskal(ArrayList<Path> list, ArrayList<ArrayList<Cell>> grid) {
    // sort list before start kruskals
    new Quicksort<Path>(list).quicksort();
    this.worklist = list;
    this.sizeOfGraph = 0;
    this.representatives = this.initializeHashMap(grid);
  }

  // creates a minimum spanning tree
  // EFFECT: removes every path in the worklist and processes it
  ArrayList<Path> createMST() {
    int i = 0;
    ArrayList<Path> pathsInTree = new ArrayList<Path>();
    while (i < sizeOfGraph - 1) {
      Path nextPath = worklist.remove(0);
      Cell x = nextPath.from;
      Cell y = nextPath.to;
      // We looked at the posn class and it has a .equals method
      // which tests for extentional equality
      if (!this.find(x.pos).equals(this.find(y.pos))) {
        pathsInTree.add(nextPath);
        this.union(find(x.pos), find(y.pos));
        i += 1;
      }
    }
    return pathsInTree;
  }

  // We did not test find and union
  // We didn't test it after talking with a TA, and we decided this was the best design decision
  // find and union should be private, because we don't want a user to be able to mess
  // with the representatives and give us an invalid mst. We also test find and union
  // in createMST();. We believed making it private was more important.

  // find the representative
  private Posn find(Posn pos) {
    if (!pos.equals(this.representatives.get(pos))) {
      return this.find(this.representatives.get(pos));
    }
    return pos;
  }

  // unions two representatives
  private void union(Posn pos1, Posn pos2) {
    this.representatives.put(pos1, pos2);
  }

  // initalizes every node's representative to itself
  // we made initalizeHashMap public because it does not mutate, so our data cannot
  // be messed with by invoking this method
  HashMap<Posn, Posn> initializeHashMap(ArrayList<ArrayList<Cell>> grid) {
    HashMap<Posn, Posn> tempMap = new HashMap<Posn, Posn>();
    // updates the size of our graph and initalizes every node's representative
    // to itself
    for (ArrayList<Cell> row : grid) {
      for (Cell cell : row) {
        this.sizeOfGraph += 1;
        tempMap.put(cell.pos, cell.pos);
      }
    }
    return tempMap;
  }

}
