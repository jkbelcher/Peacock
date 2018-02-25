import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;

//To write a pattern that only uses the three dimensional coordinates of points, a pattern class
//can directly extend LXPattern.
//To write a pattern that uses collections specific to the Peacock model, extend the PeacockPattern class.
//Then from within your pattern you can access the this.model property.
//
//This prevents each pattern from needing to cast LXModel to JouleModel.

public abstract class PeacockPattern extends RandomizableLXPattern {

    protected final PeacockModel model;

    public PeacockPattern(LX lx) {
        super(lx);
        this.model = (PeacockModel)lx.model;
    }

    public int getRandomColor() {
        return this.getRandomColor(100);
    }

    public int getRandomColor(float brightness) {
        return LXColor.hsb(Math.random()*360, 100, brightness);
    }

}
