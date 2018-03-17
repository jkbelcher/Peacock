import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.BooleanParameter.Mode;

public class VUMeterPattern extends PeacockPattern {

    /////////////////////////////////////////////////
    // Pass-through parameters for two GraphicMeters
    /**
    * Gain of the meter, in decibels
    */
    public final BoundedParameter gain = (BoundedParameter)
        new BoundedParameter("Gain", 10, -48, 48)
        .setDescription("Sets the gain of the meter in dB")
        .setUnits(LXParameter.Units.DECIBELS);

    /**
    * Range of the meter, in decibels.
    */
    public final BoundedParameter range = (BoundedParameter)
        new BoundedParameter("Range", 48, 6, 96)
        .setDescription("Sets the range of the meter in dB")
        .setUnits(LXParameter.Units.DECIBELS);
        
    /**
    * Meter attack time, in milliseconds
    */
    public final BoundedParameter attack = (BoundedParameter)
        new BoundedParameter("Attack", 10, 0, 100)
        .setDescription("Sets the attack time of the meter response")
        .setUnits(LXParameter.Units.MILLISECONDS);
        
    /**
    * Meter release time, in milliseconds
    */
    public final BoundedParameter release = (BoundedParameter)
        new BoundedParameter("Release", 100, 0, 1000)
        .setDescription("Sets the release time of the meter response")
        .setExponent(2)
        .setUnits(LXParameter.Units.MILLISECONDS);
      
    public final BoundedParameter releasePeaks = (BoundedParameter)
        new BoundedParameter("ReleasePeaks", 400, 0, 1000)
        .setDescription("Sets the release time of the peaks")
        .setExponent(2)
        .setUnits(LXParameter.Units.MILLISECONDS);    
          
    /////////////////////////////////////////////////
      
    public final CompoundParameter hueRange = 
            new CompoundParameter("HueRange", 100, 10, 360)
            .setDescription("Hue range");
    
    public final CompoundParameter hueShiftSpeed = 
            new CompoundParameter("HueShift", 5, 0, 60)
            .setDescription("Hue shift speed");
    
    public final BooleanParameter mirror = 
            new BooleanParameter("Mirror", false)
            .setDescription("When ENABLED, the pattern runs in a mirror image from both sides.")
            .setMode(Mode.TOGGLE);
    
    GraphicMeter meter;
    GraphicMeter meterPeaks;
    List<GroupBandPair> pairs;
    
    float huePos = 0;
    
    public VUMeterPattern(LX lx) {
        super(lx);
        
        addParameter(hueRange);
        addParameter(hueShiftSpeed);
        addParameter(mirror);
        
        // For GraphicMeters
        addParameter(gain);
        addParameter(range);
        addParameter(attack);
        addParameter(release);
        addParameter(releasePeaks);

        initialize();
        
        this.gain.addListener(new LXParameterListener() {
              public void onParameterChanged(LXParameter p) {
                  onGainChanged();
              }
            });
        this.range.addListener(new LXParameterListener() {
              public void onParameterChanged(LXParameter p) {
                  onRangeChanged();
              }
            });
        this.attack.addListener(new LXParameterListener() {
              public void onParameterChanged(LXParameter p) {
                  onAttackChanged();
              }
            });
        this.release.addListener(new LXParameterListener() {
              public void onParameterChanged(LXParameter p) {
                  onReleaseChanged();
              }
            });
        this.releasePeaks.addListener(new LXParameterListener() {
              public void onParameterChanged(LXParameter p) {
                  onReleasePeaksChanged();
              }
            });
    }
    
    void onGainChanged() {
        this.meter.gain.setValue(this.gain.getValue());
        this.meterPeaks.gain.setValue(this.gain.getValue());
    }   
    void onRangeChanged() {
        this.meter.range.setValue(this.attack.getValue());
        this.meterPeaks.range.setValue(this.attack.getValue());
    }   
    void onAttackChanged() {
        this.meter.attack.setValue(this.attack.getValue());
        this.meterPeaks.attack.setValue(this.attack.getValue());
    }   
    void onReleaseChanged() {
        this.meter.release.setValue(this.release.getValue());
        if (this.meter.release.getValue()>this.meterPeaks.release.getValue())
            this.releasePeaks.setValue(this.meter.release.getValue());
    }   
    void onReleasePeaksChanged() {
        this.meterPeaks.release.setValue(this.releasePeaks.getValue());
    }
    
