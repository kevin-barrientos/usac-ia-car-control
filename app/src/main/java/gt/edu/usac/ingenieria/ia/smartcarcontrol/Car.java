package gt.edu.usac.ingenieria.ia.smartcarcontrol;

public class Car {

    // posible states
    private static final int NORTH = MazeCanvas.UP;
    private static final int WEST = MazeCanvas.LEFT;
    private static final int SOUTH = MazeCanvas.DOWN;
    private static final int EAST = MazeCanvas.RIGHT;

    // moves
    public static final int MOVE_FORWARD = 1;
    public static final int RIGTH = 2;
    public static final int LEFT = 3;
    public static final int MOVE_BACKWARD = 4;

    public static final int MODE_AUTOMATIC = 1;
    public static final int MODE_MANUAL = 2;

    private int mMode;

    private int currentState;

    public Car() {
        init();
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
     * Moves forward to the direction it is currently pointing
     * @return currentState {@link #NORTH} |  {@link #WEST} |  {@link #SOUTH} |  {@link #EAST} |
     */
    public int move(){
        return currentState;
    }

    /**
     * Moves to the opposite direction it is currently pointing
     * whith out changing the currentState.
     * @return opposite currentState {@link #NORTH} |  {@link #WEST} |  {@link #SOUTH} |  {@link #EAST} |
     */
    public int moveBack(){
        if(currentState == NORTH)
            return SOUTH;
        else if(currentState == WEST)
            return EAST;
        else if(currentState == SOUTH)
            return NORTH;
        else
            return WEST;
    }

    /**
     * Gets car's mode
     * @return {@link #MODE_AUTOMATIC} | {@link #MODE_MANUAL}
     */
    public int getMode() {
        return mMode;
    }

    /**
     * Sets car's mode. ({@link #MODE_AUTOMATIC} | {@link #MODE_MANUAL})
     * @param mode
     */
    public void setMode(int mode) {
        if(mode != MODE_AUTOMATIC && mode != MODE_MANUAL)
            return;
        this.mMode = mode;
    }

    public void init() {
        currentState = EAST;
        mMode = MODE_MANUAL;
    }
}
