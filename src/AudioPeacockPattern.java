import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.audio.LXAudioComponent;
import heronarts.lx.audio.FourierTransform;

public class AudioPeacockPattern extends PeacockPattern {
    public AudioPeacockPattern(LX lx) {
        super(lx);
    }

    int buffLength = lx.engine.audio.getInput().left.bufferSize();
    int sampleRate = lx.engine.audio.getInput().left.sampleRate();
    FourierTransform fftLeft = new FourierTransform(buffLength, sampleRate);
    FourierTransform fftRight = new FourierTransform(buffLength, sampleRate);
    float[] leftSamples = new float[buffLength];
    float[] rightSamples = new float[buffLength];
    float[] leftFft = new float[buffLength];
    float[] rightFft = new float[buffLength];

    @Override
    public void run(double deltaMs) {
        // float hue = this.hue.getValuef();
        float meter = lx.engine.audio.meter.getValuef();
        // float rmsLeft = lx.engine.audio.getInput().left.getRms();
        // float rmsRight = lx.engine.audio.getInput().right.getRms();
        // float hb = lx.engine.audio.meter.getBandf(6);
        // float lb = lx.engine.audio.meter.getBandf(1);
        lx.engine.audio.getInput().left.getSamples(leftSamples);
        lx.engine.audio.getInput().right.getSamples(rightSamples);
        fftLeft.compute(leftSamples);
        fftRight.compute(rightSamples);

        int numBands = fftLeft.getNumBands();
        float[] leftBandFloats = new float[numBands];
        float[] rightBandFloats = new float[numBands];

        for (int i=0; i<numBands; i++) {
            leftBandFloats[i] = fftLeft.getBand(i);
            rightBandFloats[i] = fftRight.getBand(i);
        }

        for (TailPixel tp : this.model.tailPixels) {
            if (tp.feather == 0){
                int color = LXColor.hsb(360, 50, rightBandFloats[(30 - tp.params.rung) % numBands] * 100.0);
                colors[tp.p.index] = color;
            } else if (tp.feather == 6) {
                int color = LXColor.hsb(360, 0, leftBandFloats[0] * 100.0);
                colors[tp.p.index] = color;
            } else if (tp.feather > 6) {
                int color = LXColor.hsb(360, 0, leftBandFloats[tp.feather - 7] * 100.0);
                colors[tp.p.index] = color;
            } else if (tp.feather < 6) {
                int color = LXColor.hsb(360, 0, leftBandFloats[7 - tp.feather] * 100.0);
                colors[tp.p.index] = color;
            }
        }
    }

}
