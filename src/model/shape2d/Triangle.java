package model.shape2d;

import control.myawt.SKPoint2D;
import java.awt.Color;

public class Triangle extends Shape2D {

    private SKPoint2D pointA;
    private SKPoint2D pointB;
    private SKPoint2D pointC;

    public enum Modal {
        COMMON_TRIANGLE,
        EQUILATERAL_TRIANGLE,
    }

    public Triangle(boolean[][] markedChangeOfBoard, Color[][] changedColorOfBoard, String[][] changedCoordOfBoard, Color filledColor) {
        super(markedChangeOfBoard, changedColorOfBoard, changedCoordOfBoard, filledColor);
        pointA = new SKPoint2D(-1, -1);
        pointB = new SKPoint2D(-1, -1);
        pointC = new SKPoint2D(-1, -1);
    }

    /**
     * Set 3 points of triangle by using startPoint2D and endPoint2D The type of
     * triangle is either common or equilateral depending on Modal
     *
     * @param startPoint
     * @param endPoint
     */
    public void setProperty(SKPoint2D startPoint, SKPoint2D endPoint, Modal modal) {
        int width = (int) (endPoint.getCoordX() - startPoint.getCoordX());
        int height = (int) (endPoint.getCoordY() - startPoint.getCoordY());

        int adjustingValue;
        int widthDirection = this.getWidthDirection(width);
        int heightDirection = this.getHeightDirection(height);

        if (modal == Modal.COMMON_TRIANGLE) {
            this.startPoint2D = startPoint;
            this.endPoint2D = endPoint;
            adjustingValue = 0;
        } else {
            adjustingValue = -widthDirection; // giá trị để dịch 1 pixel có hướng, căn chỉnh lại cho đều 2 cạnh của tam giác đều

            int preferedLength = this.getPreferredLength(width, height);

            this.startPoint2D = startPoint;
            this.endPoint2D.setLocation(this.startPoint2D.getCoordX() + widthDirection * preferedLength, this.startPoint2D.getCoordY() + heightDirection * preferedLength);

        }

        double Ay, By, Cy;
        if (this.endPoint2D.getCoordY() < this.startPoint2D.getCoordY()) {
            Ay = this.endPoint2D.getCoordY();
            By = this.startPoint2D.getCoordY();
            Cy = this.startPoint2D.getCoordY();
        } else {
            Ay = this.startPoint2D.getCoordY();
            By = this.endPoint2D.getCoordY();
            Cy = this.endPoint2D.getCoordY();
        }

        pointA.setLocation(((this.startPoint2D.getCoordX() + this.endPoint2D.getCoordX()) / 2), Ay);
        pointB.setLocation(this.startPoint2D.getCoordX(), By);
        pointC.setLocation(this.endPoint2D.getCoordX() + adjustingValue, Cy);

        centerPoint2D.setLocation(
                pointA.getCoordX(),
                pointC.getCoordY() + (int) ((2.0 / 3.0) * Math.abs(pointC.getCoordY() - pointA.getCoordY()))
        );
    }

    @Override
    public void setProperty(SKPoint2D startPoint, SKPoint2D endPoint) {
        setProperty(startPoint, endPoint, Modal.COMMON_TRIANGLE);
    }

    @Override
    public void saveCoordinates() {
        pointA.saveCoord(this.changedCoordOfBoard);
        pointB.saveCoord(this.changedCoordOfBoard);
        pointC.saveCoord(this.changedCoordOfBoard);
    }

    /**
     * Drawing in board.
     */
    @Override
    public void drawOutline() {
        SKPoint2D tempPointA = pointA.createRotate(centerPoint2D, this.rotatedAngle);
        SKPoint2D tempPointB = pointB.createRotate(centerPoint2D, this.rotatedAngle);
        SKPoint2D tempPointC = pointC.createRotate(centerPoint2D, this.rotatedAngle);

        drawSegment(tempPointA, tempPointB);
        drawSegment(tempPointB, tempPointC);
        drawSegment(tempPointC, tempPointA);
    }

    @Override
    public void applyMove(Vector2D vector) {
        pointA.rotate(centerPoint2D, this.rotatedAngle).move(vector);
        pointB.rotate(centerPoint2D, this.rotatedAngle).move(vector);
        pointC.rotate(centerPoint2D, this.rotatedAngle).move(vector);

        centerPoint2D.move(vector);
    }

    @Override
    public void createRotateInstance(SKPoint2D centerPoint, double angle) {
        if (pointSet.isEmpty()) {
            return;
        }

        double totalAngle = rotatedAngle + angle;

        SKPoint2D newPointA = pointA.createRotate(centerPoint, totalAngle);
        SKPoint2D newPointB = pointB.createRotate(centerPoint, totalAngle);
        SKPoint2D newPointC = pointC.createRotate(centerPoint, totalAngle);

        newPointA.saveCoord(changedCoordOfBoard);
        newPointB.saveCoord(changedCoordOfBoard);
        newPointC.saveCoord(changedCoordOfBoard);

        drawSegment(newPointA, newPointB);
        drawSegment(newPointB, newPointC);
        drawSegment(newPointC, newPointA);
    }

    @Override
    public void createOCenterSymInstance() {
        super.createOCenterSymInstance();

        SKPoint2D newPointA = pointA.createOCenterSym();
        SKPoint2D newPointB = pointB.createOCenterSym();
        SKPoint2D newPointC = pointC.createOCenterSym();

        newPointA.saveCoord(changedCoordOfBoard);
        newPointB.saveCoord(changedCoordOfBoard);
        newPointC.saveCoord(changedCoordOfBoard);
    }

    @Override
    public void createOXSymInstance() {
        super.createOXSymInstance();

        SKPoint2D newPointA = pointA.createOXSym();
        SKPoint2D newPointB = pointB.createOXSym();
        SKPoint2D newPointC = pointC.createOXSym();

        newPointA.saveCoord(changedCoordOfBoard);
        newPointB.saveCoord(changedCoordOfBoard);
        newPointC.saveCoord(changedCoordOfBoard);
    }

    @Override
    public void createOYSymInstance() {
        super.createOYSymInstance();

        SKPoint2D newPointA = pointA.createOYSym();
        SKPoint2D newPointB = pointB.createOYSym();
        SKPoint2D newPointC = pointC.createOYSym();

        newPointA.saveCoord(changedCoordOfBoard);
        newPointB.saveCoord(changedCoordOfBoard);
        newPointC.saveCoord(changedCoordOfBoard);
    }

    @Override
    public void createPointSymInstance(SKPoint2D basePoint) {
        super.createPointSymInstance(basePoint);

        SKPoint2D newPointA = pointA.createPointSym(basePoint);
        SKPoint2D newPointB = pointB.createPointSym(basePoint);
        SKPoint2D newPointC = pointC.createPointSym(basePoint);

        newPointA.saveCoord(changedCoordOfBoard);
        newPointB.saveCoord(changedCoordOfBoard);
        newPointC.saveCoord(changedCoordOfBoard);
    }

    @Override
    public void createLineSymInstance(double a, double b, double c) {
        super.createLineSymInstance(a, b, c);
        
        SKPoint2D newPointA = pointA.createLineSym(a, b, c);
        SKPoint2D newPointB = pointB.createLineSym(a, b, c);
        SKPoint2D newPointC = pointC.createLineSym(a, b, c);

        newPointA.saveCoord(changedCoordOfBoard);
        newPointB.saveCoord(changedCoordOfBoard);
        newPointC.saveCoord(changedCoordOfBoard);
    }

}
