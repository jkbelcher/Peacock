import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import heronarts.lx.model.LXPoint;
import processing.core.PApplet;

//For the Peacock each channel is a fixture.
//Both Panels and Feathers are fixtures/channels.
public class PeacockFixture extends LXAbstractFixtureMapped implements Comparable<PeacockFixture> {

    public final int channel;
    public final PeacockController controller;
    public final List<TailPixel> tailPixels = new ArrayList<TailPixel>();
    
    public PeacockFixture(int channel, PeacockController controller) {
        this.channel = channel;
        this.controller = controller;
        //PApplet.println("Fixture "+this.channel);
    }
    
    @Override
    public int compareTo(PeacockFixture o) {
        int compareChannel = o.channel;
        return compareChannel - this.channel;
    }

    public void AddTailPixel(TailPixel tailPixel)
    {
        this.tailPixels.add(tailPixel);
        this.addPoint(tailPixel.p);
    }
    
    //Called once after model is loaded
    protected void setLoaded() {
        Collections.sort(tailPixels);
    }
        
    //Return, in physical order, the indices of the LXPoints in this channel.
    //The indices are derived from the order in which the points were loaded into the model,
    //and therefore don't necessarily match anything physical.
    public int[] getPointIndicesForOutput() {
        int[] indices = new int[this.tailPixels.size()];
        int i = 0;
        for (TailPixel tp : this.tailPixels) {
            indices[i++] = tp.p.index;
          }
        return indices;        
    }
    
}
