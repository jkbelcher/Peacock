import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

public class DemoNormalFeathersLRPattern extends PeacockPattern {

    public final CompoundParameter position = 
            new CompoundParameter("Position", 0, 0, 1)
            .setDescription("Normalized position of the lit pixels");
    public final CompoundParameter width = 
            new CompoundParameter("Width", .015, 0.01, 1)
            .setDescription("Width of the lit pixels, normalized");
    
    
    public DemoNormalFeathersLRPattern(LX lx) {
        super(lx);
        
        addParameter(position);
        addParameter(width);
    }

    @Override
    protected void run(double deltaMs) {
        //Set all pixels to black before applying their new colors
        this.clearColors();
        
        //Get current parameter values
        float position = this.position.getValuef();
        float width = this.width.getValuef();
        
        //Loop over normalized collection and light pixels that are close to the position
        for (TailPixelPos tpp : model.feathersLR.tailPixels) {
            if (tpp.getN() > position - width && tpp.getN() < position + width) {
                float brightness = (width - Math.abs(position-tpp.getN())) / width;
                colors[tpp.getIndexColor()] = LXColor.scaleBrightness(LXColor.RED, brightness);
            }
        }
    }

}
