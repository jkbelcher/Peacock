import java.io.IOException;
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
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.LXStudio;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class PeacockCode extends PApplet implements LXOscListener {

    // Base unit is inches
    public final static float INCHES = 1;
    public final static float FEET = 12*INCHES;

    public static PeacockCode applet;

    // Hashmaps for bi-directional OSC routing
    HashMap<String, String> TouchOscToLXOsc = new HashMap<>();
    HashMap<String, String> LXOscToTouchOsc = new HashMap<>();
    
    // TouchOSC variables
    long lastOscHeartbeat = System.currentTimeMillis();
    boolean oscSendLock = false;
    
    // Top-level, we have a model and an LXStudio instance
    PeacockModel model;
    LXStudio lx;

    private final OscMessage oscMsg = new OscMessage("");

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
            lx.engine.osc.transmitActive.setValue(true);
            try {
                lx.engine.osc.receiver(8000).addListener(this);
                lx.engine.osc.receiver(3131).addListener(this);
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
        
        updatePatternList();
        
        // Just to get TouchOSC up-to-date. There's probably a much better way.
        lx.engine.getChannel(0).goNext();
        lx.engine.getChannel(0).goPrev();
        
        
    }
    
    public void patternChanged(LXChannel channel, LXPattern pattern) {
        PApplet.println("New Pattern selected: " + pattern.getLabel());
        updatePatternControls(channel, pattern);
    }
    
    public void updatePatternList() {
        // Update the TouchOSC pattern selection list 
        for (int ptToggleIndex = 1; ptToggleIndex <= 13; ptToggleIndex++)
        {
            if (ptToggleIndex > lx.engine.getChannel(0).getPatterns().size() - 1) {
                SendToTouchOSCclients("/patternlist/patternlabel"+ptToggleIndex+"/visible", 0);
                SendToTouchOSCclients("/patternlist/patterntoggle/"+ptToggleIndex+"/visible", 0);
            } else {
                if (ptToggleIndex == lx.engine.getChannel(0).getActivePatternIndex()) {
                    SendToTouchOSCclients("/patternlist/patterntoggle"+ptToggleIndex, 1);
                } else {
                    SendToTouchOSCclients("/patternlist/patterntoggle"+ptToggleIndex, 0);
                }
                
                SendToTouchOSCclients("/patternlist/patternlabel"+ptToggleIndex+"/visible", 1);
                SendToTouchOSCclients("/patternlist/patterntoggle/"+ptToggleIndex+"/visible", 1);
            }
        }
    }
    
    public void updatePatternControls(LXChannel channel, LXPattern pattern) {
        // Update touchOSC clients with current pattern parameters/values
        SendToTouchOSCclients("/patternname", pattern.getLabel());
        
        // Clear OSC address hashmaps
        TouchOscToLXOsc.clear();
        LXOscToTouchOsc.clear();
        
        for (LXPattern p: lx.engine.getChannel(0).getPatterns()) {
            if (p.getIndex() > 0) {
                SendToTouchOSCclients("/patternlist/patternlabel"+p.getIndex(), p.getLabel());
            }
        }
        
        //Hide all parameter controls, in case there are fewer in this pattern than the previous pattern.
        for (int pIndex = 1; pIndex < 8; pIndex++)
        {
            SendToTouchOSCclients("/paramlabel"+pIndex+"bool", "UNUSED");
            SendToTouchOSCclients("/paramlabel"+pIndex+"bool/visible", 0);
            SendToTouchOSCclients("/paramlabel"+pIndex+"fader", "UNUSED");
            SendToTouchOSCclients("/paramlabel"+pIndex+"fader/visible", 0);
            SendToTouchOSCclients("/paramcontrol"+pIndex+"bool/visible", 0);
            SendToTouchOSCclients("/paramcontrol"+pIndex+"fader/visible", 0);
        }
        
        //Set labels and type for this pattern's parameters
        int pIndex = 1;
        int pIndexBool = 1;
        int pIndexFader = 1;
        for (LXParameter p : pattern.getParameters()) {
            
            //Type
            if (p instanceof BooleanParameter) {
                //Label
                SendToTouchOSCclients("/paramlabel"+pIndex+"bool", p.getLabel());
                SendToTouchOSCclients("/paramlabel"+pIndex+"bool/visible", 1);

                int boolStatus = ((BooleanParameter) p).getValueb() ? 1 : 0;
                SendToTouchOSCclients("/paramcontrol"+pIndexBool+"bool", boolStatus);
                SendToTouchOSCclients("/paramcontrol"+pIndexBool+"bool/visible", 1);
                TouchOscToLXOsc.put("/paramcontrol"+pIndexBool+"bool", pattern.getOscAddress().toString()+"/"+p.getPath());
                LXOscToTouchOsc.put(pattern.getOscAddress().toString()+"/"+p.getPath(), "/paramcontrol"+pIndexBool+"bool");
                pIndexBool++;

            } else if (p instanceof CompoundParameter) {
                //Label
                SendToTouchOSCclients("/paramlabel"+pIndexFader+"fader", p.getLabel());
                SendToTouchOSCclients("/paramlabel"+pIndexFader+"fader/visible", 1);

                SendToTouchOSCclients("/paramcontrol"+pIndexFader+"fader", ((CompoundParameter) p).getNormalized());                
                SendToTouchOSCclients("/paramcontrol"+pIndexFader+"fader/visible", 1);
                TouchOscToLXOsc.put("/paramcontrol"+pIndexFader+"fader", pattern.getOscAddress().toString()+"/"+p.getPath());
                LXOscToTouchOsc.put(pattern.getOscAddress().toString()+"/"+p.getPath(), "/paramcontrol"+pIndexFader+"fader");
                pIndexFader++;
            }

            
        }
    }
    
    public void SendToTouchOSCclients(String address, String val) {
        oscMsg.clearArguments();
        oscMsg.setAddressPattern(address);
        oscMsg.add(val);
        sendMsg(oscMsg);
    }

    public void SendToTouchOSCclients(String address, double val) {
        oscMsg.clearArguments();
        oscMsg.setAddressPattern(address);
        oscMsg.add(val);
        sendMsg(oscMsg);
    }

    public void SendToTouchOSCclients(String address, int val) {
        oscMsg.clearArguments();
        oscMsg.setAddressPattern(address);
        oscMsg.add(val);
        sendMsg(oscMsg);
    }

    public void sendMsg(OscMessage msg) {
        try {
            if (!oscSendLock) {
                oscSendLock = true;
                lx.engine.osc.transmitter("192.168.1.215", 8080).send(oscMsg);
                oscSendLock = false;    
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public void oscMessage(OscMessage arg0) {
        String oscAddress = arg0.getAddressPattern().toString();
        String[] oscInAddressSplit = oscAddress.split("/");
        if (TouchOscToLXOsc.containsKey(oscAddress)) {
            String[] addressSplit = TouchOscToLXOsc.get(oscAddress).split("/");
            String pName = addressSplit[addressSplit.length - 1];
            lx.engine.getChannel(0).getActivePattern()
                .getParameter(pName)
                .setValue(arg0.getDouble());
        } else if (LXOscToTouchOsc.containsKey(oscAddress)) {
            SendToTouchOSCclients(LXOscToTouchOsc.get(oscAddress), arg0.getDouble());    
        } else if (oscAddress.equals("/1/prevpattern") && arg0.getBoolean()) {
            lx.engine.getChannel(0).goPrev();
        } else if (oscAddress.equals("/1/nextpattern") && arg0.getBoolean()) {
            lx.engine.getChannel(0).goNext();
        } else if (oscAddress.contains("/patternlist/patterntoggle")) {
            int patternIndex = Integer.parseInt(oscInAddressSplit[3]);
            lx.engine.getChannel(0).goIndex(patternIndex);
            updatePatternList();
        }
    }
    
    public void draw(){
        // Empty placeholder... LX handles everything for us!
        
        // Heartbeat to touchOSC in case a packet gets dropped
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOscHeartbeat > 1000.0) {
            updatePatternControls(lx.engine.getChannel(0), lx.engine.getChannel(0).getActivePattern());
            updatePatternList();
            lastOscHeartbeat = currentTime;
        }
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
