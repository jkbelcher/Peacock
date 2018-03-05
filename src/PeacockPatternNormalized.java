import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.BooleanParameter.Mode;

public abstract class PeacockPatternNormalized extends PeacockPattern {

    public final PeacockModelNormalized modelN;
    
    public final BooleanParameter nextGroup = 
            new BooleanParameter("NextGroup")
            .setDescription("Change the pattern to target the next TailPixelGroup")
            .setMode(Mode.MOMENTARY);

    public PeacockPatternNormalized(LX lx) {
        super(lx);
        
        this.modelN = new PeacockModelNormalized(model);
        
        addParameter(nextGroup);
        this.nextGroup.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (((BooleanParameter)p).getValueb()) {
                    goNextGroup();
                }
            }
            });
    }

    public PeacockPatternNormalized(LX lx, TailPixelGroup[] groups) {
        super(lx);
        
        this.modelN = new PeacockModelNormalized(model, groups);

        addParameter(nextGroup);
        this.nextGroup.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (((BooleanParameter)p).getValueb()) {
                    goNextGroup();
                }
            }
            });
    }
    
    public PeacockPatternNormalized(LX lx, PeacockModelNormalized modelN) {
        super(lx);
        
        this.modelN = modelN;
    
        addParameter(nextGroup);
        this.nextGroup.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (((BooleanParameter)p).getValueb()) {
                    goNextGroup();
                }
            }
            });
    }    
    
    public void goNextGroup() {
        this.modelN.goNext();
    }

    public void randomizeTargetGroup() {
        this.modelN.goRandom();
    }

}
