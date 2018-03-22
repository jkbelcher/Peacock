import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;

/**
 * This model represents the entire Peacock puppet.
 * It contains lists of the logical lighted components on the puppet.
 */
public class PeacockModel extends LXModel {

    public final List<PeacockFixture> allPeacockFixtures;   //One fixture for each controller channel.  Panels and Feathers are fixtures.
    
    public final List<PeacockController> controllers;
    public final List<TailPixel> tailPixels;
    
    //Logical groupings of pixels
    //Use these maps to find specific components by ID
    public final List<TailPixelGroup> feathers;
    public final List<TailPixelGroup> panels;
    public final TailPixelGroup body;
    public final TailPixelGroup neck;
    public final TailPixelGroup eyes;
    
    //Normalized mappings
    //There are a bunch of different ways to group/order the spirals.
    //Use these objects to conveniently address pixels in a particular order, using a normalized 0..1 range.
    //Each group contains a list of pairs of [Tailpixel] + [normalized position within the group]
    public final TailPixelGroup feathersLR;
    public final TailPixelGroup panelsLR;
    public final TailPixelGroup spiralsCW_IO;
    public final TailPixelGroup spiralsCCW_IO;        
    //TO-DO: normalized each direction overall spiral
    
    //Head Eye pixels
    //TO-DO: Add [head] eye pixels here.  They will load from a different .csv file    
       
    public PeacockModel(LXFixture[] allFixtures, List<PeacockFixture> allPeacockFixtures, List<PeacockController> controllers, List<TailPixel> tailPixels) {
        super(allFixtures);

        this.allPeacockFixtures = allPeacockFixtures;
        this.controllers = controllers;
        this.tailPixels = tailPixels;
        
        //Sort TailPixels within each collection
        Collections.sort(this.allPeacockFixtures);
        for (PeacockFixture fixture : this.allPeacockFixtures) {
            fixture.setLoaded();
        }
        
        //Logical groups
        this.feathers = new ArrayList<TailPixelGroup>();
        this.panels = new ArrayList<TailPixelGroup>();
        this.body = new TailPixelGroup();
        this.neck = new TailPixelGroup();
        this.eyes = new TailPixelGroup();
        
        //Normalized mappings
        this.feathersLR = new TailPixelGroup();
        this.panelsLR = new TailPixelGroup();
        this.spiralsCW_IO = new TailPixelGroup();
        this.spiralsCCW_IO = new TailPixelGroup();
        
        this.initializeSubCollections();
    }
    
