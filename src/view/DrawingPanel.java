package view;

import control.SettingConstants;
import control.util.Ultility;
import control.myawt.SKPoint2D;
import control.myawt.SKPoint3D;
import model.shape2d.Rectangle;
import model.shape2d.Segment2D;
import model.shape2d.Triangle;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Stack;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.shape2d.Arrow2D;
import model.shape2d.Diamond;
import model.shape2d.Ellipse;
import model.shape2d.Line2D;
import model.shape2d.Shape2D;
import model.shape2d.Star;
import model.shape2d.animation.Ground;
import model.shape2d.animation.Smoke;
import model.shape2d.animation.Volcano;
import model.shape3d.Cylinder;
import model.shape3d.Rectangular;
import model.tuple.MyPair;

/**
 * Class used for implementing drawing area.
 */
public class DrawingPanel extends JPanel {

    /**
     * The actual width of board.
     */
    public final int widthBoard = SettingConstants.WIDTH_DRAW_AREA;

    /**
     * The actual height of board.
     */
    public final int heightBoard = SettingConstants.HEIGHT_DRAW_AREA;

    // Color and coord property of all points when any drawing action is applied
    /**
     * Color of each pixel after applying drawing action.
     */
    public Color[][] colorOfBoard;

    /**
     * Coordination text of each pixel after applying drawing action.
     */
    private String[][] coordOfBoard;

    // The color property of all points when a new drawing action is happened.
    private Color[][] changedColorOfBoard;
    private String[][] changedCoordOfBoard;

    // This array is used to mark the point is changed in an action.
    private boolean[][] markedChangeOfBoard;

    // Recent drawn shape 2d
    private Shape2D recentShape;

    /**
     * Undo stack of coordinate
     */
    private static Stack<String[][]> undoCoordOfBoardStack;
    private static Stack<Color[][]> undoColorOfBoardStack;
    private static Stack<String[][]> redoCoordOfBoardStack;
    private static Stack<Color[][]> redoColorOfBoardStack;

    /**
     * Showing grid lines flag.
     */
    private boolean showGridFlag;

    /**
     * Showing coordinate flag.
     */
    private boolean showCoordinateFlag;

    /**
     * Starting point of drawn object.
     */
    private SKPoint2D startDrawingPoint;

    /**
     * Ending point of drawn object.
     */
    private SKPoint2D endDrawingPoint;

    /**
     * User selected coordinate system.
     */
    private SettingConstants.CoordinateMode coordinateMode;

    /**
     * Selected option by user.
     */
    private SettingConstants.DrawingToolMode selectedToolMode;

    /**
     * User selected color.
     */
    private Color selectedColor;

    /**
     * User customized line style.
     */
    private SettingConstants.LineStyle selectedLineStyle;

    /**
     * User customized line size.
     */
    private Integer selectedLineSize;
    private SKPoint2D Polygon_previousPoint;
    private SKPoint2D Polygon_firstPoint;
    private boolean firstTime = true;

    private SKPoint2D eraserPos = new SKPoint2D();
    private boolean eraserIsSelected;

    public DrawingPanel() {
        this.colorOfBoard = new Color[heightBoard][widthBoard];
        this.coordOfBoard = new String[heightBoard][widthBoard];

        this.changedColorOfBoard = new Color[heightBoard][widthBoard];
        this.changedCoordOfBoard = new String[heightBoard][widthBoard];

        this.markedChangeOfBoard = new boolean[heightBoard][widthBoard];

        undoCoordOfBoardStack = new Stack<>();
        undoColorOfBoardStack = new Stack<>();
        redoCoordOfBoardStack = new Stack<>();
        redoColorOfBoardStack = new Stack<>();

        showGridFlag = SettingConstants.DEFAULT_VISUAL_SHOW_GRID;
        showCoordinateFlag = SettingConstants.DEFAULT_VISUAL_SHOW_COORDINATE;

        coordinateMode = SettingConstants.CoordinateMode.MODE_2D;

        startDrawingPoint = new SKPoint2D(SettingConstants.DEFAULT_UNUSED_POINT);
        endDrawingPoint = new SKPoint2D(SettingConstants.DEFAULT_UNUSED_POINT);

        selectedToolMode = SettingConstants.DrawingToolMode.DRAWING_LINE_SEGMENT;
        selectedColor = SettingConstants.DEFAULT_FILL_COLOR;
        selectedLineStyle = SettingConstants.LineStyle.DEFAULT;
        selectedLineSize = SettingConstants.DEFAULT_LINE_SIZE;

        recentShape = null;

        resetChangedPropertyArray();
        resetSavedPropertyArray();

        this.addMouseMotionListener(new CustomMouseMotionHandling());
        this.addMouseListener(new CustomMouseClickHandling());
    }

    public Color[][] getColorOfBoard() {
        return this.colorOfBoard;
    }

    public String[][] getCoordOfBoard() {
        return this.coordOfBoard;
    }

    public boolean isEmpty() {
        boolean result = true;
        for (int i = 0; i < heightBoard; i++) {
            for (int j = 0; j < widthBoard; j++) {
                if (!colorOfBoard[i][j].equals(SettingConstants.DEFAULT_PIXEL_COLOR)) {
                    result = false;
                    break;
                }
            }
            if (!result) {
                break;
            }
        }
        return result;
    }

