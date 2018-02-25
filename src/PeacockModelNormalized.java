import java.util.ArrayList;
import java.util.List;

import heronarts.lx.model.LXFixture;

/**
 * This is a virtual wrapper around the PeacockModel
 * which allows interchanging of a target TailPixelGroup
 * without affecting the calling class.
 */
public class PeacockModelNormalized {

    public final PeacockModel model;
    
    private final List<TailPixelGroup> tailPixelGroups;    
    private int index;    
    private TailPixelGroup group;
    
    public PeacockModelNormalized(PeacockModel model) {
        this.model = model;
        
        tailPixelGroups = new ArrayList<TailPixelGroup>();
        tailPixelGroups.add(model.feathersLR);
        tailPixelGroups.add(model.panelsLR);
        tailPixelGroups.add(model.spiralsCW_IO);
        
        setIndex(0);
    }
    
    public int getIndex () {
        return this.index;
    }
    
    public PeacockModelNormalized setIndex(int index) {
        if (index < this.tailPixelGroups.size()) {
            this.index = index;
            this.group = this.tailPixelGroups.get(index);
        }
        return this;
    }
    
    public TailPixelGroup getTailPixelGroup() {
        return this.group;
    }
    
    public Object[] getTailPixelGroupArray() {
        //_fixtures.toArray(new LXFixture[_fixtures.size()])
        return this.tailPixelGroups.toArray();
    }
    
    public PeacockModelNormalized setTailPixelGroup(TailPixelGroup newGroup) {
        int newIndex = tailPixelGroups.indexOf(newGroup);
        
        if (newIndex > -1) {
            this.index = newIndex;
            this.group = newGroup;
        }
        
        return this;
    }
    
    public int numberTailPixelGroups() {
        return this.tailPixelGroups.size();
    }

    public PeacockModelNormalized goNext() {
        this.setIndex((index+1)%this.tailPixelGroups.size());
        return this;
    }
    
}
