import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import heronarts.lx.model.LXPoint;
import processing.core.PApplet;

//Inherited from Joule the art car

public class BeagleboneController extends LXAbstractFixtureMapped {

    public final ControllerParameters params;
    public final List<TailPixel> tailPixels = new ArrayList<TailPixel>();

    public BeagleboneController(ControllerParameters params) {
        this.params = params;
        PApplet.println("Controller "+this.params.id+": "+this.params.ipAddress+":"+this.params.port+", "+this.params.numberOfChannels+" channels, "+this.params.LEDsPerChannel+" pixels per channel.");
    }

    public void AddTailPixel(TailPixel tailPixel)
    {
        this.tailPixels.add(tailPixel);
        this.addPoint(tailPixel.p);
    }


    public AbstractMap<Integer, LXPoint> getPointsMapped()
    {
        final TreeMap<Integer, LXPoint> mappedPoints = new TreeMap<Integer, LXPoint>();
        /*
           for (Cluster cluster : this.clusters) {
           for (Gem gem : cluster.gems) {
           for(Map.Entry<Integer,LXPoint> entry : gem.getPointsMapped().entrySet()) {
           mappedPoints.put((gem.params.controllerChannel*this.params.LEDsPerChannel)+entry.getKey(), entry.getValue());
           }
           }
           }
        //PApplet.println("BeagleboneController.getPointsMapped()");
        PApplet.println("Controller",this.params.id+":",mappedPoints.size(),"mapped points");
        */
        return mappedPoints;
    }

}
