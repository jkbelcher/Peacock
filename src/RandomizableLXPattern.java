import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.BooleanParameter.Mode;

public abstract class RandomizableLXPattern extends LXPattern {

    public final BooleanParameter randomize = 
            new BooleanParameter("Randomize")
            .setDescription("Randomize the parameters")
            .setMode(Mode.MOMENTARY);
    
    public final BooleanParameter autoRandom = 
            new BooleanParameter("AutoRandom", true)
            .setDescription("When ENABLED, randomize will be called every time the pattern is started.")
            .setMode(Mode.TOGGLE);

    public RandomizableLXPattern(LX lx) {
        super(lx);
        
        addParameter(randomize);
        addParameter(autoRandom);
        
        this.randomize.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (((BooleanParameter)p).getValueb()) {
                    setRandomParameters();
                }
            }
            });
    }

    /**
     * Subclasses can override this method to set random values for parameters.
     */
    public /* abstract */ void setRandomParameters() {        
    }
    
    public void onActive()
    {
        //The pattern was started!  If autoRandomize = true, select random parameter values.
        if (this.autoRandom.getValueb()) {
            this.setRandomParameters();
        }
    }
    
    public static void randomizeParameter(BoundedParameter parameter) {
        float newValue = (float) ((Math.random()*(parameter.range.max-parameter.range.min))+parameter.range.min);
        parameter.setValue(newValue);
    }

    public static void randomizeParameter(DiscreteParameter parameter) {
        int newValue = (int) (Math.random() * ((float)(parameter.getRange()))+parameter.getMinValue());
        parameter.setValue(newValue);
    }

}
