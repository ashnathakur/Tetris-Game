package tetris;
import java.awt.*;
import java.awt.event.*;
import static java.lang.Math.*;
import static java.lang.String.format;
import java.util.*;
import javax.swing.*;
import static tetris.Config.*;

public class Tetris extends JPanel implements Runnable{
//implemented runnable for the instances of class to be run by threads and JPanel to make user friendly game
	enum Dir {
        right(1, 0), down(0, 1), left(-1, 0);
		//parameterized constructor
        Dir(int x, int y) {
            this.x = x;
            this.y = y;
        }
        final int x, y;
    };
 
    //these values to be assigned to the elements of the playing grid
    public static final int EMPTY = -1;
    public static final int BORDER = -2;
 
  //enum shape has been used as a data type to define falling shape
    Shape fallingShape; 
    Shape nextShape;
 
   // position of falling shape
    int fallingShapeRow;
    int fallingShapeCol;
 
    //initializing the playing grid
    final int[][] grid = new int[nRows][nCols];
 
    Thread fallingThread;// calling of the thread
    final Scoreboard scoreboard = new Scoreboard();//making object of the scoreboard class
    static final Random rand = new Random();//random class object
    
    
 //constructor of class tetris
    public Tetris() {
        setPreferredSize(dim);//specifying dimensions of the gaming window
        setBackground(bgColor);//background color of the gaming window
        setFocusable(true);//sets the focusable state of the gaming window
 
        initGrid();// calling the method initGrid
        selectShape();//calling the method select shape
 
        addMouseListener(new MouseAdapter() {
         @Override
            public void mousePressed(MouseEvent e) {
                //check whether game is over
            	if (scoreboard.isGameOver()) {
                    startNewGame();
                    repaint();
                }
            }
        });
     //Adds the specified key listener to receive key events from this component.
     //addKeyListener() of java.awt.Component
        addKeyListener(new KeyAdapter() {
            boolean fastDown;
           //invoked when a key is pressed
            @SuppressWarnings("deprecation")
			public void keyPressed(KeyEvent e) {
 
                if (scoreboard.isGameOver())
                    return;
                //getKeyCode to return integer value of the key pressed
                switch (e.getKeyCode()) {
                //constants for the key pressed are being used
                    case KeyEvent.VK_UP:
                        if (canRotate(fallingShape))
                            rotate(fallingShape);
                        break;
                    case KeyEvent.VK_W:
                        if (canRotate(fallingShape))
                            rotate(fallingShape);
                        break;
                    case KeyEvent.VK_A:
                        if (canMove(fallingShape, Dir.left))
                            move(Dir.left);
                        break;
 
                    case KeyEvent.VK_D:
                        if (canMove(fallingShape, Dir.right))
                            move(Dir.right);
                        break;
 
                    case KeyEvent.VK_S:
                        if (!fastDown) {
                            fastDown = true;
                            while (canMove(fallingShape, Dir.down)) {
                                move(Dir.down);
                                repaint();
                            }
                            shapeHasLanded();
                        }
                        break;
                    case KeyEvent.VK_P:
                    {Thread temp=fallingThread;
                    temp.suspend();
                    }	
                    	break;
                    case KeyEvent.VK_R:
                    {
                    	Thread temp=fallingThread;
                    	temp.resume();
                    }
                    	break;
                    case KeyEvent.VK_LEFT:
                        if (canMove(fallingShape, Dir.left))
                            move(Dir.left);
                        break;
 
                    case KeyEvent.VK_RIGHT:
                        if (canMove(fallingShape, Dir.right))
                            move(Dir.right);
                        break;
 
                    case KeyEvent.VK_DOWN:
                        if (!fastDown) {
                            fastDown = true;
                            while (canMove(fallingShape, Dir.down)) {
                                move(Dir.down);
                                repaint();
                            }
                            shapeHasLanded();
                        }
                }
                repaint();
            }
 
            @Override
            public void keyReleased(KeyEvent e) {
                fastDown = false;
            }
        });
    }
    //function for selecting shape
    void selectShape() {
        fallingShapeRow = 1;
        fallingShapeCol = 5;
        fallingShape = nextShape;
        Shape[] shapes = Shape.values();//array containing the blocks values
        nextShape = shapes[rand.nextInt(shapes.length)];//using random to get the block
        if (fallingShape != null)
            fallingShape.reset();//reseting the value of the falling shape
    }
    
