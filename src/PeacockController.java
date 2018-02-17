import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import heronarts.lx.model.LXAbstractFixture;
import processing.core.PApplet;

public class PeacockController extends LXAbstractFixture {

    public final ControllerParameters params;
    public final List<PeacockFixture> fixtures;
    public final TreeMap<Integer,PeacockFixture> fixturesDict;

    public PeacockController(ControllerParameters params) {
        this.params = params;
        this.fixtures = new ArrayList<PeacockFixture>();
        this.fixturesDict = new TreeMap<Integer,PeacockFixture>();
        
        PApplet.println("Controller "+this.params.id+": "+this.params.ipAddress+":"+this.params.port);
    }
    
    public PeacockController addFixture(PeacockFixture fixture) {
        this.fixtures.add(fixture);
        this.fixturesDict.put(fixture.channel, fixture);
        
        return this;
    }
    
    public PeacockFixture getFixture(int key) {
        return this.fixturesDict.get(key);
    }
    
    public Boolean containsFixture(int key) {
        return this.fixturesDict.containsKey(key);
    }

}