    private void initialize() {        
        int numMeterBands =  model.panels.size();

        this.meter = new GraphicMeter(lx.engine.audio.input.mix, numMeterBands);
        this.meterPeaks = new GraphicMeter(lx.engine.audio.input.mix, numMeterBands);
        this.meter.gain.setValue(this.gain.getValue());
        this.meterPeaks.gain.setValue(this.gain.getValue());
        this.range.setValue(this.meter.range.getValue());
        this.meter.attack.setValue(this.attack.getValue());
        this.meterPeaks.attack.setValue(this.attack.getValue());
        this.meter.release.setValue(this.release.getValue());
        this.meterPeaks.release.setValue(this.releasePeaks.getValue());        
        
        startModulator(this.meter);
        startModulator(this.meterPeaks);

        // Pair every panel to a band in the GraphicMeter
        this.pairs = new ArrayList<GroupBandPair>();
        float hueRangePerPanel = 1f / (float)model.panels.size();
        
        for (int i=0; i < model.panels.size(); i++) {
            this.pairs.add(new GroupBandPair(model.panels.get(i), i, hueRangePerPanel * (float)i));
        }
    }    

    @Override
    protected void run(double deltaMs) {
        this.clearColors();
        
        float hueRange = this.hueRange.getValuef();

        float hueShiftSpeed = this.hueShiftSpeed.getValuef();
        float hueChange = hueShiftSpeed*100/60.0f/1000.0f*((float)deltaMs);
                  
        this.huePos += hueChange;
        this.huePos %= 360;
        
        boolean mirror = this.mirror.getValueb();
                
        // This loop may look a little muddy.  It was rushed.        
        for (GroupBandPair gb : this.pairs) {
            float percent = this.meter.getBandf(gb.iBand);
            float numPixelsf = mirror ? ((float)gb.group.size()) / 2f : (float)gb.group.size(); 
            int numPixels = (int)numPixelsf; 
            float litPixelsf = numPixelsf * percent;
            float percentLastPixel = litPixelsf - ((int)litPixelsf);
            int litPixels = (int) litPixelsf;
            
            float hue = ((gb.hueOffset * hueRange) + this.huePos) % 360;
            int color = LXColor.hsb(hue, 100, 100);         
            
            for (int iPixel = 0; iPixel < litPixels; iPixel++) {                
                colors[gb.group.tailPixels.get(iPixel).getIndexColor()] = color;
                if (mirror) {
                    colors[gb.group.tailPixels.get(gb.group.size()-1-iPixel).getIndexColor()] = color;
                }
            }
            if (litPixels < numPixels) {
                colors[gb.group.tailPixels.get(litPixels).getIndexColor()] = LXColor.scaleBrightness(color, percentLastPixel);
                if (mirror)
                    colors[gb.group.tailPixels.get(gb.group.size()-1-litPixels).getIndexColor()] = LXColor.scaleBrightness(color, percentLastPixel);                    
            }
            
            // Get peaks from a meter with a slower release
            float peakPercent = this.meterPeaks.getBandf(gb.iBand);
            float iPeakPixelf = (numPixelsf - 1f) * peakPercent;
            int iPeakPixel = (int) iPeakPixelf;
            if (iPeakPixel < numPixels && mirror) {
                iPeakPixel++;               
            }
            colors[gb.group.tailPixels.get(iPeakPixel).getIndexColor()] = LXColor.WHITE;
            if (mirror)
                colors[gb.group.tailPixels.get(gb.group.size()-1-iPeakPixel).getIndexColor()] = LXColor.WHITE;                
        }
    }

    private class GroupBandPair {
        public TailPixelGroup group;
        public int iBand;
        public float hueOffset;
        
        public GroupBandPair (TailPixelGroup edge, int iBand, float hueOffset) {
            this.group = edge;
            this.iBand = iBand;
            this.hueOffset = hueOffset;
        }        
    }

}