    //starting the new game fucton by resetting all previous values
    void startNewGame() {
        stop();
        initGrid();
        selectShape();
        scoreboard.reset();
        (fallingThread = new Thread(this)).start();//starting a new thread to intitalize falling of another block
    }
 // to stop the game
    void stop() {
        if (fallingThread != null) {
            Thread tmp = fallingThread;
            fallingThread = null;
            tmp.interrupt();//to stop running of previous thread
        }
    }
 
    void initGrid() {
        for (int r = 0; r < nRows; r++) {
            Arrays.fill(grid[r], EMPTY);//giving value -1 to all the elements of the array or playing grid row wise.
    //Arrays.fill(a,value): Assigns the specified int value to each element of the specified array of integers.
            for (int c = 0; c < nCols; c++) {
                if (c == 0 || c == nCols - 1 || r == nRows - 1)
                    grid[r][c] = BORDER;//assigning value -2 to the border of the playing grid.
            }
        }
    }
 
    @Override
    public void run() {
 
        while (Thread.currentThread() == fallingThread) {
        	//Returns a reference to the currently executing thread object.
            try {
                Thread.sleep(500);//Causes the currently executing thread to sleep
            } catch (InterruptedException e) {
                return;
            }
            	if (!scoreboard.isGameOver()) {
                if (canMove(fallingShape, Dir.down)) {
                    move(Dir.down);
                } else {
                    shapeHasLanded();
                }
                repaint();//repaint the component
            }
        }
    }
/*This Graphics2D class extends the Graphics class to provide more sophisticated control over geometry, coordinate 
transformations, color management,and text layout.*/
    void drawStartScreen(Graphics2D g) {
        g.setFont(mainFont);
        
        //Fills the interior of a Shape 
        g.setColor(titlebgColor);
        g.fill(titleRect);//creating block for title tetris
        g.fill(clickRect);//creating block for clicking to start the game
 
        g.setColor(textColor);
        g.drawString("Tetris", titleX, titleY);//writing the title
 
        g.setFont(smallFont);
        g.drawString("click to start", clickX, clickY);//writing the text for clicking
    }
 
    //function for creating the matrices
    void drawSquare(Graphics2D g, int colorIndex, int r, int c) {
        g.setColor(colors[colorIndex]);//setting the color to the matrix using the index value
        g.fillRect(leftMargin + c * blockSize, topMargin + r * blockSize,
                blockSize, blockSize);
//The resulting rectangle covers an area width pixels wide by height pixels tall.
        
        g.setStroke(smallStroke);//the area between the border
        g.setColor(squareBorder);//border of the matrics
        g.drawRect(leftMargin + c * blockSize, topMargin + r * blockSize,
                blockSize, blockSize);
    }
 
    void drawUI(Graphics2D g) {
        // grid background
        g.setColor(gridColor);
        g.fill(gridRect);
 
        // the blocks dropped in the grid
        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {
                int idx = grid[r][c];
                if (idx > EMPTY)//checking if the space is empty
                    drawSquare(g, idx, r, c);
            }
        }
 
        // the borders of grid and preview panel
        g.setStroke(largeStroke);//grid borders
        g.setColor(gridBorderColor);
        g.draw(gridRect);
       // g.draw(previewRect);
 
