import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.audio.LXAudioComponent;
import heronarts.lx.audio.FourierTransform;

public class RainbowAmplitudePattern extends PeacockPattern {

    public final BooleanParameter lrSplit =
        new BooleanParameter("L/R Split", false)
            .setDescription("When ENABLED, left/right audio channels will map only to the pixels from each corresponding side.")
            .setMode(BooleanParameter.Mode.TOGGLE);

    public final CompoundParameter bandGate =
        new CompoundParameter("BandGate", 4.5f, 0f, 8f)
            .setDescription("Any value below this in FFT bands will effectively be gated");

    float colorValue;

    public RainbowAmplitudePattern(LX lx) {
        super(lx);

        addParameter(lrSplit);
        addParameter(bandGate);
    }

    int buffLength = lx.engine.audio.getInput().left.bufferSize();
    int sampleRate = lx.engine.audio.getInput().left.sampleRate();
    int color;

    FourierTransform fftMix = new FourierTransform(buffLength, sampleRate);
    FourierTransform fftLeft = new FourierTransform(buffLength, sampleRate);
    FourierTransform fftRight = new FourierTransform(buffLength, sampleRate);
    float[] mixSamples = new float[buffLength];
    float[] leftSamples = new float[buffLength];
    float[] rightSamples = new float[buffLength];
    float[] currentSample = new float[buffLength];

    @Override
    public void run(double deltaMs) {
        float meter = lx.engine.audio.meter.getValuef();

        // effectively discard the top of half of FFT bands
        int numBands = fftMix.getNumBands() / 2;
        float[] mixBandFloats = new float[numBands];
        float[] leftBandFloats = new float[numBands];
        float[] rightBandFloats = new float[numBands];

        if (lrSplit.getValueb() == false) {
            lx.engine.audio.getInput().mix.getSamples(mixSamples);
            fftMix.compute(mixSamples);
            for (int i=0; i < numBands; i++) {
                mixBandFloats[i] = (fftMix.getBand(i) >= bandGate.getValuef()) ? fftMix.getBand(i) : 0;
            }
        } else {
            lx.engine.audio.getInput().left.getSamples(leftSamples);
            lx.engine.audio.getInput().right.getSamples(rightSamples);
            fftLeft.compute(leftSamples);
            fftRight.compute(rightSamples);
            for (int i=0; i < numBands; i++) {
                leftBandFloats[i] = (fftLeft.getBand(i) >= bandGate.getValuef()) ? fftLeft.getBand(i) : 0;

                rightBandFloats[i] = (fftRight.getBand(i) >= bandGate.getValuef()) ? fftRight.getBand(i) : 0;
            }
        }

        for (TailPixel tp : this.model.tailPixels) {
            colorValue = (float)(Math.sqrt(Math.pow(tp.p.y, 2) + Math.pow(tp.p.x, 2)) / ((model.xMax / model.yMax) * model.xMax)) * 360;
            if (lrSplit.getValueb() == false) {
                currentSample = mixBandFloats;
            } else {
                currentSample = (tp.p.x < 0) ? leftBandFloats : rightBandFloats;
            }
            if (tp.feather == 0) {
                color = LXColor.hsb(colorValue, 100, currentSample[(30 - tp.params.rung) % numBands] * 100.0);
            } else if (tp.feather == 6) {
                color = LXColor.hsb(colorValue, 100, currentSample[0] * 100.0);
            } else if (tp.feather > 6) {
                color = LXColor.hsb( colorValue, 100, currentSample[tp.feather - 7] * 100.0);
            } else if (tp.feather < 6) {
                color = LXColor.hsb( colorValue, 100, currentSample[7 - tp.feather] * 100.0);
            } else {
                continue;
            }
            colors[tp.p.index] = color;
        }
    }
}
