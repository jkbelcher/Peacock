import java.io.FileReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

/**
 * This model represents the entire Peacock puppet.
 * It contains lists of the logical lighted components on the puppet.
 */
public class PeacockModel extends LXModel {

    public final List<PeacockFixture> allPeacockFixtures;   //One fixture for each controller channel.  Panels and Feathers are fixtures.
    
    public final List<PeacockController> controllers;
    public final List<TailPixel> tailPixels;
    
    //Sub-collections of tail pixels
    //public final List<TailPixel> eyePixels;
    //public final List<TailPixel> panelPixels;

    //Logical groupings of tail pixels
    //Use these maps to find specific components by ID
    //public final AbstractMap<Integer, TailPixelGroup> spirals;		//*Probably can change this from map to list and just create them in order
    public final List<TailPixelGroup> feathers;
    public final List<TailPixelGroup> panels;
    
    //Normalized mappings
    //There are a bunch of different ways to group/order the spirals.
    //Use these objects to conveniently address pixels in a particular order, using a normalized 0..1 range.
    //Each group contains a list of pairs of [Tailpixel] + [normalized position within the group]
    public final TailPixelGroup feathersLR;
    public final TailPixelGroup panelsLR;
    
    //public final TailPixelGroup spiralsLR;
    //public final TailPixelGroup spiralsRL;
    //public final TailPixelGroup spiralsLOnly;
    //public final TailPixelGroup spiralsROnly;
    
    //Do normalized each direction overall spiral
    
    //Head Eye pixels
    //TO-DO: Add [head] eye pixels here.  They will load from a different .csv file    
       
    public Boolean isInitialized=false;

    public PeacockModel(LXFixture[] allFixtures, List<PeacockFixture> allPeacockFixtures, List<PeacockController> controllers, List<TailPixel> tailPixels) {
        super(allFixtures);

        this.allPeacockFixtures = allPeacockFixtures;
        this.controllers = controllers;
        this.tailPixels = tailPixels;
        
        //Sort TailPixels within each collection
        for (PeacockFixture fixture : this.allPeacockFixtures) {
            fixture.setLoaded();
        }
        
        //this.eyePixels = new ArrayList<TailPixel>();
        //this.panelPixels = new ArrayList<TailPixel>();
        //this.spirals = new TreeMap<Integer, TailPixelGroup>();
        this.feathers = new ArrayList<TailPixelGroup>();
        this.panels = new ArrayList<TailPixelGroup>();
        
        this.feathersLR = new TailPixelGroup();
        this.panelsLR = new TailPixelGroup();
        
        this.initializeSubCollections();
    }
    
    private void initializeSubCollections() {             	
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
        
        /*
        //Spirals (numbered from the original circular layout)
        for (TailPixel p : this.tailPixels) {
            //Create the containing spiral if it does not exist.
            if (!spirals.containsKey(p.params.spiral)) {
                TailPixelGroup newGroup = new TailPixelGroup();
                newGroup.addTailPixelPosition(new TailPixelPos(p));
                spirals.put(p.params.spiral, newGroup);
            }
        }
        */
    }
    
    protected PeacockModel computeNormalsPeacock() {
        //Positions are computed here, after the model is built and calculateNormals() has been called on it.
        //This is in case a collection wants to sort itself using a normalized value.
        
        //Sort by feather, then by position
        this.feathersLR.tailPixels.sort((p1,p2) -> p1.pixel.params.feather == p2.pixel.params.feather ? p2.pixel.params.position - p1.pixel.params.position : p1.pixel.params.feather - p2.pixel.params.feather);
        this.feathersLR.copyIndicesToChildren().calculateNormalsByIndex();

        this.panelsLR.tailPixels.sort((p1,p2) -> p1.pixel.params.panel == p2.pixel.params.panel ? Float.compare(p1.pixel.p.r, p2.pixel.p.r) : p1.pixel.params.panel - p2.pixel.params.panel);
        this.panelsLR.copyIndicesToChildren().calculateNormalsByIndex();
    	
    	return this;
    }

    ///////////////////////////////////////////////////////
    //Static members for loading configuration from files:

    //For CSV files:
    static public final String subSeparator = ";";

    public static PeacockModel LoadConfigurationFromFile() throws Exception
    {
        final List<PeacockFixture> allPeacockFixtures = new ArrayList<PeacockFixture>();
        final List<PeacockController> controllers = new ArrayList<PeacockController>();
        final List<TailPixel> tailPixels = new ArrayList<TailPixel>();

        //We use dictionaries to pair objects during the loading process
        final TreeMap<Integer,PeacockController> controllersDict = new TreeMap<Integer,PeacockController>();

        //Controllers
        List<ControllerParameters> cP = ReadControllersFromFile("./config/controllers.csv");
        for (ControllerParameters p : cP) {
            PeacockController newController = new PeacockController(p);
            controllers.add(newController);
            controllersDict.put(p.id, newController);
        }

        //Tail Pixels
        List<TailPixelParameters> tpP = ReadTailPixelsFromFile("./config/bestXandYsV3.csv");
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
            new ParseInt(), // numberOfChannels
            new ParseInt() // LEDsPerChannel
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
                p.numberOfChannels = Integer.parseInt(c.get("numberOfChannels").toString());
                p.LEDsPerChannel = Integer.parseInt(c.get("LEDsPerChannel").toString());

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
