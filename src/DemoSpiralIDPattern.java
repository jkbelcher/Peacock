import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import processing.core.PApplet;
import heronarts.lx.parameter.BooleanParameter.Mode;

public class DemoSpiralIDPattern extends PeacockPattern {
    public final DiscreteParameter spiralID = 
        new DiscreteParameter("SpiralID", 1, 1, 24)
        .setDescription("Spiral ID to light");
	  
    public final DiscreteParameter position = 
            new DiscreteParameter("Position", 1, 1, 75)
            .setDescription("Position");

    public final BooleanParameter identify = 
            new BooleanParameter("Identify")
            .setDescription("Identify the current values")
            .setMode(Mode.MOMENTARY);    

	public DemoSpiralIDPattern(LX lx) {
		super(lx);
		
        addParameter(spiralID);
        addParameter(position);
        addParameter(identify);        
	}
    
	@Override
	protected void run(double arg0) {
		//this.clearColors();
	    this.setColors(LXColor.RED);
	    
		//Get the current value of the parameter
		int spiralID = this.spiralID.getValuei();
		int position = this.position.getValuei();
		
		//Hacky solution to help us identify configuration issues
		if (this.identify.getValueb()) {
		    PApplet.println("Spiral: " + spiralID + "  Position: " + position);
		}

		for (TailPixel tp : model.tailPixels) {
			if (tp.params.spiral == spiralID) {
				colors[tp.p.index] = tp.params.position == position ? LXColor.WHITE : LXColor.BLUE;
			}
		}
	}
}
