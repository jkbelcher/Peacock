import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class ColorMappablePattern extends PeacockPatternNormalized {

    public final DiscreteParameter red = 
            new DiscreteParameter("Red", 0, 0, 255)
            .setDescription("Red");
    
    public final DiscreteParameter green = 
            new DiscreteParameter("Green", 0, 0, 255)
            .setDescription("Green");
    
    public final DiscreteParameter blue = 
            new DiscreteParameter("Blue", 0, 0, 255)
            .setDescription("Blue");
    
    public final CompoundParameter brightness = 
            new CompoundParameter("Brightness", 0, 0, 1)
            .setDescription("Brightness");
    
    public ColorMappablePattern(LX lx) {
        this(lx, new TailPixelGroup[] { PeacockCode.applet.model.feathersLR });
    }

    public ColorMappablePattern(LX lx, TailPixelGroup[] groups) {
        super(lx, groups);

        addParameter(red);
        addParameter(green);
        addParameter(blue);
        addParameter(brightness);        
    }

    public ColorMappablePattern(LX lx, PeacockModelNormalized modelN) {
        super(lx, modelN);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void run(double deltaMs) {
        this.clearColors();
        
        int red = this.red.getValuei();
        int green = this.green.getValuei();
        int blue = this.blue.getValuei();
        float brightness = this.brightness.getValuef();
        
        int c = LXColor.scaleBrightness(LXColor.rgb(red, green, blue), brightness);
        
        TailPixelGroup tailPixelGroup = this.modelN.getTailPixelGroup();
        
        for (int i = 0; i < tailPixelGroup.size(); i++) {
            TailPixelPos tpp = tailPixelGroup.tailPixels.get(i);
            colors[tpp.getIndexColor()] = c;
        }

    }

}
