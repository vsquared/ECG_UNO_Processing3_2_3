import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import controlP5.*; 
import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ecg_1 extends PApplet {

Serial port;
PrintWriter output;

ControlP5 cp5;
Textarea valueFld;
Textlabel logFld;

PFont font;
final int fntSize = 11;
final int menuBarW = 65;
int gray = color(0, 160, 100);

String[] data;
String fileName;

String portName;
int baudRate;
int[] baud = {9600, 14400, 19200, 28800, 38400, 57600, 115200};
int itemSelected;

int index = 0;
int counter = 0;
int count = 0;
int inByte = 0;

float yIn;
float x1, y1, x2, y2;
float x3, y3, x4, y4;

int BLUE = color(12, 16, 255);

boolean connected = false;
boolean showGraph = false;
boolean showText = false;

int[] y = new int[0];

public String getDateTime()
{
 int s = second();
 int m = minute();
 int h = hour();
 int day = day();
 int mo = month();
 int yr = year();

// Avoid slashes which create folders
String date = nf(mo,2)+nf(day,2)+yr+"_";
String time = nf(h,2)+nf(m,2)+nf(s,2);
String dateTime = date+time;
return dateTime;
}

public void setup() {
 
 if (frame != null) {
   surface.setResizable(true);
 }
 background(BLUE);
 
 cp5 = new ControlP5(this);
 font = createFont("SourceCodePro-Regular.tif", fntSize);
 String[] ports = Serial.list();
 List p = Arrays.asList(ports);
 
 cp5.addScrollableList("SerialPorts")
     .setPosition(10, 3)
     .setSize(230, 90)
     .setCaptionLabel("Serial Ports")
     .setBarHeight(18)
     .setItemHeight(18)
     .setFont(font)
     .addItems(p);
      
 List b = Arrays.asList("9600","14400","19200","28800","38400","57600","115200");      
 cp5.addScrollableList("Baud")    
      .setPosition(250, 3)
      .setSize(60,90)
      .setBarHeight(18)
      .setItemHeight(18)
      .setFont(font)
      .addItems(b); 
   
 cp5.addButton("Connect")
     .setPosition(320, 3)
     .setFont(font)
     .setSize(85,19);
     
 cp5.addButton("Disconnect")
     .setPosition(320,23)
     .setFont(font)
     .setSize(85,19);
           
 cp5.addButton("Save")
     .setPosition(415,3)
     .setFont(font)
     .setSize(70,19)
     .setCaptionLabel("Save Data");
 
 cp5.addButton("Open")
     .setPosition(415,23)
     .setFont(font)
     .setSize(70,19)
     .setCaptionLabel("Open File");
      
 cp5.addButton("ScreenShot")
     .setPosition(495,3)
     .setFont(font)
     .setSize(80,19);
 
 cp5.addButton("ClrScrn")
     .setPosition(495,23)
     .setFont(font)
     .setSize(80,19)
     .setCaptionLabel("Clr Screen");
      
 cp5.addButton("Replay")
     .setPosition(585,3)
     .setFont(font)
     .setSize(90,19)
     .setCaptionLabel("Replay Data");
  
 cp5.addButton("RescanPorts")
     .setPosition(585,23)
     .setFont(font)
     .setSize(90,19)
     .setCaptionLabel("Rescan Ports");
        
 cp5.addTextlabel("Label")
     .setText("Display:")
     .setPosition(680,3)
     .setColorValue(255)
     .setFont(font);
                    
 cp5.addRadioButton("Radio")
     .setPosition(685,25)
     .setFont(font)
     .setSize(15,15)
     .setItemsPerRow(3)
     .setSpacingColumn(34)
     .addItem("Graph", 0)
     .addItem("Text", 1)
     .setColorLabel(color(255))
     .activate(0);
     showGraph = true;
   
 valueFld = cp5.addTextarea("Value")
     .setPosition(780,3)
     .setSize(50, menuBarW - 6)
     .setColorBackground(0)
     .setFont(font)
     .setLineHeight(14);
 
 logFld = cp5.addTextlabel("Log")
     .setPosition(320,45)
     .setSize(360, 18)
     .setFont(font)
     .setLineHeight(14);
 
 cp5.addButton("Quit")
     .setPosition(width-60,3)
     .setFont(font)
     .setSize(50,19);
}
     
public void ClrScrn() {
 background(BLUE);
}

public void SerialPorts(int n ) {
 /* request selected item from Map based on index n */
 portName = cp5.get(ScrollableList.class, "SerialPorts").getItem(n).get("name").toString();
 logFld.setText("portSelected: "+portName);
 background(BLUE);
}

public void Baud(int n ) {
  baudRate = baud[n];
  logFld.setText("baudRate: "+baudRate);
  background(BLUE);
}

public void EmptyArray(){
  y = new int[0];
}

public void Connect() {
 // **** Zero out data array **** //
 EmptyArray();
 port = new Serial(this, portName, baudRate);
 connected = true;
 logFld.setText("Connect btn was hit.");
}

public void Disconnect() {
  port.stop();
  yIn = 0;
  count = 0;
  connected = false;
  logFld.setText("Disconnect button was hit.");
}

public void Save() {
  String dateTimeStr = getDateTime();
  String fileToSave = dateTimeStr+".txt";
  String[] data = new String[y.length];
  for (int i = 0; i < y.length; i++) {
    data[i] = y[i]+",";
  }
  saveStrings(fileToSave, data);
  logFld.setText("Data was saved to a file in app folder.");
}

public void ScreenShot() {
 String dateTimeStr = getDateTime();
 String imageOut = dateTimeStr+".png";
 save(imageOut);
 logFld.setText("Screenshot was saved to app folder.");
}

public void RescanPorts() {
 logFld.setText("Rescan ports.");
 cp5.get(ScrollableList.class, "SerialPorts").clear();
 String[] ports = Serial.list();
 List p = Arrays.asList(ports);
 cp5.get(ScrollableList.class, "SerialPorts").addItems(p);
}

public void fileSelected(File selection) {
 if (selection != null) {
  // reset required for multiple selections 
  index = 0;
  counter = 0;
  fileName = selection.getAbsolutePath(); 
  logFld.setText("File selected: " + fileName);
  data = loadStrings(selection.getAbsolutePath());
 }
}

public void Open(){
 selectInput("Select a file to process:", "fileSelected");
}

public void Replay(){
 if (fileName == null) {
   logFld.setText("There is no file selected.");
   // To avoid null pointer exception
   return;
 } else {
  background(BLUE);
  data = loadStrings(fileName);
  index = 0;
  counter = 0; 
 }
}

public void Radio(int radioID) {
  switch(radioID) {
    case(0):logFld.setText("Graph selected as output.");
     showGraph = true;
     showText = false;
     break;
    case(1):logFld.setText("Text selected as output.");
     showGraph = false;
     showText = true;
     break;  
  }
} 

public void Quit() {
  exit();
}

public void draw() {
 // **** Menu Bar **** // 
 fill(128);
 rect(0, 0, width-1, menuBarW);
 // **** Graph of data from serial connection **** //
 if (connected && showGraph){
   if (count > width) {
    count = 0;
    background(BLUE);
    }  
   if (count == 0) {
     x1 = count;
     y1 = yIn;
    }  
   if (count > 0) {
     x2 = count;
     y2 = yIn;
     stroke(255);
     line(x1, y1, x2, y2);
     x1 = x2;
     y1 = y2; 
     } 
    count++;
 }else { 
 // **** Graph of saved file with comma separated values **** // 
 if (data != null){
  if (index < data.length){
    String[] pieces = split(data[index], ",");   
    if (counter > width){
     counter = 0;
     background(BLUE);
    }
    if (counter == 0) {
      x3 = 0;
      y3 = height - PApplet.parseFloat(pieces[0]);
    } 
    if (counter > 0){
    // println("["+index+"] "+pieces[0]);   
     x4 = counter;
     y4 = height - PApplet.parseFloat(pieces[0]);
      stroke(255);
      line(x3, y3, x4, y4);
      x3 = x4;
      y3 = y4;  
    }
    index++;
    counter++;
 }
 }     
}
}

// **** Byte data sent without frame markers **** //
// **** Commas added if data displayed and/or saved to file. **** //
public void serialEvent (Serial port) {
 int inByte = port.read();
 y = append(y,inByte);
 String inStr = str(inByte)+",\n";
 yIn = height - inByte;
 if(showText == true) {valueFld.append(inStr);}
}
  public void settings() {  size(900, 450); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ecg_1" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
