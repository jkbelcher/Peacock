import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import java.util.ArrayList;

public class SweepingRainbowPattern extends PeacockPattern {

    public final CompoundParameter width =
        new CompoundParameter("Width", 10f, 0f, 50f)
            .setDescription("Width");

    public final CompoundParameter speed =
        new CompoundParameter("Speed", 1f, 0f, 10f)
            .setDescription("Speed");

    public final CompoundParameter maxBrightness =
        new CompoundParameter("Maximum Brightness", 100f, 0f, 100f)
            .setDescription("Maximum Brightness");

    float colorValue, radius, angle = -90f, brightness;
    double radians, xRadial, x1Variant, x2Variant, x1, x2, x3, x4, yRadial, y1Variant, y2Variant, y1, y2, y3, y4;
    float computeAngle = 0;
    ArrayList<Point> points;

    public SweepingRainbowPattern(LX lx) {
        super(lx);

        addParameter(width);
        addParameter(speed);
        addParameter(maxBrightness);
        points = new ArrayList<Point>();
    }

    @Override
    public void run(double deltaMs) {

        radius = (model.xMax / model.yMax) * model.xMax;

        for (TailPixel tp : this.model.tailPixels) {

            colorValue = (float)(Math.sqrt(Math.pow(tp.p.y, 2) + Math.pow(tp.p.x, 2)) / radius) * 360;
            // if pixel falls within range of angle
            if (insideSweepPath(tp.p.x, tp.p.y, radius, width.getValuef())) {
                brightness = maxBrightness.getValuef();
            } else {
                brightness = 0;
            }
            colors[tp.p.index] = LXColor.hsb( colorValue, 100, brightness);
        }
        angle += speed.getValuef();

        if (angle >= 90f) {
            angle = -90f;
        }
    }

    private boolean insideSweepPath(float x, float y, float radius, float width)
    {
        computeAngle = 0;
        points = new ArrayList<Point>();
        double radians = Math.toRadians(angle);
        xRadial = Math.sin(radians) * radius;
        yRadial = Math.cos(radians) * radius;

        x1Variant = (width / 2) * Math.cos(Math.toRadians(angle - 90));
        y1Variant = (width / 2) * Math.sin(Math.toRadians(angle - 90));
        x2Variant = (width / 2) * Math.sin(Math.toRadians(angle + 90));
        y2Variant = (width / 2) * Math.cos(Math.toRadians(angle + 90));


        points.add(new Point(x1Variant, y1Variant));
        points.add(new Point(x2Variant, y2Variant));
        points.add(new Point(xRadial + x2Variant, yRadial + y2Variant));
        points.add(new Point(xRadial + x1Variant, yRadial + y1Variant));

        int i, j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if (((points.get(i).y > y) != (points.get(j).y > y)) &&
                (x < (points.get(j).x - points.get(i).x) * (y - points.get(i).y) / (points.get(j).y - points.get(i).y) + points.get(i).x))
                result = !result;
        }
        return result;
    }

    private class Point
    {
        public double x, y;
        public Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }
}
