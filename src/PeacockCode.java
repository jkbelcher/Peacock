import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;

import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannel.Listener;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.StreamingACNDatagram;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.LXStudio;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class PeacockCode extends PApplet implements LXOscListener {

    // Base unit is inches
    public final static float INCHES = 1;
    public final static float FEET = 12*INCHES;

    public static PeacockCode applet;

    // For mapping pattern names to indexes.
    HashMap<String, Integer> PatternNameToIndex = new HashMap<>();

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

        // Patterns on channel 0
        PatternNameToIndex.put("panel"   , 1);
        PatternNameToIndex.put("feather" , 2);
        PatternNameToIndex.put("channel" , 3);
        PatternNameToIndex.put("spiral"  , 4);
        PatternNameToIndex.put("solid"   , 5);
        PatternNameToIndex.put("audio"   , 6);

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
                
                //Make patterns available in the browser
                lx.registerPattern(SolidColorPeacockPattern.class);
                lx.registerPattern(DashesPattern.class);
                lx.registerPattern(RainbowShiftPattern.class);
                lx.registerPattern(VUMeterPattern.class);
                lx.registerPattern(StrobePattern.class);
                lx.registerPattern(RisingSquaresPattern.class);
                lx.registerPattern(HorizontalSquaresPattern.class);
                lx.registerPattern(RainbowAmplitudePattern.class);
                
                //Add demo patterns to browser
                lx.registerPattern(DemoNormalCollectionPattern.class);
                lx.registerPattern(DemoChannelPattern.class);
                lx.registerPattern(DemoSpiralIDPattern.class);
                lx.registerPattern(DemoNormalPanelsLRPattern.class);
                lx.registerPattern(DemoNormalFeathersLRPattern.class);

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
                .addPattern(new RainbowShiftPattern(lx))
                .addPattern(new DashesPattern(lx))
                .addPattern(new VUMeterPattern(lx))
                .addPattern(new StrobePattern(lx))
                .addPattern(new RisingSquaresPattern(lx))
                .addPattern(new HorizontalSquaresPattern(lx))
                .addPattern(new AudioPeacockPattern(lx))
                .addPattern(new SolidColorPeacockPattern(lx))
                .addPattern(new RainbowAmplitudePattern(lx))
                .focusedPattern.setValue(1);
            lx.engine.getChannel(0).goNext();

            lx.engine.audio.enabled.setValue(true);
            lx.engine.audio.meter.gain.setValue(18);
            try {
                lx.engine.osc.receiver(8000).addListener(this);
            } catch (SocketException e) {
                e.printStackTrace();
            }

        }
        
        lx.engine.getChannel(0).addListener(new Listener() {

            @Override
            public void effectAdded(LXBus arg0, LXEffect arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void effectMoved(LXBus arg0, LXEffect arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void effectRemoved(LXBus arg0, LXEffect arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void indexChanged(LXChannel arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void patternAdded(LXChannel arg0, LXPattern arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void patternDidChange(LXChannel channel, LXPattern pattern) {
                patternChanged(channel, pattern);                
            }

            @Override
            public void patternMoved(LXChannel arg0, LXPattern arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void patternRemoved(LXChannel arg0, LXPattern arg1) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void patternWillChange(LXChannel arg0, LXPattern arg1, LXPattern arg2) {
                // TODO Auto-generated method stub
                
            }            
        });
    }
    
    public void patternChanged(LXChannel channel, LXPattern pattern) {
        PApplet.println("New Pattern selected: " + pattern.getLabel());
        //Send OSC updates to Parameters page in TouchOSC
        
        //Pattern name
        SendToTouchOSCclients("/patternname "+pattern.getLabel());
        
        //Hide all parameter controls, in case there are fewer in this pattern than the previous pattern.
        PApplet.println("Resetting controls...");
        for (int pIndex = 1; pIndex < 11; pIndex++)
        {
            SendToTouchOSCclients("/paramlabel"+pIndex+" UNUSED");
            SendToTouchOSCclients("/paramlabel"+pIndex+"/visible 0");
            SendToTouchOSCclients("/paramcontrol"+pIndex+"bool/visible 0");
            SendToTouchOSCclients("/paramcontrol"+pIndex+"fader/visible 0");                    
        }
        
        //Set labels and type for this pattern's parameters
        int pIndex = 1;
        for (LXParameter p : pattern.getParameters()) {            
            PApplet.println(p.getClass());
            //Label
            SendToTouchOSCclients("/paramlabel"+pIndex+" "+p.getLabel());
            SendToTouchOSCclients("/paramlabel"+pIndex+"/visible 1");
                                            
            //Type
            if (p instanceof BooleanParameter) {
                String boolStatus = ((BooleanParameter) p).getValueb() ? "1" : "0";
                SendToTouchOSCclients("/paramcontrol"+pIndex+"bool " + boolStatus);                
                SendToTouchOSCclients("/paramcontrol"+pIndex+"bool/visible 1");
            } else if (p instanceof CompoundParameter) {
                SendToTouchOSCclients("/paramcontrol"+pIndex+"fader " + p.getValue());                
                SendToTouchOSCclients("/paramcontrol"+pIndex+"fader/visible 1");
            }
            
            pIndex++;
        }
    }
    
    public void SendToTouchOSCclients(String command) {
        //TO-DO: send the command to client instead of printing.
        PApplet.println(command);        
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

    @Override
    public void oscMessage(OscMessage arg0) {
        String[] oscAddressArray = arg0.getAddressPattern().toString().split("/");
        // PApplet.println(oscAddressArray);
        // PApplet.println(arg0);
        String patternName = oscAddressArray[1];
        int patternIndex = PatternNameToIndex.get(patternName);

        if (oscAddressArray.length > 2) {
            String patternParam = oscAddressArray[2];
            // PApplet.println(patternParam);

            // Parse to integers where needed (can't send ints with osc)
            if (patternName.equals("channel") || patternName.equals("spiral")) {
                lx.engine.getChannel(0)
                    .getPattern(patternIndex)
                    .getParameter(patternParam)
                    .setValue(arg0.getInt(0));
            } else {
                lx.engine.getChannel(0)
                    .getPattern(patternIndex)
                    .getParameter(patternParam)
                    .setValue(arg0.getDouble(0));
            }
        } else {
            lx.engine.getChannel(0)
                .goIndex(patternIndex);
            lx.engine.getChannel(0)
                .focusedPattern
                .setValue(patternIndex);
        }
    }

}
