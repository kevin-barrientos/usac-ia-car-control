package gt.edu.usac.ingenieria.ia.smartcarcontrol;

public class Car {

    // posible states
    private static final int NORTH = MazeCanvas.UP;
    private static final int WEST = MazeCanvas.LEFT;
    private static final int SOUTH = MazeCanvas.DOWN;
    private static final int EAST = MazeCanvas.RIGHT;

    public static final int RIGTH = 1;
    public static final int LEFT = 2;

    private int currentState;

    public Car() {
        currentState = EAST;
    }

    /**
     * Change car state
     * @param direction {@link #NORTH} | {@link #WEST} | {@link #SOUTH} | {@link #EAST}
     */
    public void turn(int direction) {
        if(direction != RIGTH && direction != LEFT)
            return;
        switch (currentState){
            case NORTH:
                currentState = direction == LEFT ? WEST : EAST;
                break;
            case WEST:
                currentState = direction == LEFT ? SOUTH : NORTH;
                break;
            case SOUTH:
                currentState = direction == LEFT ? EAST : WEST;
                break;
            case EAST:
                currentState = direction == LEFT ? NORTH : SOUTH;
                break;
        }
    }

    /**
     * Determines wich way to move
     * @return currentState
     */
    public int move(){
        return currentState;
    }
}