        // scoreboard
        int x = scoreX;
        int y = scoreY;
        g.setColor(textColor);
        g.setFont(smallFont);
        g.drawString(format("HIGHSCORE  %d", scoreboard.getTopscore()), x, y);
        g.drawString(format("LEVEL      %d", scoreboard.getLevel()), x, y + 30);
        g.drawString(format("SCORE      %d", scoreboard.getScore()), x, y + 60);
 
        
    }
    
 
    void drawFallingShape(Graphics2D g) {
        int idx = fallingShape.ordinal();//the position in its enum declaration, 
        for (int[] p : fallingShape.pos)
            drawSquare(g, idx, fallingShapeRow + p[1], fallingShapeCol + p[0]);
    }
 
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);//painting the falling block
        Graphics2D g = (Graphics2D) gg;
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
               // RenderingHints.VALUE_ANTIALIAS_ON);
 
        drawUI(g);//calling the function for 
 
        if (scoreboard.isGameOver()) {
            drawStartScreen(g);
        } else {
            drawFallingShape(g);
        }
    }
 
    //for rotation of tetris
    boolean canRotate(Shape s) {
        if (s == Shape.Square)
            return false;
 
        int[][] pos = new int[4][2];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = s.pos[i].clone();
        }
 
        for (int[] row : pos) {
            int tmp = row[0];
            row[0] = row[1];
            row[1] = -tmp;
        }
 
        for (int[] p : pos) {
            int newCol = fallingShapeCol + p[0];
            int newRow = fallingShapeRow + p[1];
            if (grid[newRow][newCol] != EMPTY)//check if border has been reached 
            	{
                return false;
            }
        }
        return true;
    }
 
    void rotate(Shape s) {
        if (s == Shape.Square)
            return;
 
        for (int[] row : s.pos) {
            int tmp = row[0];
            row[0] = row[1];
            row[1] = -tmp;
        }
    }
 //updating the changes in the left rows and columns left for adding the matrices
    void move(Dir dir) {
        fallingShapeRow += dir.y;
        fallingShapeCol += dir.x;
    }
 
    boolean canMove(Shape s, Dir dir) {
        for (int[] p : s.pos) {
            int newCol = fallingShapeCol + dir.x + p[0];
            int newRow = fallingShapeRow + dir.y + p[1];
            if (grid[newRow][newCol] != EMPTY)//reached the border that is no more tile can be adjusted
                return false;
        }
        return true;
    }
 
    void shapeHasLanded() {
        addShape(fallingShape);
        //
        if (fallingShapeRow < 2) {
            scoreboard.setGameOver();
            scoreboard.setTopscore();
            stop();
        } else {
            scoreboard.addLines(removeLines());
        }
        selectShape();
    }
 //remove the filled lines
    int removeLines() {
        int count = 0;
        for (int r = 0; r < nRows - 1; r++) {
            for (int c = 1; c < nCols - 1; c++) {
                if (grid[r][c] == EMPTY)
                    break;
                if (c == nCols - 2) {
                    count++;
                    removeLine(r);
                }
            }
        }
        return count;
    }
 
    void removeLine(int line) {
        for (int c = 0; c < nCols; c++)
            grid[line][c] = EMPTY;
 
        for (int c = 0; c < nCols; c++) {
            for (int r = line; r > 0; r--)
                grid[r][c] = grid[r - 1][c];
        }
    }
 
    void addShape(Shape s) {
        for (int[] p : s.pos)
            grid[fallingShapeRow + p[1]][fallingShapeCol + p[0]] = s.ordinal();
    }
 
    public static void main(String[] args) {
       
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//Exit the application using the System exit method
            f.setTitle("Tetris Game");//gives title to the frame         
            f.add(new Tetris(), BorderLayout.CENTER);
            f.pack(); // Causes this Window to be sized to fit the preferred size and layouts of its subcomponents.
            f.setLocationRelativeTo(null);//the gaming window is placed in the center of the screen
            f.setVisible(true);//makes the window visible
    }
}
class Scoreboard {
    static final int MAXLEVEL = 9;
 
    private int level;
    private int lines;
    private int score;
    private int topscore;
    private boolean gameOver = true;//variable for checking the status of game
 
    void reset() {
        setTopscore();
        level = lines = score = 0;
        gameOver = false;
    }
 
    void setGameOver() {
        gameOver = true;
    }
 
    boolean isGameOver() {
        return gameOver;
    }
 
