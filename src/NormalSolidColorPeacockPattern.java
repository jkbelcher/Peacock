import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class NormalSolidColorPeacockPattern extends PeacockPattern {

    public final CompoundParameter hue =
        new CompoundParameter("Hue", LXColor.h(LXColor.RED) / 360, 0, 1)
        .setDescription("Hue");

    public final CompoundParameter brightness =
        new CompoundParameter("Brightness", 1, 0, 1)
        .setDescription("Brightness");

    public NormalSolidColorPeacockPattern(LX lx) {
        super(lx);

        addParameter(hue);
        addParameter(brightness);
    }

    @Override
    protected void run(double deltaMs) {

        this.clearColors();

		//Get the current value of the parameter
		int hue = Math.max(0, (int) Math.ceil(this.hue.getValue() * 360));
		int brightness = Math.max(0, (int) Math.ceil(this.brightness.getValue() * 100));
        		
        int color = LXColor.hsb(hue, 100, brightness);
        for (LXPoint p : this.model.points) {
            colors[p.index] = color;
        }
    }

}
