import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PApplet;
import heronarts.lx.parameter.BooleanParameter.Mode;

public class DemoNormalSpiralIDPattern extends PeacockPattern {

    public final CompoundParameter spiralID = 
        new CompoundParameter("SpiralID", 0, 0, 1)
        .setDescription("Spiral ID to light");
	  
    public final CompoundParameter position = 
            new CompoundParameter("Position", 0, 0, 1)
            .setDescription("Position");

    public final BooleanParameter identify = 
            new BooleanParameter("Identify")
            .setDescription("Identify the current values")
            .setMode(Mode.MOMENTARY);    

	public DemoNormalSpiralIDPattern(LX lx) {
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
		int spiralID = Math.max(1, (int) Math.ceil(this.spiralID.getValue() * 24));
		int position = Math.max(1, (int) Math.ceil(this.position.getValue() * 75));
        		
		for (TailPixel tp : model.tailPixels) {
			if (tp.params.spiral == spiralID) {
				colors[tp.p.index] = tp.params.position == position ? LXColor.BLUE : LXColor.WHITE;
			}
		}
	}
}
