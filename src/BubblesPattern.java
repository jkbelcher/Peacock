import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

/* Bubbles.
 * This pattern Copyright(c)2018 Justin Belcher, use with permission only.
 */
public class BubblesPattern extends PeacockPatternNormalized {

    public final CompoundParameter density = 
            new CompoundParameter("Density", .2, 0, 1)
            .setDescription("Density of bubbles");
    public final CompoundParameter minBubbleSpeed = 
            new CompoundParameter("MinSpeed", 1, 5, 100)
            .setDescription("Minimum pixel moves per second");
    public final CompoundParameter maxBubbleSpeed = 
            new CompoundParameter("MaxSpeed", 60, 5, 150)
            .setDescription("Maximum pixel moves per second");

    private List<Bubble> bubbles = new ArrayList<Bubble>();

    public BubblesPattern(LX lx) {
        super(lx);
        
        addParameter(density);
        addParameter(minBubbleSpeed);
        addParameter(maxBubbleSpeed);
    }

    public void setRandomParameters() {
        randomizeTargetGroup();
        randomizeParameter(this.density);
        randomizeParameter(this.minBubbleSpeed);
        randomizeParameter(this.maxBubbleSpeed);
    }
    
    private Bubble createBubble() {
        Bubble b = new Bubble();        
        b.color = LXColor.hsb(Math.random() * 360.0,100,100);
        b.pos = 0;
        
        float minSpeed = this.minBubbleSpeed.getValuef();
        float maxSpeed = this.maxBubbleSpeed.getValuef();
        float speedRange = Math.max(maxSpeed - minSpeed,1);
        float pixelsPerSec = (float) ((Math.random() * speedRange) + minSpeed);
        
        b.timePerMove = 1000f / pixelsPerSec;
        b.nextMoveTime = this.runMs + b.timePerMove;
        
        return b;
    }

    @Override
    protected void run(double deltaMs) {        
        this.clearColors();
        
        //Get the currently targeted normalized pixel group
        TailPixelGroup group = this.modelN.getTailPixelGroup();

        int maxPos = group.size() - 1;
        
        List<Bubble> expiredBubbles = new ArrayList<Bubble>();
                
        // Shift bubble positions and determine if any are expired
        for (Bubble b: this.bubbles) {
            // Is it time to increment?
            if (this.runMs > b.nextMoveTime) {
                b.pos++;
                b.nextMoveTime = this.runMs + b.timePerMove;
            }
            //Is it beyond the length of the current target group?
            if (b.pos > maxPos) {
                expiredBubbles.add(b);
            }
        }
        
        // Remove expired bubbles
        this.bubbles.removeAll(expiredBubbles);
        
        // Create new bubbles
        int targetNumBubbles = (int)(this.density.getValuef() * (float)group.size());
        int numNewBubbles = targetNumBubbles - this.bubbles.size();
        for (int n = 0; n < numNewBubbles; n++) {
            this.bubbles.add(createBubble());
        }
        
        //Render every bubble
        for (Bubble bubble : this.bubbles) {
            colors[group.tailPixels.get(bubble.pos).getIndexColor()] = bubble.color;
        }
    }

    public class Bubble {
        public int pos;
        public double timePerMove;
        public int color;
        public double nextMoveTime;
    }
}
