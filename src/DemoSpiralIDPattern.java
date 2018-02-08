import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.DiscreteParameter;

public class DemoSpiralIDPattern extends PeacockPattern {

    public final DiscreteParameter spiralID = 
        new DiscreteParameter("SpiralID", 1, 1, 24)
        .setDescription("Spiral ID to light");
	  
	public DemoSpiralIDPattern(LX lx) {
		super(lx);
		
		addParameter(spiralID);
	}

	@Override
	protected void run(double arg0) {
		this.clearColors();

		//Get the current value of the parameter
		int spiralID = this.spiralID.getValuei();

		int fgColor = LXColor.WHITE;		

		for (TailPixel tp : model.tailPixels) {
			if (tp.params.spiral == spiralID) {
				colors[tp.p.index] = fgColor;
			}
		}
	}
}
