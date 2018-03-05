import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.BooleanParameter.Mode;

public class DashesPattern extends PeacockPatternNormalized {

    public final CompoundParameter hue = 
            new CompoundParameter("Hue", LXColor.h(LXColor.RED), 0, 360)
            .setDescription("Hue");
    public final DiscreteParameter length = 
            new DiscreteParameter("Length", 5, 3, 30)
            .setDescription("Number of pixels per dash");
    public final DiscreteParameter lengthOff = 
            new DiscreteParameter("LenOff", 1, 0, 30)
            .setDescription("Number of pixels between each dash");    
    public final CompoundParameter fade = 
            new CompoundParameter("Soft", .1, .05, .4)
            .setDescription("Softness, or Percentage of dash that fades out to black.");    
    public final CompoundParameter speed = 
            new CompoundParameter("Speed", 5, 0, 60)
            .setDescription("Pixel moves per second");

    public DashesPattern(LX lx) {
        super(lx);

        this.modelN.setTailPixelGroup(model.spiralsCW_IO);  //start with a fun one
        
        addParameter(hue);
        addParameter(length);
        addParameter(lengthOff);
        addParameter(fade);
        addParameter(speed);
        
        this.autoRandom.setValue(false);
    }
    
    public void setRandomParameters() {
        randomizeTargetGroup();
        randomizeParameter(this.hue);
        randomizeParameter(this.length);
        randomizeParameter(this.lengthOff);
        randomizeParameter(this.fade);
        randomizeParameter(this.speed);
    }
    
    float pos = 0;

    @Override
    protected void run(double deltaMs) {
        this.clearColors();
        
        float hue = this.hue.getValuef();
        int length = this.length.getValuei();
        float lengthf = (float)length;
        int lengthOff = this.lengthOff.getValuei();
        float speed = this.speed.getValuef();
        float fade = this.fade.getValuef();
        float fadeLen = fade * lengthf;
        
        int totalLen = length + lengthOff;
        float totalLenf = (float)totalLen;
        
        //Decrement the position, which visually advances the pattern
        this.pos -= speed * (float)deltaMs / 1000f;
        if (pos < 0) {
            pos += totalLenf;
        }
        
        //Calculate array of brightnesses for this frame
        float bright[] = new float[totalLen];
        float offset = pos;
        for (int iBright = 0; iBright < bright.length ; iBright++) {
            if (offset < fadeLen) {
                bright[iBright] = (offset / fadeLen) * 100f; 
            } else if (offset < lengthf - fadeLen) {
                bright[iBright] = 100f;
            } else if (offset < lengthf) {
                bright[iBright] = ((lengthf-offset) / fadeLen) * 100f;                
            } else {
                bright[iBright] = 0f;
            }
            
            offset += 1f;
            if (offset >= totalLenf) {
                offset -= totalLenf;
            }            
        }
        
        //Get the currently targeted normalized pixel group
        TailPixelGroup tailPixelGroup = this.modelN.getTailPixelGroup();

        //Draw it
        int b=0;
        for (int i = 0; i < tailPixelGroup.size(); i++) {
            TailPixelPos tpp = tailPixelGroup.tailPixels.get(i);
            colors[tpp.getIndexColor()] = LXColor.hsb(hue, 100f, bright[b]);
            
            //Cycle through the brightness array
            b++;
            b %= bright.length;
        }

    }

}
