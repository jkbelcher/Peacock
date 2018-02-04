import java.util.AbstractMap;
import java.util.TreeMap;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import processing.core.PApplet;

//Inherited from Joule the art car

//This provides a layer of abstraction between fixtures and outputs.
//
//The base class LXAbstractFixture assumes that pixels are added in the exact
//physical order as the structure.
//This class provides a couple advantages:
//1. More flexibility in the creation of the fixture.  Points do not have to be added in order.
//2. The fixture can skip a pixel position if desired.
// -One use of this is to allow the fixture's configuration to contain information regarding bad, ie skipped, pixels.

public class LXAbstractFixtureMapped extends LXAbstractFixture {

    //Override this to return the points in order as they are on the physical strip
    //The key value indexes are relative to the start of the fixture.  A parent fixture
    //can modify keys but must keep the values the same as they are globally unique.
    public AbstractMap<Integer, LXPoint> getPointsMapped()
    {
        final TreeMap<Integer, LXPoint> mappedPoints = new TreeMap<Integer, LXPoint>();
        for (int i=0; i<this.points.size(); i++) {
            mappedPoints.put(i, this.points.get(i));
        }
        PApplet.println("LXAbstractFixtureMapped.getPointsMapped()");

        return mappedPoints;
    }

    public LXPoint getPoint(int i) {
        return this.points.get(i);
    }

}
