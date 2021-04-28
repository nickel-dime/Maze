// represents an path between two cells
class Path implements Comparable<Path> {

  // these need to be public because in order
  // to create a tree of paths we need to know
  // the cells that the paths are made up of
  final Cell from;
  final Cell to;

  private final int weight;

  Path(Cell from, Cell to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  Path(Cell from, Cell to) {
    this(from, to, 0);
  }

  // compares two paths by weight
  // public because kruskal's needs to be able to sort paths by weight
  public int compareTo(Path o) {
    return this.weight - o.weight;
  }

  // (We start with a grid of cells with no connections, and as we remove a wall between two cells,
  // we add connections between those cells)

  // removes a wall with this path
  // public so our maze can remove walls
  // EFFECT: adds the appropriate connections between cells
  void removeWall() {
    String dir = from.direction(to.pos);
    // connect cells to each other
    from.addPath(this);
    // and allow paths both ways between two cells
    to.addPath(new Path(to, from));
    if (dir.equals("u")) {
      from.showTop = false;
    } else if (dir.equals("d")) {
      to.showTop = false;
    } else if (dir.equals("l")) {
      from.showLeft = false;
    } else if (dir.equals("r")) {
      to.showLeft = false;
    }
  }
}

