import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class SolidColorPeacockPattern extends PeacockPatternNormalized {

    public final CompoundParameter hue =
        new CompoundParameter("Hue", LXColor.h(LXColor.RED), 0, 360)
        .setDescription("Hue");
    public final CompoundParameter brightness =
        new CompoundParameter("Brightness", 100, 0, 100)
        .setDescription("Brightness");

    public SolidColorPeacockPattern(LX lx) {
        super(lx, new TailPixelGroup[] { 
                ((PeacockModel)(lx.model)).feathersLR,
                ((PeacockModel)(lx.model)).panelsLR,
                ((PeacockModel)(lx.model)).body,
                ((PeacockModel)(lx.model)).neck,
                ((PeacockModel)(lx.model)).eyes
                });

        addParameter(hue);
        addParameter(brightness);
    }

    public void setRandomParameters() {
        //randomizeTargetGroup();   //Don't change the group on this pattern 
        randomizeParameter(this.hue);
        randomizeParameter(this.brightness);
    }
    
    @Override
    protected void run(double deltaMs) {
        this.clearColors();

        float hue = this.hue.getValuef();
        float brightness = this.brightness.getValuef();

        int color = LXColor.hsb(hue, 100, brightness);

        for (TailPixelPos p : this.modelN.getTailPixelGroup().tailPixels) {
            colors[p.getIndexColor()] = color;
        }
    }

}
