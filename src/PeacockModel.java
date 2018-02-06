import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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

    public final List<BeagleboneController> controllers;
    public final List<Spiral> spirals;
    public final List<TailPixel> tailPixels;

    public Boolean isInitialized=false;

    public PeacockModel(LXFixture[] allFixtures, List<BeagleboneController> controllers, List<Spiral> spirals, List<TailPixel> tailPixels) {
        super(allFixtures);

        this.controllers = controllers;
        this.spirals = spirals;
        this.tailPixels = tailPixels;
    }

    ///////////////////////////////////////////////////////
    //Static members for loading configuration from files:

    //For CSV files:
    static public final String subSeparator = ";";

    public static PeacockModel LoadConfigurationFromFile() throws Exception
    {
        final List<BeagleboneController> controllers = new ArrayList<BeagleboneController>();
        final List<Spiral> spirals = new ArrayList<Spiral>();
        final List<TailPixel> tailPixels = new ArrayList<TailPixel>();

        final TreeMap<Integer,BeagleboneController> controllersDict = new TreeMap<Integer,BeagleboneController>();
        final HashMap<Integer,Spiral> spiralsDict = new HashMap<Integer,Spiral>();

        //Controllers
        List<ControllerParameters> cP = ReadControllersFromFile("./config/controllers.csv");
        for (ControllerParameters p : cP) {
            BeagleboneController newController = new BeagleboneController(p);
            controllers.add(newController);
            controllersDict.put(p.id, newController);
        }

        //Tail Pixels
        //List<TailPixelParameters> tpP = ReadTailPixelsFromFile("./config/tailpixels.csv");
        List<TailPixelParameters> tpP = ReadTailPixelsFromFile("./config/peacock-latest-edited.csv");
        for (TailPixelParameters p : tpP) {
            TailPixel newTailPixel = new TailPixel(p);
            tailPixels.add(newTailPixel);

            //Add to Controller
            //System.out.printf("%d", p.controllerID);												
            controllersDict.get(p.controllerID).AddTailPixel(newTailPixel);

            //Create the containing spiral if it does not exist.
            if (!spiralsDict.containsKey(p.spiral)) {
                Spiral newSpiral = new Spiral(p.spiral);
                spirals.add(newSpiral);
                spiralsDict.put(p.spiral, newSpiral);
            }

            //Add to Spiral
            //Spiral objects exist only for convenience in patterns.
            spiralsDict.get(p.spiral).addTailPixel(newTailPixel);
        }

        //The highest level of fixture is pretty arbitrary for the Peacock.  Using Spirals works fine.
        List<LXFixture> _fixtures = new ArrayList<LXFixture>(spirals);

        return new PeacockModel(_fixtures.toArray(new LXFixture[_fixtures.size()]), controllers, spirals, tailPixels);
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
            new ParseInt(), // int id;
            new ParseDouble(), // float x;
            new ParseDouble(), // float y;
            new ParseDouble(), // float z;
            new ParseInt(), // int address;
            new ParseInt(), // int panel;
            new ParseInt(), // int feather;
            new ParseInt(), // int rung;
            new ParseInt(), // int spiral;
            new ParseInt(), // int controllerID;
            new ParseInt(), // int controllerChannel;
            new ParseInt(), // int position;
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

                p.id = Integer.parseInt(c.get("id").toString());
                p.x = Double.parseDouble(c.get("x").toString());
                p.y = Double.parseDouble(c.get("y").toString());
                p.z = Double.parseDouble(c.get("z").toString());
                p.address = Integer.parseInt(c.get("address").toString());
                p.panel = Integer.parseInt(c.get("panel").toString());
                p.feather = Integer.parseInt(c.get("feather").toString());
                p.rung = Integer.parseInt(c.get("rung").toString());
                p.spiral = Integer.parseInt(c.get("spiral").toString());
                p.controllerID = Integer.parseInt(c.get("controllerID").toString());
                p.controllerChannel = Integer.parseInt(c.get("controllerChannel").toString());
                p.position = Integer.parseInt(c.get("position").toString());

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
