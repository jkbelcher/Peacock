import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.BooleanParameter.Mode;

// We can change the target collection in this pattern for testing
public class DemoNormalCollectionPattern extends PeacockPattern {

    public final PeacockModelNormalized modelN;
    
    public final BooleanParameter nextGroup = 
            new BooleanParameter("NextGroup")
            .setDescription("Change the pattern to target the next TailPixelGroup")
            .setMode(Mode.MOMENTARY);
    public final CompoundParameter position = 
            new CompoundParameter("Position", 0, 0, 1)
            .setDescription("Normalized position of the lit pixels");
    public final CompoundParameter width = 
            new CompoundParameter("Width", .015, 0.01, 1)
            .setDescription("Width of the lit pixels, normalized");    
        
    public DemoNormalCollectionPattern(LX lx) {
        super(lx);
        
        this.modelN = new PeacockModelNormalized(model);
        this.modelN.setTailPixelGroup(model.spiralsCW_IO);

        addParameter(nextGroup);
        this.nextGroup.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (((BooleanParameter)p).getValueb()) {
                    goNextGroup();
                }
            }
            });        

        addParameter(position);
        addParameter(width);        
    }
    
    public void goNextGroup() {
        this.modelN.goNext();
    }
    
    public void setRandomParameters() {
        randomizeParameter(this.position);
        randomizeParameter(this.width);
    }

    @Override
    protected void run(double deltaMs) {
        //Set all pixels to black before applying their new colors
        this.clearColors();
        
        //Get current parameter values
        float position = this.position.getValuef();
        float width = this.width.getValuef();
        
        //Loop over normalized collection and light pixels that are close to the position
        TailPixelGroup targetGroup = this.modelN.getTailPixelGroup();
        for (TailPixelPos tpp : targetGroup.tailPixels) {
            if (tpp.getN() > position - width && tpp.getN() < position + width) {
                float brightness = (width - Math.abs(position-tpp.getN())) / width;
                colors[tpp.getIndexColor()] = LXColor.scaleBrightness(LXColor.RED, brightness);
            }
        }
    }

}