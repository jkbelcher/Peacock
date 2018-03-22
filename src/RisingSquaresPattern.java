import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class RisingSquaresPattern extends PeacockPattern {

    public final DiscreteParameter numSquares = 
            new DiscreteParameter("Count", 30, 1, 100)
            .setDescription("Number of Squares");
    
    public final CompoundParameter avgSize = 
            new CompoundParameter("AvgSize", .08, .02, .4)
            .setDescription("Average size, as percentage of model width");
    
    public final CompoundParameter sizeVariance = 
            new CompoundParameter("SizeVariance", .03, 0, .2)
            .setDescription("Variance in sizes");
    
    public final CompoundParameter minSpeed = 
            new CompoundParameter("MinSpeed", .1, .01, 2)
            .setDescription("Minimum speed in normal ranges per second");
    
    public final CompoundParameter maxSpeed = 
            new CompoundParameter("MaxSpeed", 1, .05, 5)
            .setDescription("Maximum speed in normal ranges per second");
    
    private List<Square> squares;
    
    public RisingSquaresPattern(LX lx) {
        super(lx);

        addParameter(numSquares);
        addParameter(avgSize);
        addParameter(sizeVariance);
        addParameter(minSpeed);
        addParameter(maxSpeed);

        this.autoRandom.setValue(false);
    }

    public void setRandomParameters() {
        randomizeParameter(this.numSquares);
        randomizeParameter(this.avgSize);
        randomizeParameter(this.sizeVariance);
        randomizeParameter(this.minSpeed);
        randomizeParameter(this.maxSpeed);
    }
    
    public void onActive() {
        initialize();
    }

    private void initialize() {        
        this.squares = new ArrayList<Square>();
        
        int numSquares = this.numSquares.getValuei();
        
        for (int i = 0; i < numSquares; i++) {
            Square newSquare = createSquare();
            this.squares.add(newSquare);            
        }
    }
    
    private Square createSquare() {
        float avgSize = this.avgSize.getValuef();
        float sizeVar = this.sizeVariance.getValuef();
        
        Square s = new Square();        
        s.color = LXColor.hsb(Math.random() * 360.0,100,100);
        s.sizeX = (float) ((Math.random() * sizeVar) + avgSize - (sizeVar / 2));
        s.sizeY = s.sizeX * (model.xRange / model.yRange);
        s.xn = (float) (float)Math.random();
        s.yMin = 0 - (s.sizeY/2);
        s.yRange = s.sizeY + 1f;
        s.yMax = 1 + (s.sizeY/2);
        s.yn = s.yMin;
        s.calcXRange();
        s.calcYRange();
        
        float minSpeed = this.minSpeed.getValuef();
        float maxSpeed = this.maxSpeed.getValuef();
        float speedRange = Math.max(maxSpeed - minSpeed, 0.1f);
        float speed = (float) ((Math.random() * speedRange) + minSpeed);

        s.speed = speed;
        
        s.startTime = this.runMs;
        s.lifeTime = 1000f / s.speed;
        s.endTime = s.startTime + s.lifeTime;
                
        return s;
    }    
    
    @Override
    protected void run(double deltaMs) {
        this.clearColors();
        
        int numSquares = this.numSquares.getValuei();
        
        List<Square> expiredSquares = new ArrayList<Square>();

        // Foreach square: adjust current position.  Create new square if it's beyond max position.
        for (Square s : this.squares) {

            s.updatePosition(this.runMs);
            s.calcYRange();
            
            if (s.yn > s.yMax) {
                expiredSquares.add(s);
            }
        }
            
        for (Square expiredSquare : expiredSquares) {
            this.squares.remove(expiredSquare);
        }

        int numNewSquares = numSquares - this.squares.size();
        for (int n = 0; n < numNewSquares; n++) {
            this.squares.add(createSquare());
        }

        //Render every square
        for (int i = 0; i < model.tailPixels.size(); i++) {
            TailPixel tp = model.tailPixels.get(i);
            
            for (Square s : this.squares) {
                if (tp.p.xn > s.xLow && tp.p.xn < s.xHigh && tp.p.yn > s.yLow && tp.p.yn < s.yHigh) {
                    colors[tp.p.index] = s.color;
                }
            }
        }
    }
    
    public class Square {
        public int color;
        public float sizeX;     //Normalized.
        public float sizeY;     //Normalized. Different from X because model isn't square.
        public float speed;

        public float xn;
        public float yn;
        public float yRange;
        public float yMin;
        public float yMax;
        
        public float xLow;
        public float xHigh;
        public float yLow;
        public float yHigh;
        
        public double startTime;
        public double lifeTime;
        public double endTime;
        
        public void updatePosition(double currentTime) {
            this.yn = this.yMin + this.yRange * (float)((currentTime - startTime) / lifeTime);        
        }
        
        public void calcXRange() {
            xLow = xn - (sizeX / 2);
            xHigh = xn + (sizeX / 2);            
        }        
        
        public void calcYRange() {
            yLow = yn - (sizeY / 2);
            yHigh = yn + (sizeY / 2);            
        }        
    }

}
