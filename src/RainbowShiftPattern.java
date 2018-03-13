import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

public class RainbowShiftPattern extends PeacockPatternNormalized {

    private static final float RADIANS_PER_REVOLUTION = 2.0f;

    public final CompoundParameter hueRange = 
            new CompoundParameter("hueRange", 128, 5, 400)
            .setDescription("hueRange");
    public final CompoundParameter speed = 
            new CompoundParameter("Speed", .14, .001, 1.1)
            .setDescription("Speed in full range shifts per second");
    
    float huePos = 0;

    public RainbowShiftPattern(LX lx) {
        super(lx);

        addParameter(hueRange);
        addParameter(speed);        

        this.autoRandom.setValue(false);
    }
    
    public void setRandomParameters() {
        randomizeTargetGroup();
        randomizeParameter(this.hueRange);
        randomizeParameter(this.speed);
    }

    @Override
    protected void run(double deltaMs) {
        this.clearColors();
        
        float hueRange = this.hueRange.getValuef();
        
        //Calc current position
        float speed = this.speed.getValuef();
        float degreesMoved = hueRange*speed*(((float) deltaMs)/1000f);
        
        huePos -= degreesMoved;
        huePos %= 360;
        
        //Get the currently targeted normalized pixel group
        TailPixelGroup tailPixelGroup = this.modelN.getTailPixelGroup();

        int numPixels = tailPixelGroup.size();
        float numPixelsf = (float) numPixels;
        float degreesPerPixel = (hueRange/numPixelsf);

        for (int i = 0; i < numPixels; i++) {
            TailPixelPos tpp = tailPixelGroup.tailPixels.get(i);
            float hue = ((degreesPerPixel*((float) i))+huePos) % 360;
            colors[tpp.getIndexColor()] = LXColor.hsb(hue, 100, 100);
        }        
    }

}
