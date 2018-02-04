import heronarts.lx.model.LXPoint;
import processing.core.PApplet;

public class TailPixel extends LXAbstractFixtureMapped {

    public final TailPixelParameters params;
    public final LXPoint p;

    public TailPixel(TailPixelParameters params) {
        this.params = params;
        PApplet.println(" TailPixel",this.params.x, this.params.y, this.params.z);

        //Each LXPoint should be created only once.  We'll do it here, in the TailPixel constructor.
        LXPoint lxPoint = new LXPoint(params.x, params.y, params.z);

        this.p = lxPoint;
    }

}