    void setTopscore() {
        if (score > topscore)
            topscore = score;
    }
 
    int getTopscore() {
        return topscore;
    }
 
    void addScore(int sc) {
        score += sc;
    }
 
    void addLines(int line) {
 
        switch (line) {
            case 1:
                addScore(10);
                break;
            case 2:
                addScore(20);
                break;
            case 3:
                addScore(30);
                break;
            case 4:
                addScore(40);
                break;
            default:
                return;
        }
 
        lines += line;
        if (lines > 10)
            addLevel();
    }
 
    void addLevel() {
        lines %= 10;
        if (level < MAXLEVEL)
            level++;
    }
 
    int getLevel() {
        return level;
    }
 
    int getLines() {
        return lines;
    }
 
    int getScore() {
        return score;
    }
}
enum Shape {
    ZShape(new int[][]{{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}),
    SShape(new int[][]{{0, -1}, {0, 0}, {1, 0}, {1, 1}}),
    IShape(new int[][]{{0, -1}, {0, 0}, {0, 1}, {0, 2}}),
    TShape(new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}),
    Square(new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}),
    LShape(new int[][]{{-1, -1}, {0, -1}, {0, 0}, {0, 1}}),
    JShape(new int[][]{{1, -1}, {0, -1}, {0, 0}, {0, 1}});
 
    private Shape(int[][] shape) {
        this.shape = shape;
        pos = new int[4][2];
        reset();
    }
 
    void reset() {
        for (int i = 0; i < pos.length; i++) {
            pos[i] = shape[i].clone();
        }
    }
 
    final int[][] pos, shape;
}


final class Config {
    final static Color[] colors = {Color.green, Color.red, Color.blue,
        Color.pink, Color.orange, Color.cyan, Color.magenta};
 
    final static Font mainFont = new Font("Monospaced", Font.BOLD, 48);//object of class font
    final static Font smallFont = mainFont.deriveFont(Font.BOLD, 18);
//Creates a new Font object by replicating this Font object and applying a new style and size.
//Constructs a Dimension and initializes it to the specified width and specified height.   
    final static Dimension dim = new Dimension(640, 640);
 
/*Constructs a new Rectangle whose upper-left corner is specified as (x,y) and whose width and height are specified 
 by the arguments of the same name.*/
    final static Rectangle gridRect = new Rectangle(46, 47, 308, 517);//gaming area
    final static Rectangle previewRect = new Rectangle(387, 47, 200, 200);//preview rectangle
    final static Rectangle titleRect = new Rectangle(70, 150, 252, 100);//title rectangle
    final static Rectangle clickRect = new Rectangle(80, 375, 252, 40);//click rectangle
 
    final static int blockSize = 30;
    final static int nRows = 18;
    final static int nCols = 12;
    final static int topMargin = 50;
    final static int leftMargin = 20;
    final static int scoreX = 400;
    final static int scoreY = 330;
    final static int titleX = 100;//margin from x axis of the title
    final static int titleY = 210;//margin from the y axis of the title
    final static int clickX = 120;//margin from the x axis of the click text
    final static int clickY = 400;//margin from the y axis of the click text
    final static int previewCenterX = 467;
    final static int previewCenterY = 97;
    
    //The BasicStroke class defines a basic set of rendering attributes for the outlines of graphics primitives,
    final static Stroke largeStroke = new BasicStroke(6);
    final static Stroke smallStroke = new BasicStroke(2);
    
 //The Color class is used to encapsulate colors in the defaultsRGB color space comes under java.awt.* package
    final static Color squareBorder = Color.white;//color given to the border of the upcoming tile shown
    final static Color titlebgColor = Color.white;
    final static Color textColor = Color.black;
    final static Color bgColor = new Color(0xF0F8FF);//parameterized constructor of the color class called
    final static Color gridColor = new Color(0xC0C0C0);
    final static Color gridBorderColor = new Color(0x7788AA);
}

/* Game can work both by using the A,S,D keys or the left,right,down arrow keys */