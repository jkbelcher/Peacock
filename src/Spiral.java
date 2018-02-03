import java.util.ArrayList;
import java.util.List;

import heronarts.lx.model.LXAbstractFixture;

//Represents one spiral on lights on the Peacock's tail.
//This will probably also be a single string of LEDs, ie one channel on the LED controller.
//A spiral either curves to the right or left.  Currently I expect we will choose a numbering system that will identify the curve direction and position.
public class Spiral extends LXAbstractFixture {

    public final int ID;
    public final List<TailPixel> tailPixels;

    public Spiral(int id) {
        this.ID = id;
        this.tailPixels = new ArrayList<TailPixel>();
    }

    public Spiral addTailPixel(TailPixel tailPixel) {

        this.addPoint(tailPixel.p);
        this.tailPixels.add(tailPixel);

        return this;
    }

}
