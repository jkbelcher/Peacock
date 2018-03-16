import heronarts.lx.model.LXPoint;
import processing.core.PApplet;

public class TailPixel extends LXAbstractFixtureMapped implements Comparable<TailPixel> {

    public final TailPixelParameters params;
    public final LXPoint p;
    public final int feather;
    public final int panel;

    public TailPixel(TailPixelParameters params) {
        this.params = params;
        //PApplet.println("TailPixel: ",this.params.x, this.params.y, this.params.z);

        //Each LXPoint should be created only once.  We'll do it here, in the TailPixel constructor.
        LXPoint lxPoint = new LXPoint(params.x, params.y, params.z);
        this.p = lxPoint;
        
        this.feather = this.params.feather;
        this.panel = this.params.panel;
    }

    @Override
    public int compareTo(TailPixel o) {
        int comparePosition = o.params.position;
        return comparePosition - this.params.position;
    }
    
    public Boolean isFeatherPixel() {
        return this.feather > 0;        
    }
    
    public Boolean isPanelPixel() {
        return this.panel > 0;
    }
    
    public Boolean isBodyPixel() {
        return this.panel==0 && this.feather==0 && this.params.controllerChannel == 13;
    }

    public Boolean isNeckPixel() {
        return this.panel==0 && this.feather==0 && this.params.controllerChannel == 14;
    }

    public Boolean isEyePixel() {
        return this.panel==0 && this.feather==0 && this.params.controllerChannel == 30;
    }

}
