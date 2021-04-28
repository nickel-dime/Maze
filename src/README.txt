Welcome to our maze game! We did some of the extra credit as well, and you can see all the controls below.
Note: we ommitted the public keyword from our methods and classes because the automatic grader said we need
javadoc, which we covered in class yet.
Note: You can find our examples and tests in the Maze.java file in ExamplesMaze

Extra Credit we did:
- Provide an option to toggle the viewing of the visited paths.
- Allow the user the ability to start a new maze without restarting the program.
- Keep the score of wrong moves — for either the automatic solutions or
    manual ones — and maybe keep statistics on which one of the
    two algorithms had fewer steps for each maze.
- In addition to animating the solution of the maze, also animate the construction of the maze: on each tick, show a single wall being knocked down.
- Color every square with a gradient of colors indicating how far it is from the start of the maze. E.g. red means very close to the start, and blue means very far.
- Color every square with a gradient of colors indicating how far it is from the exit of the maze. E.g. red means very close to the exit, and blue means very far. (Are these colors exactly the opposite of the previous ones, or is this a different color pattern altogether?)
- (Tricky) Construct mazes with a bias in a particular direction — a preference for horizontal or vertical corridors. (Hint: you might wish to play tricks with the edge weights here.)

CONTROLS:
"r" - Resets the maze and draws a new one
"h" - Resets the maze and draws a maze with only horizontal lines
"v" - Resets the maze and draws a maze with only vertical lines
"d" - Preforms a depth first search on the maze
"b" - Preforms a breadth first search on the maze
"up key" - Moves the player up if it can move there
"left key" - Moves the player left if it can move there
"down key" - Moves the player down if it can move there
"right key" - Moves the player right if it can move there
"p" - Toggles displaying the paths that either the search or the player has taken (the paths are the cells visited)
"c" - Clears the screen but keeps the current maze active
"t" - Toggles whether the displaying of the paths/optimal solution is immediate or is an animation
"s" - Makes a gradient which shows how far the cell is from the start
"e" - Makes a gradient which shows how far the cell is from the end
