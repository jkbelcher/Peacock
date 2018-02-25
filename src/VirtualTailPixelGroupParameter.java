import heronarts.lx.parameter.DiscreteParameter;

public class VirtualTailPixelGroupParameter extends DiscreteParameter {

    public PeacockModelNormalized modelN;
    
    public VirtualTailPixelGroupParameter(String label, PeacockModelNormalized modelN) {
        super(label, modelN.getTailPixelGroupArray());
        
        this.modelN = modelN;
    }

    public VirtualTailPixelGroupParameter(String arg0, int arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public VirtualTailPixelGroupParameter(String arg0, String[] arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public VirtualTailPixelGroupParameter(String arg0, Object[] arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public VirtualTailPixelGroupParameter(String arg0, int arg1, int arg2) {
        super(arg0, arg1, arg2);
        // TODO Auto-generated constructor stub
    }

    public VirtualTailPixelGroupParameter(String arg0, String[] arg1, int arg2) {
        super(arg0, arg1, arg2);
        // TODO Auto-generated constructor stub
    }

    public VirtualTailPixelGroupParameter(String arg0, int arg1, int arg2, int arg3) {
        super(arg0, arg1, arg2, arg3);
        // TODO Auto-generated constructor stub
    }

}
