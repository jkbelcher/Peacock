import java.util.ArrayList;
import java.util.List;

import heronarts.lx.model.LXAbstractFixture;

public class TailPixelGroup extends LXAbstractFixture {

	public final List<TailPixelPos> tailPixels;
	public int id = 0;
	
	public TailPixelGroup() {
	    this(0);
	}

	public TailPixelGroup(int id) {
	    this.id=id;
	    tailPixels = new ArrayList<TailPixelPos>();
    }

	public TailPixelGroup addTailPixelPosition(TailPixelPos newItem) {
		this.tailPixels.add(newItem);
		this.addPoint(newItem.getPoint());
		return this;
	}
	
	public TailPixelGroup copyIndicesToChildren() {
	    for (int i = 0; i<this.tailPixels.size(); i++) {
	        this.tailPixels.get(i).setIndexGroup(i);
	    }
	    return this;
	}	
	
	public TailPixelGroup calculateNormalsByIndex()	{
		//Once all children have been added to the group,
		//calculate the normalized positions of children based on index
		
		//Justin's thoughts: The range of normals will be 0..1.  Let's have 0 include no points
		//and 1 include all points.  To do this, the first normalized position will be 1/[number of pixels].
		//The last normalized position will be [number of pixels]/[number of pixels] = 1.
		//No pixel within the group will have a normalized position of zero.
		//Note this is true for our TailPixelGroups but not necessarily for other normalized
		//positions in the LX framework.
		
		float numPixels = (float)this.tailPixels.size();
		
		for (TailPixelPos item : this.tailPixels) {
			item.setNormal(((float)(item.getIndexGroup()+1))/numPixels);			
		}		
		
		return this;
	}

	public int size() {
	    return this.tailPixels.size();
	}
}
