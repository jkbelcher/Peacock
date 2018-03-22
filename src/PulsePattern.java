import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class PulsePattern extends PeacockPattern {

    public final CompoundParameter speed =
        new CompoundParameter("Speed", 1f, 0f, 10f)
            .setDescription("Speed");

    public final CompoundParameter maxBrightness =
        new CompoundParameter("Maximum Brightness", 100f, 0f, 100f)
            .setDescription("Maximum Brightness");


    float colorValue, brightness = 0f, brightDegrees;

    public PulsePattern(LX lx) {
        super(lx);
        addParameter(speed);
        addParameter(maxBrightness);
    }
    
    public void setRandomParameters() {
        randomizeParameter(this.speed);
        randomizeParameter(this.maxBrightness);
    }

    @Override
    public void run(double deltaMs) {

        brightness = (float) (Math.cos( Math.toRadians(brightDegrees)) + 1) * (maxBrightness.getValuef() / 2);

        for (TailPixel tp : this.model.tailPixels) {
            colorValue = (float)(Math.sqrt(Math.pow(tp.p.y, 2) + Math.pow(tp.p.x, 2)) / ((model.xMax / model.yMax) * model.xMax)) * 360;
            colors[tp.p.index] = LXColor.hsb( colorValue, 100, brightness);
        }

        brightDegrees += speed.getValuef();

        if (brightDegrees >= 360) {
            brightDegrees = 0;
        }
    }
}
