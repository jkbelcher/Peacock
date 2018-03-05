import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.color.LXColor;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.StreamingACNDatagram;
import heronarts.p3lx.LXStudio;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class PeacockCode extends PApplet {

    // Base unit is inches
    public final static float INCHES = 1;
    public final static float FEET = 12*INCHES;

    public static PeacockCode applet;

    // Top-level, we have a model and an LXStudio instance
    PeacockModel model;
    LXStudio lx;

    //For "help" mode which helps define mapped/unmapped pixels
    private boolean isHelpMode = false;
    public static final List<StreamingACNDatagram2> datagrams = new ArrayList<StreamingACNDatagram2>();

    public static void main(String[] args) {
        //		PApplet.main("PeacockCode");
        PApplet.main(new String[] { "--present", PeacockCode.class.getName() });
    }

    public void settings(){
        size(displayWidth, displayHeight, P3D);
        pixelDensity(displayDensity());
        /*if (frame != null) {
          frame.setResizable(true);
        }
        */
    }

    public void setup(){
        PeacockCode.applet = this;

        //Monitor key events for Ctrl+Q = Quit, Ctrl+H = Help, etc.
        registerMethod("keyEvent", this);

        // Create the model, which describes where our light points are
        println("Loading config from file...");
        try {
            model = PeacockModel.LoadConfigurationFromFile("./config/controllers.csv", "./config/bigboi.csv");  //Big Peacock
            //model = PeacockModel.LoadConfigurationFromFile("./config/controllers.csv", "./config/bestXandYsV3.csv");  //Small Peacock
            PApplet.println("Loaded"
                    ,model.controllers.size() + " controllers,"
                    ,model.allPeacockFixtures.size() + " fixtures,"
                    ,model.tailPixels.size() + " tailPixels,"
                    ,"and " + model.points.length + " total pixels.");
        } catch (Exception e) {
            PApplet.println("Failure while loading model configuration from file.");
            e.printStackTrace();
            exit();
        }
        println("...finished loading config.");

        // Create the P3LX engine
        // Third parameter=true starts in Multi-threaded mode
        lx = new LXStudio(this, model, true)  {
            @Override
            protected void initialize(LXStudio lx, LXStudio.UI ui) {
                // Add custom LXComponents or LXOutput objects to the engine here,
                // before the UI is constructed

                /*
                //Example patterns from Joule:
                lx.registerPattern(SimpleChasePattern.class);
                lx.registerPattern(GemEdgePattern.class);
                */

                //Cast the model to access model-specific properties from within this overridden initialize() function.
                PeacockModel m = (PeacockModel)model;
                
                try {
                    //Create a UDP datagram for each output universe.
                    //Currently these are 1:1 with controller channels.
                    for (PeacockFixture fixture : m.allPeacockFixtures) {
                        int universe = fixture.channel;
                        if (universe > 0) {
                            int[] indicesForDatagram = fixture.getPointIndicesForOutput();                   
                            StreamingACNDatagram2 datagram = (StreamingACNDatagram2) new StreamingACNDatagram2(universe, indicesForDatagram)
                                .setAddress(fixture.controller.params.ipAddress)
                                .setPort(fixture.controller.params.port);
                            datagrams.add(datagram);
                        }
                    }    

                    //Create a UDP LXDatagramOutput to own these packets
                    LXDatagramOutput output = new LXDatagramOutput(lx);
                    for (StreamingACNDatagram2 dg : datagrams) {
                        output.addDatagram(dg);
                    }
                    
                    this.addOutput(output);		//Comment out for development
                    
                } catch (UnknownHostException e) {
                    println("Unknown Host Exception while constructing UDP output: " + e);
                    e.printStackTrace();
                } catch (SocketException e) {
                    println("Socket Exception while constructing UDP output: " + e);
                    e.printStackTrace();
                }
            }

            @Override
            protected void onUIReady(LXStudio lx, LXStudio.UI ui) {
                // The UI is now ready, can add custom UI components if desired
                ui.preview.addComponent(new UIWalls(model));
                ui.leftPane.engine.setVisible(true);
            }
        };

        //Use multi-threading for network output
        //lx.engine.output.mode.setValue(LXOutput.Mode.RAW);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.framesPerSecond.setValue(100);
        
        model.computeNormalsPeacock();

        if (!model.isInitialized) {
            model.isInitialized = true;

            //For development, initialize to desired pattern.
            lx.engine.getChannel(0)
                .addPattern(new DashesPattern(lx))
                .addPattern(new DemoNormalCollectionPattern(lx))
                .addPattern(new DemoNormalPanelsLRPattern(lx))
                .addPattern(new DemoNormalFeathersLRPattern(lx))
                .addPattern(new DemoChannelPattern(lx))
                .addPattern(new DemoSpiralIDPattern(lx))
                .addPattern(new SolidColorPeacockPattern(lx))
                .addPattern(new AudioPeacockPattern(lx))
                .focusedPattern.setValue(1);
            lx.engine.getChannel(0).goNext();

            lx.engine.audio.enabled.setValue(true);
            lx.engine.audio.meter.gain.setValue(18);

        }
    }

    public void draw(){
        // Empty placeholder... LX handles everything for us!
    }

    public void keyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        int action = keyEvent.getAction();
        if (action == KeyEvent.RELEASE) {
            switch (keyCode) {
                //Ctrl+Q to quit
                case java.awt.event.KeyEvent.VK_Q:
                    if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                        exit();
                    }
                    break;
                    //Ctrl+H for help mode On
                case java.awt.event.KeyEvent.VK_H:
                    if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                        setHelpModeOn();
                    }
                    break;
                    //Ctrl+J for help mode Off
                case java.awt.event.KeyEvent.VK_J:
                    if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                        setHelpModeOff();
                    }
                    break;
            }
        }
    }

    void setHelpModeOn() {
        /*for (LEDScapeDatagram output : PeacockCode.datagrams) {
          output.setBackgroundColor(LXColor.RED);
        }
        */
        this.isHelpMode = true;
        println("Help mode ON");
    }

    void setHelpModeOff() {
        /*
           for (LEDScapeDatagram output : PeacockCode.datagrams) {
           output.setBackgroundColor(LXColor.BLACK);
           }
           */
        this.isHelpMode = false;
        println("Help mode OFF");
    }

}