    public void setStartDrawingPoint(int coordX, int coordY) {
        this.startDrawingPoint.setLocation(coordX, coordY);
    }

    public void setEndDrawingPoint(int coordX, int coordY) {
        this.endDrawingPoint.setLocation(coordX, coordY);
    }

    /**
     * Check for undo action.
     *
     * @return
     */
    public boolean ableUndo() {
        return !undoColorOfBoardStack.empty();
    }

    /**
     * Check for redo action.
     *
     * @return
     */
    public boolean ableRedo() {
        return !redoColorOfBoardStack.empty();
    }

    /**
     * Set selected drawing tool button.
     *
     * @param selectedToolMode
     */
    public boolean setSelectedToolMode(SettingConstants.DrawingToolMode selectedToolMode) {
        if ((selectedToolMode == SettingConstants.DrawingToolMode.DRAWING_TRANSFORM_ROTATION)
                || (selectedToolMode == SettingConstants.DrawingToolMode.DRAWING_TRANSFORM_SYMMETRY)) {
            if (recentShape == null) {
                JOptionPane.showMessageDialog(this.getParent(), "Draw your object before using transformation!");
                return false;
            }
        }

        this.selectedToolMode = selectedToolMode;
        return true;
    }

    /**
     * Get current drawing tool button.
     *
     * @return
     */
    public SettingConstants.DrawingToolMode getSelectedToolMode() {
        return this.selectedToolMode;
    }

    /**
     * Set selected line size.
     *
     * @param lineSize
     */
    public void setSelectedLineSize(int lineSize) {
        selectedLineSize = lineSize;
    }

    /**
     * Set selected line style.
     *
     * @param lineStyle
     */
    public void setSelectedLineStyle(SettingConstants.LineStyle lineStyle) {
        selectedLineStyle = lineStyle;
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }

    /**
     * Change coordinate system for the board. <br>
     * Release all buffered memory and clear the board.
     *
     * @param mode
     */
    public void setCoordinateMode(SettingConstants.CoordinateMode mode) {
        // Clear old coordinate system before changing coordinate mode flag
        this.coordinateMode = mode;

        resetChangedPropertyArray();
        resetSavedPropertyArray();
        disposeStack();

        showGridFlag = SettingConstants.DEFAULT_VISUAL_SHOW_GRID;
        showCoordinateFlag = SettingConstants.DEFAULT_VISUAL_SHOW_COORDINATE;

        this.repaint();
    }

    /**
     * Release all resource in undo, redo stack.
     */
    private void disposeStack() {
        redoColorOfBoardStack.clear();
        redoCoordOfBoardStack.clear();
        undoColorOfBoardStack.clear();
        undoCoordOfBoardStack.clear();
    }

    /**
     * Clear all drawn object in board by setting its default color and
     * coordinate value. <br>
     * This method doesn't release memory in stack. You can use disposeStack
     * method for this purpose.
     */
    public void resetSavedPropertyArray() {
        for (int i = 0; i < this.heightBoard; i++) {
            for (int j = 0; j < this.widthBoard; j++) {
                colorOfBoard[i][j] = SettingConstants.DEFAULT_PIXEL_COLOR;
                coordOfBoard[i][j] = null;
            }
        }
    }

    /**
     * Clear all buffered drawn object in board. <br>
     */
    public void resetChangedPropertyArray() {
        for (int i = 0; i < this.heightBoard; i++) {
            for (int j = 0; j < this.widthBoard; j++) {
                markedChangeOfBoard[i][j] = false;
                changedColorOfBoard[i][j] = SettingConstants.DEFAULT_PIXEL_COLOR;
                changedCoordOfBoard[i][j] = null;
            }
        }
        if (getSelectedToolMode() != SettingConstants.DrawingToolMode.DRAWING_POLYGON_FREE) {
            firstTime = true;
            Polygon_firstPoint = null;
            Polygon_previousPoint = null;
        }

//        if (getSelectedToolMode() != SettingConstants.DrawingToolMode.TOOL_ERASER){
//            eraserIsSelected = false;
//        }
    }

    public void setShowGridLinesFlag(boolean flag) {
        showGridFlag = flag;
        this.repaint();
    }

    public void setShowCoordinateFlag(boolean flag) {
        showCoordinateFlag = flag;
        this.repaint();
    }