    private void initializeSubCollections() {
        //Feathers
        for (int i = 1; i <= 13; i++) {
            this.feathers.add(new TailPixelGroup(i));            
        }
        for (TailPixel p : this.tailPixels) {
            if (p.isFeatherPixel()) {
                this.feathers.get(p.feather-1).addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //Panels
        for (int i = 1; i <= 12; i++) {
            this.panels.add(new TailPixelGroup(i));            
        }
        for (TailPixel p : this.tailPixels) {
            if (p.isPanelPixel()) {
                this.panels.get(p.panel-1).addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //Body
        for (TailPixel p : this.tailPixels) {
            if (p.isBodyPixel()) {
                this.body.addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //Neck
        for (TailPixel p : this.tailPixels) {
            if (p.isNeckPixel()) {
                this.neck.addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //Eyes
        for (TailPixel p : this.tailPixels) {
            if (p.isEyePixel()) {
                this.eyes.addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
                
    	//FeathersLR
        for (TailPixel p : this.tailPixels) {
            if (p.isFeatherPixel()) {                
                this.feathersLR.addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //PanelsLR
        for (TailPixel p : this.tailPixels) {
            if (p.isPanelPixel()) {                
                this.panelsLR.addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //SpiralsCW_IO = Spirals, Clockwise, Inside->Outside
        for (TailPixel p : this.tailPixels) {
            if (p.isPanelPixel() && p.params.spiral % 2 == 0) {                
                this.spiralsCW_IO.addTailPixelPosition(new TailPixelPos(p));
            }
        }
        
        //SpiralsCCW_IO = Spirals, Counter-Clockwise, Inside->Outside
        for (TailPixel p : this.tailPixels) {
            if (p.isPanelPixel() && p.params.spiral % 2 != 0) {                
                this.spiralsCCW_IO.addTailPixelPosition(new TailPixelPos(p));
            }
        }        

    }
    
    protected PeacockModel computeNormalsPeacock() {
        //Positions are computed here, after the model is built and calculateNormals() has been called on it.
        //This is in case a collection wants to sort itself using a normalized value.

        //Feathers
        for (TailPixelGroup g : this.feathers) {
            g.tailPixels.sort((p1,p2) -> Float.compare(p1.getPoint().r, p2.getPoint().r));
            g.copyIndicesToChildren().calculateNormalsByIndex();
        }
        
        //Panels
        for (TailPixelGroup g : this.panels) {
            g.tailPixels.sort((p1,p2) -> Float.compare(p1.getPoint().r, p2.getPoint().r));
            g.copyIndicesToChildren().calculateNormalsByIndex();
        }

        //Body
        this.body.tailPixels.sort((p1,p2) -> p2.getPosition() - p1.getPosition());
        this.body.copyIndicesToChildren().calculateNormalsByIndex();
                
        //Neck
        this.neck.tailPixels.sort((p1,p2) -> p2.getPosition() - p1.getPosition());
        this.neck.copyIndicesToChildren().calculateNormalsByIndex();
        
        //Eyes
        this.eyes.tailPixels.sort((p1,p2) -> p2.getPosition() - p1.getPosition());
        this.eyes.copyIndicesToChildren().calculateNormalsByIndex();
        
        this.feathersLR.tailPixels.sort((p1,p2) -> p1.getFeather() == p2.getFeather() ? p1.getPosition() - p2.getPosition() : p1.getFeather() - p2.getFeather());
        this.feathersLR.copyIndicesToChildren().calculateNormalsByIndex();

        this.panelsLR.tailPixels.sort((p1,p2) -> p1.getPanel() == p2.getPanel() ? Float.compare(p1.getPoint().r, p2.getPoint().r) : p1.getPanel() - p2.getPanel());
        this.panelsLR.copyIndicesToChildren().calculateNormalsByIndex();
        
        this.spiralsCW_IO.tailPixels.sort((p1,p2) -> p1.getSpiral() == p2.getSpiral() ? p2.getPosition() - p1.getPosition() : p2.getSpiral() - p1.getSpiral());
        this.spiralsCW_IO.copyIndicesToChildren().calculateNormalsByIndex();    //*Could do normals by position and not by spiral.

        this.spiralsCCW_IO.tailPixels.sort((p1,p2) -> p1.getSpiral() == p2.getSpiral() ? p2.getPosition() - p1.getPosition() : p2.getSpiral() - p1.getSpiral());
        this.spiralsCCW_IO.copyIndicesToChildren().calculateNormalsByIndex();

    	return this;
    }

    ///////////////////////////////////////////////////////
    //Static members for loading configuration from files:

    //For CSV files:
    static public final String subSeparator = ";";

    public static PeacockModel LoadConfigurationFromFile(String controllerFile, String pixelFile) throws Exception
    {
        final List<PeacockFixture> allPeacockFixtures = new ArrayList<PeacockFixture>();
        final List<PeacockController> controllers = new ArrayList<PeacockController>();
        final List<TailPixel> tailPixels = new ArrayList<TailPixel>();

        //We use dictionaries to pair objects during the loading process
        final TreeMap<Integer,PeacockController> controllersDict = new TreeMap<Integer,PeacockController>();

        //Controllers
        List<ControllerParameters> cP = ReadControllersFromFile(controllerFile);
        for (ControllerParameters p : cP) {
            PeacockController newController = new PeacockController(p);
            controllers.add(newController);
            controllersDict.put(p.id, newController);
        }

        //Tail Pixels
        List<TailPixelParameters> tpP = ReadTailPixelsFromFile(pixelFile);
        for (TailPixelParameters p : tpP) {
            TailPixel newTailPixel = new TailPixel(p);
            tailPixels.add(newTailPixel);

            PeacockController controller = controllersDict.get(p.controllerID);

            //Create the containing fixture if it does not exist.
            PeacockFixture fixture;
            if (!controller.containsFixture(p.controllerChannel)) {
                fixture = new PeacockFixture(p.controllerChannel, controller);
                controller.addFixture(fixture);
                allPeacockFixtures.add(fixture);
            } else {
                fixture = controller.getFixture(p.controllerChannel);
            }

            //Add pixel to containing fixture.
            //This subsequently calls the important model loading method LXAbstractFixture.addPoint(LXPoint)
            fixture.AddTailPixel(newTailPixel);
        }

        //LX wants a list of fixtures that as a whole contains one instance of each LXPoint.
        List<LXFixture> _fixtures = new ArrayList<LXFixture>(allPeacockFixtures);

        return new PeacockModel(_fixtures.toArray(new LXFixture[_fixtures.size()]), allPeacockFixtures, controllers, tailPixels);
    }

    private static CellProcessor[] getControllerCsvProcessors() {
        return new CellProcessor[] {
            new UniqueHashCode(), // id (must be unique)
            new NotNull(), // ipAddress
            new ParseInt(), // port
        };
    }

    protected static List<ControllerParameters> ReadControllersFromFile(String filename) throws Exception {

        final ArrayList<ControllerParameters> results = new ArrayList<ControllerParameters>();

        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);

            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            final CellProcessor[] processors = getControllerCsvProcessors();

            Map<String, Object> c;
            while((c = mapReader.read(header, processors)) != null) {
                ControllerParameters p = new ControllerParameters();
                p.id = Integer.parseInt(c.get("id").toString());
                p.ipAddress = c.get("ipAddress").toString();
                p.port = Integer.parseInt(c.get("port").toString());

                results.add(p);
            }
        }
        finally {
            if(mapReader != null) {
                mapReader.close();
            }
        }

        return results;
    }

    private static CellProcessor[] getTailPixelCsvProcessors() {
        return new CellProcessor[] {
            new ParseInt(), // int controllerID;
            new ParseInt(), // int controllerChannel;
            new ParseInt(), // int panel;
            new ParseInt(), // int spiral;
            new ParseInt(), // int position;
            new ParseInt(), // int feather;
            new ParseInt(), // int rung;
            new ParseDouble(), // float x;
            new ParseDouble(), // float y;
            new ParseDouble(), // float z;
        };
    }

    protected static List<TailPixelParameters> ReadTailPixelsFromFile(String filename) throws Exception {

        final ArrayList<TailPixelParameters> results = new ArrayList<TailPixelParameters>();

        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);

            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            final CellProcessor[] processors = getTailPixelCsvProcessors();

            Map<String, Object> c;
            while((c = mapReader.read(header, processors)) != null) {
                TailPixelParameters p = new TailPixelParameters();

                p.controllerID = Integer.parseInt(c.get("controllerID").toString());
                p.controllerChannel = Integer.parseInt(c.get("controllerChannel").toString());
                p.panel = Integer.parseInt(c.get("panel").toString());
                p.spiral = Integer.parseInt(c.get("spiralNum").toString());
                p.position = Integer.parseInt(c.get("position").toString());
                p.feather = Integer.parseInt(c.get("Feather").toString());
                p.rung = Integer.parseInt(c.get("Rung").toString());
                p.x = Double.parseDouble(c.get("X in Inches").toString());
                p.y = Double.parseDouble(c.get("Y in Inches").toString());
                p.z = Double.parseDouble(c.get("Z in Inches").toString());

                results.add(p);
            }
        }
        finally {
            if(mapReader != null) {
                mapReader.close();
            }
        }

        return results;
    }

}
