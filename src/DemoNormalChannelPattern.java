import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter.Mode;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PApplet;

public class DemoNormalChannelPattern extends PeacockPattern {

    public final CompoundParameter channel = 
        new CompoundParameter("Channel", 0, 0, 1)
        .setDescription("Channel to light");
	  
    public final CompoundParameter position = 
            new CompoundParameter("Position", 0, 0, 1)
            .setDescription("Position");

    public final BooleanParameter identify = 
            new BooleanParameter("Identify")
            .setDescription("Identify the current values")
            .setMode(Mode.MOMENTARY);    


    public DemoNormalChannelPattern(LX lx) {
        super(lx);
        
        addParameter(channel);
        addParameter(position);
        addParameter(identify);        
    }
    
    @Override
    protected void run(double arg0) {
        this.clearColors();

		//Get the current value of the parameter
		int channel = Math.max(1, (int) Math.ceil(this.channel.getValue() * 29));
		int position = Math.max(1, (int) Math.ceil(this.position.getValue() * 75));
        		
        //Hacky solution to help us identify configuration issues
        if (this.identify.getValueb()) {
            PApplet.println("Channel: " + channel + "  Position: " + position);
        }

        for (PeacockFixture fixture : model.allPeacockFixtures) {
            if (fixture.channel == channel)
            {
                for (TailPixel tp : fixture.tailPixels) {
                        colors[tp.p.index] = LXColor.RED;
                }
                
                if (fixture.tailPixels.size() > position) {
                    colors[fixture.tailPixels.get(position).p.index] = LXColor.BLUE;
                }
            }
        }
    }
    
}
