
public class TailPixelPos {

	public final TailPixel pixel;
	public int index;
	public float n;    //Normalized position (0..1) within the parent TailPixelGroup
	
	public TailPixelPos(TailPixel pixel) {		
		this(pixel, 0);
	}
	
	public TailPixelPos(TailPixel pixel, int index) {
		this.pixel = pixel;
		this.index = index;
		this.n = 0;
	}

	public TailPixelPos setIndex(int index) {
		this.index = index;
		return this;
	}
	
	public TailPixelPos setNormal(float n) {
		this.n = n;
		return this;
	}
	
}