    /**
     * Copy color value of each pixel from <code>color_board_from</code> to
     * <code>color_board_to</code>. <br>
     * Require they have the same size.
     *
     * @param color_board_from
     * @param color_board_to
     */
    public void copyColorValue(Color[][] color_board_from, Color[][] color_board_to, boolean fromColorOBToChangedCOB) {
        int height = color_board_from.length / SettingConstants.RECT_SIZE;
        int width = color_board_from[0].length / SettingConstants.RECT_SIZE;
        if (!fromColorOBToChangedCOB) {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    //  markedChangeOfBoard[row][col] = true;
                    color_board_to[row][col] = new Color(color_board_from[row][col].getRGB());
                }
            }
        } else {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    markedChangeOfBoard[row][col] = true;
                    color_board_to[row][col] = new Color(color_board_from[row][col].getRGB());
                }
            }
        }

    }

    /**
     * Copy coordinate value of each pixel from <code>coord_board_from</code> to
     * <code>coord_board_to</code>. <br>
     * Require they have the same size.
     *
     * @param coord_board_from
     * @param coord_board_to
     */
    public void mergeCoordValue(String[][] coord_board_from, String[][] coord_board_to) {
        int height = coord_board_from.length / SettingConstants.RECT_SIZE;
        int width = coord_board_from[0].length / SettingConstants.RECT_SIZE;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (coord_board_from[row][col] != null) {
                    coord_board_to[row][col] = coord_board_from[row][col];
                }
            }
        }
    }

    public void copyCoordValue(String[][] coord_board_from, String[][] coord_board_to) {
        int height = coord_board_from.length / SettingConstants.RECT_SIZE;
        int width = coord_board_from[0].length / SettingConstants.RECT_SIZE;

        for (int row = 0; row < height; row++) {
            System.arraycopy(coord_board_from[row], 0, coord_board_to[row], 0, width);
        }
    }

    /**
     * Save the current color of drawing board to undo stack.
     */
    private void saveCurrentColorBoardToUndoStack() {
        Color[][] tempBoard = new Color[heightBoard][widthBoard];
        copyColorValue(colorOfBoard, tempBoard, false);
        undoColorOfBoardStack.push(tempBoard);
    }

    /**
     * Save the current coordinates of drawing board to undo stack.
     */
    private void saveCurrentCoordBoardToUndoStack() {
        String[][] tempBoard = new String[heightBoard][widthBoard];
        copyCoordValue(coordOfBoard, tempBoard);
        undoCoordOfBoardStack.push(tempBoard);
    }

    /**
     * Save the current color of drawing board to redo stack.
     */
    private void saveCurrentColorBoardToRedoStack() {
        Color[][] tempBoard = new Color[heightBoard][widthBoard];
        copyColorValue(colorOfBoard, tempBoard, false);
        redoColorOfBoardStack.push(tempBoard);
    }

    /**
     * Save the current coordinates of drawing board to redo stack.
     */
    private void saveCurrentCoordBoardToRedoStack() {
        String[][] tempBoard = new String[heightBoard][widthBoard];
        copyCoordValue(coordOfBoard, tempBoard);
        redoCoordOfBoardStack.push(tempBoard);
    }

    private void hidePixels(SKPoint2D hidePixelsPos, boolean dragged) {
        if (!dragged) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    hidePixelsPos.setLocation(eraserPos.getCoordX() + i, eraserPos.getCoordY() + j);
                    if (Ultility.checkValidPoint(changedColorOfBoard, hidePixelsPos.getCoordX(), hidePixelsPos.getCoordY())) {
                        changedColorOfBoard[(int) hidePixelsPos.getCoordY()][(int) hidePixelsPos.getCoordX()] = SettingConstants.DEFAULT_EMPTY_BACKGROUND_COLOR;
                        markedChangeOfBoard[(int) hidePixelsPos.getCoordY()][(int) hidePixelsPos.getCoordX()] = true;
                    }
                }

            }
        } else {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    hidePixelsPos.setLocation(eraserPos.getCoordX() + i, eraserPos.getCoordY() + j);
                    if (Ultility.checkValidPoint(changedColorOfBoard, hidePixelsPos.getCoordX(), hidePixelsPos.getCoordY())) {
                        changedColorOfBoard[(int) hidePixelsPos.getCoordY()][(int) hidePixelsPos.getCoordX()] = SettingConstants.DEFAULT_EMPTY_BACKGROUND_COLOR;
                        markedChangeOfBoard[(int) hidePixelsPos.getCoordY()][(int) hidePixelsPos.getCoordX()] = true;
                        if (coordOfBoard[(int) hidePixelsPos.getCoordY()][(int) hidePixelsPos.getCoordX()] != null) {
                            coordOfBoard[(int) hidePixelsPos.getCoordY()][(int) hidePixelsPos.getCoordX()] = null;
                        }
                    }
                }

            }
        }

    }

    /**
     * Get the previous status of board. <br>
     * This reverts back to the saved state at the top of undo stack. If user
     * just changes any color but not coordinate of objects, we only get the
     * color from undo stack of color, not stack of coordinates. The same works
     * for changing coordinate but not color. Simultaneously, the redo stack
     * will push the current state to its.
     */
    public void undo() {

        resetChangedPropertyArray();
        if (!undoColorOfBoardStack.empty()) {
            saveCurrentColorBoardToRedoStack();
//            Color[][] tempBoard = new Color[heightBoard][widthBoard];
//            copyColorValue(undoColorOfBoardStack.pop(), tempBoard, false);
            copyColorValue(undoColorOfBoardStack.pop(), colorOfBoard, false);
        }
        if (!undoCoordOfBoardStack.empty()) {
            saveCurrentCoordBoardToRedoStack();
//            String [][] tempBoard = new String[heightBoard][widthBoard];
//            copyCoordValue(undoCoordOfBoardStack.pop(), tempBoard);
            copyCoordValue(undoCoordOfBoardStack.pop(), coordOfBoard);
//            System.out.println(coordOfBoard[2][2]);
        }

    }

    /**
     * Get the previous status of board after undo action. <br>
     * This reverts back to the saved state at the top of redo stack. If user
     * just changes any color but not coordinate of objects, we only get the
     * color from redo stack of color, not stack of coordinates. The same works
     * for changing coordinate but not color. Simultaneously, the undo stack
     * will push the current state to its.
     */
    public void redo() {
        if (!redoColorOfBoardStack.empty()) {
            saveCurrentColorBoardToUndoStack();
            copyColorValue(redoColorOfBoardStack.pop(), colorOfBoard, false);
        }
        if (!redoCoordOfBoardStack.empty()) {
            saveCurrentCoordBoardToUndoStack();
            copyCoordValue(redoCoordOfBoardStack.pop(), coordOfBoard);
        }
        repaint();
    }

    /**
     * Save the last user's drawing action to saved board.
     */
    public void apply() {
        // Clear redo stacks.
//        redoColorOfBoardStack.clear();
//        redoCoordOfBoardStack.clear();

        // Save current state to undo stack.
        saveCurrentColorBoardToUndoStack();
        saveCurrentCoordBoardToUndoStack();
        // Merge of changed color to saved state of board
        // NOTE: Why not mergeColorValue coordinate??
        mergeColorValue();
        MainFrame.button_Undo.setEnabled(this.ableUndo());
        // Save the changed coordinate into board.
        mergeCoordValue(changedCoordOfBoard, coordOfBoard);

        // Reset marked change array.
        // resetChangedPropertyArray();
    }

    private boolean isNotSelected() {
        return (startDrawingPoint.equal(SettingConstants.DEFAULT_UNUSED_POINT)
                && endDrawingPoint.equal(SettingConstants.DEFAULT_UNUSED_POINT));
    }

    public void setSelected(SKPoint2D startPoint, SKPoint2D endPoint) {
        startDrawingPoint.setLocation(startPoint);
        endDrawingPoint.setLocation(endPoint);
    }

    public boolean getShowGridFlag() {
        return showGridFlag;
    }

    public boolean getShowCoordinateFlag() {
        return showCoordinateFlag;
    }

    /**
     * Return the current coordinate system.
     *
     * @return SettingConstants.CoordinateMode
     */
    public SettingConstants.CoordinateMode getCoordinateMode() {
        return this.coordinateMode;
    }

    /**
     * Fill background board with specific color.
     *
     * @param graphic
     * @param backgroundColor
     */
    private void drawBackgroundBoard(Graphics graphic, Color backgroundColor) {
        graphic.setColor(backgroundColor);
        graphic.fillRect(0, 0, this.widthBoard, this.heightBoard);
    }

    /**
     * Show axis and point coordination.
     *
     * @param graphic
     */
    private void showBoardCoordination(Graphics graphic) {
        /*
            Show axis coordination.
         */
        graphic.setColor(SettingConstants.DEFAULT_COORDINATE_AXIS_COLOR);

        if (coordinateMode == SettingConstants.CoordinateMode.MODE_2D) {
            // Ox axis 
            graphic.drawLine(1, SettingConstants.COORD_Y_O, this.widthBoard, SettingConstants.COORD_Y_O);
            // Oy axis
            graphic.drawLine(SettingConstants.COORD_X_O, 1, SettingConstants.COORD_X_O, this.heightBoard);

            // Add ), Ox, Oy text string
            graphic.drawString("O", SettingConstants.COORD_X_O - 10, SettingConstants.COORD_Y_O + 10);
            graphic.drawString("Ox", this.widthBoard - 20, SettingConstants.COORD_Y_O + 10);
            graphic.drawString("Oy", SettingConstants.COORD_X_O - 20, 10);

        } else {
            // Ox
            graphic.drawLine(SettingConstants.COORD_X_O, SettingConstants.COORD_Y_O, this.widthBoard, SettingConstants.COORD_Y_O);
            // Oz
            graphic.drawLine(SettingConstants.COORD_X_O, 1, SettingConstants.COORD_X_O, SettingConstants.COORD_Y_O);
            // Oy
            graphic.drawLine(SettingConstants.COORD_X_O, SettingConstants.COORD_Y_O, 1, SettingConstants.COORD_X_O + SettingConstants.COORD_Y_O);

            // Add O, Ox, Oy, Oz text string
            graphic.drawString("O", SettingConstants.COORD_X_O - 10, SettingConstants.COORD_Y_O + 10);
            graphic.drawString("Ox", this.widthBoard - 20, SettingConstants.COORD_Y_O + 10);
            graphic.drawString("Oy", SettingConstants.COORD_X_O - SettingConstants.COORD_Y_O - 10, this.heightBoard - 10);
            graphic.drawString("Oz", SettingConstants.COORD_X_O - 20, 10);
        }

        /*
            Show point coordination.
         */
        graphic.setColor(SettingConstants.DEFAULT_COORDINATE_POINT_COLOR);

        for (int i = 0; i < heightBoard; i++) {
            for (int j = 0; j < widthBoard; j++) {
                String coordinateProperty = coordOfBoard[i][j];

                if (coordinateProperty != null) {

                    int posX = (j + 1) * SettingConstants.RECT_SIZE;
                    int posY = i * SettingConstants.RECT_SIZE - 2;

                    // Normalize
                    if (posX <= 0) {
                        posX = 1;
                    } else if (posX >= widthBoard) {
                        posX = widthBoard - coordinateProperty.length() * SettingConstants.SIZE;
                    }

                    if (posY <= 0) {
                        posY = 1;
                    } else if (posY >= heightBoard) {
                        posY = heightBoard - SettingConstants.SIZE;
                    }

                    graphic.drawString(coordinateProperty, posX, posY);
                }
            }
        }
    }

    /**
     * Paint the board but not showing coordination.
     *
     * @param graphic
     */
    private void paintBoardColor(Graphics graphic) {
        if (showGridFlag) {
            drawBackgroundBoard(graphic, SettingConstants.DEFAULT_GRID_BACKGROUND_COLOR);
        } else {
            drawBackgroundBoard(graphic, SettingConstants.DEFAULT_EMPTY_BACKGROUND_COLOR);
        }

        for (int i = 0; i < this.heightBoard / SettingConstants.RECT_SIZE; i++) {
            for (int j = 0; j < this.widthBoard / SettingConstants.RECT_SIZE; j++) {
                if (markedChangeOfBoard[i][j] == true) {
                    graphic.setColor(changedColorOfBoard[i][j]);
                } else {
                    graphic.setColor(colorOfBoard[i][j]);
                }

                graphic.fillRect(j * SettingConstants.RECT_SIZE + 1,
                        i * SettingConstants.RECT_SIZE + 1,
                        SettingConstants.SIZE,
                        SettingConstants.SIZE
                );
            }
        }

        if (selectedToolMode == SettingConstants.DrawingToolMode.TOOL_ERASER) {
            graphic.setColor(Color.BLACK);
            graphic.drawRect((int) (eraserPos.getCoordX() - 1) * SettingConstants.RECT_SIZE, (int) (eraserPos.getCoordY() - 1) * SettingConstants.RECT_SIZE,
                    SettingConstants.RECT_SIZE * 3, SettingConstants.RECT_SIZE * 3);
        }
    }

    @Override
    public void paintComponent(Graphics graphic) {
        super.paintComponent(graphic);

        paintBoardColor(graphic);

        if (showCoordinateFlag) {
            showBoardCoordination(graphic);
        }
    }

    /**
     * Merge color of this board with another in pixel having position marked in
     * permittedPoint array.
     *
     * @param permittedPoint
     * @param otherBoard
     */
    private void mergeColorValue() {
        for (int i = 0; i < this.heightBoard / SettingConstants.RECT_SIZE; i++) {
            for (int j = 0; j < this.widthBoard / SettingConstants.RECT_SIZE; j++) {
                if (markedChangeOfBoard[i][j] == true) {
                    colorOfBoard[i][j] = new Color(changedColorOfBoard[i][j].getRGB());
                }
            }
        }
    }

    public boolean checkStartingPointAvailable() {
        return (startDrawingPoint.getCoordX() != -1 && startDrawingPoint.getCoordY() != -1);
    }

    public void paintRotation(SKPoint2D centerPoint, double angle) {
        if (recentShape == null) {
            return;
        }

        recentShape.drawVirtualRotation(centerPoint, angle);
        apply();
        repaint();
    }

    public void paintOCenterSymmetry() {
        if (recentShape == null) {
            return;
        }
        recentShape.drawOCenterSymmetry();
        apply();
        repaint();
    }

    public void paintOXSymmetry() {
        if (recentShape == null) {
            return;
        }
        recentShape.drawOXSymmetry();
        apply();
        repaint();
    }

    public void paintOYSymmetry() {
        if (recentShape == null) {
            return;
        }

        recentShape.drawOYSymmetry();
        apply();
        repaint();
    }

    public void paintViaPointSymmetry(SKPoint2D centerPoint) {
        if (recentShape == null) {
            return;
        }

        recentShape.drawPointSymmetry(centerPoint);
        apply();
        repaint();
    }

    public void paintViaLineSymmetry(double a, double b, double c) {
        if (recentShape == null) {
            return;
        }

        recentShape.drawLineSymmetry(a, b, c);
        apply();
        repaint();
    }

    public void draw3DShapeRectangular(double center_x, double center_y, double center_z, int width, int height, int high) {
        resetChangedPropertyArray();
        Rectangular rectangular = new Rectangular(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
        rectangular.setProperty(center_x, center_y, center_z, width, height, high);
        rectangular.drawOutline();
        rectangular.saveCoordinates();
        apply();
        repaint();
        recentShape = null;
    }

    public void draw3DShapeCylinder(double center_x, double center_y, double center_z, int radius, int high) {
        resetChangedPropertyArray();
        Cylinder cylinder = new Cylinder(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
        cylinder.setProperty(center_x, center_y, center_z, radius, high);
        cylinder.drawOutline();
        cylinder.saveCoordinates();
        apply();
        repaint();
        recentShape = null;
    }

    public void draw3DShapePyramid(SKPoint2D centerPoint, int bottomEdge, int high) {

        apply();
        repaint();
        recentShape = null;
    }

    public void draw3DShapeSphere(SKPoint2D centerPoint, int radius) {

        apply();
        repaint();
        recentShape = null;
    }

    public MyPair getXBound() {
        int half_x = (int) (this.widthBoard / (SettingConstants.RECT_SIZE) * 2);
        return new MyPair(-half_x, half_x);
    }

    public MyPair getYBound() {
        int half_y = (int) (this.heightBoard / (SettingConstants.RECT_SIZE) * 2);
        return new MyPair(-half_y, half_y);
    }

    private class CustomMouseClickHandling implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent event) {

            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }

            SettingConstants.DrawingToolMode selectedTool = getSelectedToolMode();
        }

        @Override
        public void mousePressed(MouseEvent event) {
            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }

            setStartDrawingPoint(event.getX() / SettingConstants.RECT_SIZE, event.getY() / SettingConstants.RECT_SIZE);

            resetChangedPropertyArray();

            switch (selectedToolMode) {
                case DRAWING_LINE_FREE: {
                    markedChangeOfBoard[(int) startDrawingPoint.getCoordY()][(int) startDrawingPoint.getCoordX()] = true;
                    changedColorOfBoard[(int) startDrawingPoint.getCoordY()][(int) startDrawingPoint.getCoordX()] = selectedColor;
                    System.out.println(startDrawingPoint.getCoordX() + " " + startDrawingPoint.getCoordY());
                    startDrawingPoint.saveCoord(changedCoordOfBoard);
                    repaint();
                    recentShape = null;
                    break;
                }
                case TOOL_COLOR_FILLER: {
                    copyColorValue(colorOfBoard, changedColorOfBoard, true);
                    SKPoint2D currentMousePos = new SKPoint2D();
                    currentMousePos.setLocation(event.getX() / SettingConstants.RECT_SIZE, event.getY() / SettingConstants.RECT_SIZE);
                    Ultility.paint(changedColorOfBoard, markedChangeOfBoard, currentMousePos, selectedColor);
                    repaint();
                    recentShape = null;
                    break;
                }
                case DRAWING_POLYGON_FREE: {
                    if (checkStartingPointAvailable()) {
                        Segment2D segment = new Segment2D(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        if (Polygon_previousPoint == null) {
                            Polygon_previousPoint = new SKPoint2D(startDrawingPoint);
                            Polygon_firstPoint = new SKPoint2D(Polygon_previousPoint);

                            markedChangeOfBoard[(int) Polygon_firstPoint.getCoordY()][(int) Polygon_firstPoint.getCoordX()] = true;
                            changedColorOfBoard[(int) Polygon_firstPoint.getCoordY()][(int) Polygon_firstPoint.getCoordX()] = new Color(255, 0, 0);
                            startDrawingPoint.saveCoord(changedCoordOfBoard);
                            repaint();
                            return;
                        }
                        int D_X[] = {-1, 0, 0, 1, -1, 1, 1, -1};
                        int D_Y[] = {0, -1, 1, 0, 1, 1, -1, -1};
                        SKPoint2D neibourhoodPoint;
                        boolean end = false;

                        for (int i = 0; i < 8; i++) {
                            neibourhoodPoint = new SKPoint2D(Polygon_previousPoint.getCoordX() + D_X[i], Polygon_previousPoint.getCoordY() + D_Y[i]);
                            if (neibourhoodPoint.equal(Polygon_firstPoint)) {
                                end = true;
                                break;
                            }
                        }
                        //chạy khi click vào điểm start mới
                        if (end == true || Polygon_previousPoint.equal(Polygon_firstPoint)) {
                            if (firstTime == true) {
                                firstTime = false;

                            } else {
                                Polygon_previousPoint = new SKPoint2D(startDrawingPoint);
                                Polygon_firstPoint = new SKPoint2D(Polygon_previousPoint);
                                firstTime = true;

                                markedChangeOfBoard[(int) Polygon_firstPoint.getCoordY()][(int) Polygon_firstPoint.getCoordX()] = true;
                                changedColorOfBoard[(int) Polygon_firstPoint.getCoordY()][(int) Polygon_firstPoint.getCoordX()] = new Color(255, 0, 0);
                                startDrawingPoint.saveCoord(changedCoordOfBoard);

                                repaint();
                                return;
                            }

                        }

                        segment.setProperty(Polygon_previousPoint, startDrawingPoint, Segment2D.Modal.STRAIGHT_LINE);

                        segment.setLineStyle(selectedLineStyle);
                        segment.drawOutline();

                        end = false;
                        if (startDrawingPoint.equal(Polygon_firstPoint)) {
                            end = true;
                        }
                        if (!end) {
                            for (int i = 0; i < 8; i++) {
                                neibourhoodPoint = new SKPoint2D(startDrawingPoint.getCoordX() + D_X[i], startDrawingPoint.getCoordY() + D_Y[i]);
                                if (neibourhoodPoint.equal(Polygon_firstPoint)) {
                                    end = true;
                                    break;
                                }
                            }
                        }

                        if (!end) {
                            segment.saveCoordinates();
                        }

                        recentShape = segment;
                        Polygon_previousPoint.setLocation(startDrawingPoint);

                        markedChangeOfBoard[(int) Polygon_firstPoint.getCoordY()][(int) Polygon_firstPoint.getCoordX()] = true;
                        changedColorOfBoard[(int) Polygon_firstPoint.getCoordY()][(int) Polygon_firstPoint.getCoordX()] = new Color(255, 0, 0);
                        Polygon_firstPoint.saveCoord(changedCoordOfBoard);
                        repaint();

                        recentShape = null;
                    }
                    break;
                }
                case TOOL_ERASER: {
                    //Không resetChangedProperty vì đây là đè, cố ý muốn xóa
                    if (checkStartingPointAvailable()) {
                        SKPoint2D hidePixelsPos = new SKPoint2D();
                        eraserPos.setLocation(event.getX() / SettingConstants.RECT_SIZE, event.getY() / SettingConstants.RECT_SIZE);
                        hidePixels(hidePixelsPos, true);
                    }
                    repaint();
                    recentShape = null;
                }
                case TOOL_ANIMATION: {
                    if (checkStartingPointAvailable()) {
//                        Fish fish = new Fish(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
//                        SKPoint2D startP = new SKPoint2D(50,30);
//                        SKPoint2D endP = new SKPoint2D (70,70);
//                        
//                        apply();
//                        resetChangedPropertyArray();
//                        copyColorValue(colorOfBoard, changedColorOfBoard, true);
//                        fish.paintFish1(startP, endP);
//                        fish.paintFish2(new SKPoint2D(30,65), new SKPoint2D(110,110));
                        SKPoint2D startP_Volcano = new SKPoint2D(80, 45);
                        SKPoint2D endP_Volcano = new SKPoint2D(40, 105);

                        SKPoint2D startP_Cloud = new SKPoint2D(35, 30);

                        SKPoint2D startP_Ground = new SKPoint2D(0, 25);
                        SKPoint2D startP_Smoke = new SKPoint2D(startP_Volcano, 15, -25);
                        SKPoint2D startP_Sun = new SKPoint2D(startP_Volcano, -50, -30);
                        SKPoint2D startP_Tree = new SKPoint2D(startP_Volcano, -30, 20);
                        SKPoint2D startP_Fish1 = new SKPoint2D(startP_Volcano, 50, 50);
                        SKPoint2D startP_Fish2 = new SKPoint2D(startP_Volcano, -30, 42);
//                          
//                          AppleTree tree = new AppleTree(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
//                          tree.drawAppleTree(startP_Tree);
//                          tree.paintAppleTree(startP_Tree);
//                          tree.paintApple();
//                          
//                          apply();
//                          resetChangedPropertyArray();
//                          
//                          Sun sun = new Sun(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
//                          
//                          sun.drawSun(startP_Sun);
//                          apply();
//                          resetChangedPropertyArray();
//                          copyColorValue(colorOfBoard, changedColorOfBoard, true);
//                          sun.paintSun(startP_Sun);
//                          
//                          apply();
//                          resetChangedPropertyArray();
//                          
                        Ground ground = new Ground(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        ground.drawGround(startP_Ground);
                        ground.paintGround(startP_Ground);

                        apply();
                        resetChangedPropertyArray();

                        ground.drawAndPaintFlowers();

                        apply();
                        resetChangedPropertyArray();

//                            River river = new River(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
//                            river.drawRiver(new SKPoint2D (startP_Ground,0,26));
//                            river.paintRiver(new SKPoint2D (startP_Ground,0,26));
//                            
//                            apply();
//                            resetChangedPropertyArray();
//                            Cloud cloud =new Cloud(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
//                            cloud.drawCloud(startP_Cloud);
//                          Fish fish = new Fish(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
//                          fish.drawFish1(startP_Fish1, new SKPoint2D(0,0));
//                          fish.drawFish2(startP_Fish2, new SKPoint2D(0,0));
//                          fish.paintFish1(startP_Fish1, new SKPoint2D(0,0));
//                          fish.paintFish2(startP_Fish2, new SKPoint2D(0,0));
//                          apply();
//                          resetChangedPropertyArray();
                        Volcano volcano = new Volcano(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        Smoke smoke = new Smoke(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);

                        smoke.drawSmoke(startP_Smoke);
                        volcano.drawVolcano(startP_Volcano, endP_Volcano);
                        //draw xong paint luôn, vì lúc này mảng tạm đã có dữ liệu (khác với vẽ chuột, lúc đó ko có dữ liệu, phải copyCoordValue)
                        volcano.paintVolcano(startP_Volcano);

                        apply();
                        resetChangedPropertyArray();

                    }
                    repaint();
                    recentShape = null;
                    break;
                }

            }
        }

        @Override
        public void mouseReleased(MouseEvent event) {

            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }

            apply();
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (selectedToolMode == SettingConstants.DrawingToolMode.TOOL_ERASER__FALSE || selectedToolMode == SettingConstants.DrawingToolMode.TOOL_ERASER) //trường hợp từ mode khác, bấm vào eraser thì dùng đến vế thứ 2 của if!
            {
                selectedToolMode = SettingConstants.DrawingToolMode.TOOL_ERASER;
                // Transparent 16 x 16 pixel cursor image.
                BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

                // Create a new blank cursor.
                Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                        cursorImg, new Point(0, 0), "blank cursor");
                setCursor(blankCursor);
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (selectedToolMode == SettingConstants.DrawingToolMode.TOOL_ERASER) {
                selectedToolMode = SettingConstants.DrawingToolMode.TOOL_ERASER__FALSE;// set về cái này để repaint() lại mất eraser!
                repaint();
            }
        }

    }

    private class CustomMouseMotionHandling implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent event) {

            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }

            setEndDrawingPoint(event.getX() / SettingConstants.RECT_SIZE, event.getY() / SettingConstants.RECT_SIZE);

            switch (selectedToolMode) {
                case DRAWING_LINE_SEGMENT: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Segment2D segment = new Segment2D(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        if (event.isShiftDown()) {
                            segment.setProperty(startDrawingPoint, endDrawingPoint, Segment2D.Modal.LINE_45_90_DEGREE);
                        } else {
                            segment.setProperty(startDrawingPoint, endDrawingPoint, Segment2D.Modal.STRAIGHT_LINE);
                        }
                        segment.setLineStyle(selectedLineStyle);
                        segment.drawOutline();
                        segment.saveCoordinates();
                        recentShape = segment;
                    }
                    repaint();
                    break;
                }
                case DRAWING_LINE_STRAIGHT: {
                    // Do no thing
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Line2D line = new Line2D(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        line.setLineStyle(selectedLineStyle);
                        line.setProperty(startDrawingPoint, endDrawingPoint);
                        line.drawOutline();
                        line.saveCoordinates();
                        recentShape = line;
                    }
                    repaint();
                    break;
                }

                case DRAWING_LINE_FREE: {
                    if (checkStartingPointAvailable()) {
                        Segment2D pixel = new Segment2D(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        pixel.setProperty(startDrawingPoint, endDrawingPoint, Segment2D.Modal.STRAIGHT_LINE);
                        pixel.drawOutline();
                    }
                    setStartDrawingPoint(event.getX() / SettingConstants.RECT_SIZE, event.getY() / SettingConstants.RECT_SIZE);
                    repaint();
                    break;
                }
                case DRAWING_POLYGON_TRIANGLE: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Triangle triangle = new Triangle(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        if (event.isShiftDown()) {
                            triangle.setProperty(startDrawingPoint, endDrawingPoint, Triangle.Modal.EQUILATERAL_TRIANGLE);
                        } else {
                            triangle.setProperty(startDrawingPoint, endDrawingPoint, Triangle.Modal.COMMON_TRIANGLE);
                        }
                        triangle.setLineStyle(selectedLineStyle);
                        triangle.drawOutline();
                        triangle.saveCoordinates();
                        recentShape = triangle;
                    }
                    repaint();
                    break;
                }
                case DRAWING_POLYGON_RECTANGLE: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Rectangle rectangle = new Rectangle(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        if (event.isShiftDown()) {
                            rectangle.setProperty(startDrawingPoint, endDrawingPoint, Rectangle.Modal.SQUARE);
                        } else {
                            rectangle.setProperty(startDrawingPoint, endDrawingPoint, Rectangle.Modal.RECTANGLE);
                        }
                        rectangle.setLineStyle(selectedLineStyle);
                        rectangle.drawOutline();
                        rectangle.saveCoordinates();
                        recentShape = rectangle;
                    }
                    repaint();
                    break;
                }
                case DRAWING_POLYGON_ELLIPSE: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Ellipse ellipse = new Ellipse(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);

                        if (event.isShiftDown()) {
                            ellipse.setProperty(startDrawingPoint, endDrawingPoint, Ellipse.Modal.CIRLCE);
                        } else {
                            ellipse.setProperty(startDrawingPoint, endDrawingPoint, Ellipse.Modal.ELLIPSE);
                        }

                        ellipse.setLineStyle(selectedLineStyle);
                        ellipse.drawOutline();
                        ellipse.saveCoordinates();
                        recentShape = ellipse;
                    }
                    repaint();
                    break;
                }
                case DRAWING_SHAPE_STAR: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Star star = new Star(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        star.setProperty(startDrawingPoint, endDrawingPoint);
                        star.setLineStyle(selectedLineStyle);
                        star.drawOutline();
                        star.saveCoordinates();
                        recentShape = star;
                    }
                    repaint();
                    break;
                }
                case DRAWING_SHAPE_DIAMOND: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Diamond diamond = new Diamond(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        if (event.isShiftDown()) {
                            diamond.setProperty(startDrawingPoint, endDrawingPoint, Diamond.Modal.SQUARE_DIAMOND);
                        } else {
                            diamond.setProperty(startDrawingPoint, endDrawingPoint, Diamond.Modal.COMMON_DIAMOND);
                        }
                        diamond.setLineStyle(selectedLineStyle);
                        diamond.drawOutline();
                        diamond.saveCoordinates();
                        recentShape = diamond;
                    }
                    repaint();
                    break;
                }
                case DRAWING_SHAPE_ARROW: {
                    resetChangedPropertyArray();
                    if (checkStartingPointAvailable()) {
                        Arrow2D arrow = new Arrow2D(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, selectedColor);
                        arrow.setProperty(startDrawingPoint, endDrawingPoint);
                        arrow.setLineStyle(selectedLineStyle);
                        arrow.drawOutline();
                        arrow.saveCoordinates();
                        recentShape = arrow;
                    }
                    repaint();
                    break;
                }
                case TOOL_ERASER: {
                    //Không resetChangedProperty vì đây là đè, cố ý muốn xóa
                    if (checkStartingPointAvailable()) {
                        SKPoint2D hidePixelsPos = new SKPoint2D();
                        eraserPos.setLocation(event.getX() / SettingConstants.RECT_SIZE, event.getY() / SettingConstants.RECT_SIZE);
                        hidePixels(hidePixelsPos, true);
                    }
                    repaint();
                }
            }
        }

        /*
        Use for showing eraser.
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            if (selectedToolMode == SettingConstants.DrawingToolMode.TOOL_ERASER) {
//                SKPoint2D currentMousePos = new SKPoint2D(e.getCoordX() / SettingConstants.RECT_SIZE, e.getCoordY() / SettingConstants.RECT_SIZE);
//                drawEraser(currentMousePos);

                resetChangedPropertyArray(); // để nó hiện lại những chỗ đã đi qua
                SKPoint2D hidePixelsPos = new SKPoint2D();
                eraserPos.setLocation(e.getX() / SettingConstants.RECT_SIZE, e.getY() / SettingConstants.RECT_SIZE);
                hidePixels(hidePixelsPos, false);

                repaint();

            }
            return;
        }

    }

}
