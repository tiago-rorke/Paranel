import processing.core.*; 
import processing.xml.*; 

import java.awt.*; 
import toxi.math.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class paranel_v0_1_2 extends PApplet {



/*   // STRUCTURE OVERVIEW //

- "Sections" are the vertical sections represented in top left "Section View"
    - section positions stored in sectionAngle[] array
- Form is created with walls or "Surfaces", defined by the Surface class.
    - Surface.numCPoints = number of points on each section of a surface (always the same number of points on every section of the same surface)
    - numLayers = number of build layers, as defined by height/carve.
    - Shape of each section is divided into a point per build layer, and stored in Surface.rSharp[numSections][numLayers] 
    - numVertices is the number of points on the tool path for each build layer.
    - The tool path for each layer is calculated using the section points for each layer, Surface.rSharp[][],  the Surface.path[numLayers][numVertices]

- Control Splines?
- Control Points?


*/


//import codeThreadLib.library.*;
 // hatched lines?


// Display vars ======================//

int appWidth = 1024;
int appHeight = 768;

int previewX = appWidth/2;
int previewY = appHeight-appHeight/4 - 10;
int crossSectionX = appWidth/4 + 150;
int crossSectionY = appHeight/4;

int buttonsX = 160;
int buttonsY = appHeight - 350;

int topViewX = 3*appWidth/4;
int topViewY = appHeight/4;

int exportX = 160;
int exportY = appHeight-80;

float rScale = 10;  // Scale to render all (if input mm)
float cScale = 20;  // Scale to render crossSection (if input mm)
float pScale = 20;  // Scale to render preview.

PGraphics preview;
int previewWidth = 800;
int previewHeight = 400;

PImage logo;

// Data vars =========================//

float fingerWidth = 17.0f;
float minFingerWidth = 8.0f;
float ringHeight = 10.0f;

float maxRingDiameter = 30.0f;
float maxRingHeight = 15.0f;
float minRingHeight = 1.0f;

int numSections = 1;
float sectionAngle[] = new float[numSections];

boolean useCSurface = true;
boolean useMSurface = false;
boolean useYSurface = false;

Surface cSurface;
Surface mSurface;
Surface ySurface;

int cNumCpoints = 2;
int mNumCpoints = 2;
int yNumCpoints = 2;

int cNumVertices = 24;
int mNumVertices = 7;
int yNumVertices = 5;

int maxNumCpoints = 10;
int minNumCpoints = 2;

// Print vars ========================//

//float minFacetWidth = 8;

float carve = 0.5f;
int feedRate = 1100;
int extrudeRate = 220;

int pathPrecision = 1000; // set number of decimal places for each gcode coordinate (1000 = 4 decimal places)

/*
float carve = 0.35;
int feedRate = 1500;
int extrudeRate = 255;
*/

int numLayers;
//int numVertices;

//======================================== UI Vars =======================================//

boolean disableControls = false;
boolean editingPoint = false;
boolean addSectionHover = false;
boolean sectionHandleHover = false;
boolean editingSection = false;

float addSectionAngle;
int selectedSection = 0;
int sectionHovered = 0;

boolean overlay = false;
boolean editingDimensions = false;

int controlSelector;
int pMouseY;

String guiPrint = "";

ControlPoint hoveredCPoint;


// Colours and Line Weights =============//

int backgroundColor = 0xff333333;

int guiColor = 0xffcccccc;
int guiColor2 = 0xffffffff;  // section handles etc.
int opacity1 = 70;
int opacity2 = 40;
float line1 = 0.8f;
float line2 = 1;

int cyan = 0xff00aeef;
int magenta = 0xffec008c;
int yellow = 0xfffff200;


// Buttons ========================== //

int fingerWidthInputFill = backgroundColor;
int ringHeightInputFill = backgroundColor;
int exportButtonFill = backgroundColor;

int cSelectOp;
int mSelectOp;
int ySelectOp;
int cAddOp;
int mAddOp;
int yAddOp;
int cSubOp;
int mSubOp;
int ySubOp;

// Text and Lables ===================//

PFont font;

String lablesEN[] = new String[20];

public void setup() {
  size(appWidth,appHeight);
  background(backgroundColor);
  smooth();
  strokeCap(ROUND);
  strokeJoin(ROUND);
  noFill();
  rectMode(CENTER);
  textAlign(CENTER);

  logo = loadImage("paranel_logo.png");

  font = loadFont("PerspectiveSansBold-48.vlw"); 
  //  font = loadFont("HelveticaWorld-Bold-48.vlw");
  textFont(font, 20); 
  setLables_English();

  preview = createGraphics(previewWidth, previewHeight, P3D);

  initialiseSurfaces();
}


public void draw() {
  background(backgroundColor);

  controlsCheck();


  if (useCSurface) {
    cSurface.calcPath();
  }
  if (useMSurface) {  
    mSurface.calcPath();
  }
  if (useYSurface) {
    ySurface.calcPath();
  }



  
  drawPreview();
  drawTopView();
  drawCrossSection();
  drawButtons();

  image(logo, width-190, height-170);

  if(overlay) {
    fill(0, 180);
    noStroke();
    rect(width/2, height/2, width, height);
    noFill();
  }
}


public void setLables_English() {

  lablesEN[1] = "Cross Section";
  lablesEN[2] = "Top View";
  lablesEN[3] = "Preview";

  lablesEN[4] = "Ring Height in mm?";
  lablesEN[5] = "Ring size in mm?";
}




public void initialiseSurfaces() {

  setVars();

  cSurface = new Surface(cyan, 1, cNumCpoints, cNumVertices);
  mSurface = new Surface(magenta, 1.5f, mNumCpoints, mNumVertices);
  ySurface = new Surface(yellow, 2, yNumCpoints, yNumVertices);
}

public void setVars() {

  numLayers = ceil(ringHeight/carve);
  //numVertices = ceil((fingerWidth*PI)/minFacetWidth);
  sectionAngle[0] = -PI/4;
}






class CodeThread {
  String codeString = "";
  String endChar = "\n";
  
  int defaultToolTemp = 230;
  int defaultBuildPlatformTemp = 105;
  

  ArrayList<PathCommand> commands = new ArrayList();


  //set unit to milimeters
  public void setUnitmm() {
    commands.add( new MillimeterCommand());
  }

  //set units to inches 
  public void setUnitInch() {
    commands.add( new InchCommand());
  }

  //move head to absolute position
  public void moveTo(float x, float y, float z, float speed) {
    commands.add( new MoveCommand(x,y,z,speed));
  }

  //turn extruder on fwd
  public void extruderOnFwd() {
    commands.add( new ExtruderOnFwdCommand());
  }

  //turn extruder on rev
  public void extruderOnRev() {
    commands.add( new ExtruderOnRevCommand());
  }

  //turn extruder off
  public void extruderOff() {
    commands.add( new ExtruderOffCommand());
  } 

  //set tool temp
  public void setToolTemp(int toolNum, int temp) {
    commands.add( new ToolTempCommand( toolNum,  temp));
  } 

  //set build platform temp
  public void setBuildPlatformTemp(int toolNum, int temp) {
    commands.add( new BuildPlatformTempCommand( toolNum,  temp));
  } 

  //set tool speed
  public void setToolSpeed(int toolNum, int temp) {
    commands.add( new ToolSpeedCommand( toolNum,  temp));
  } 
  
  public void setAbsolutePositioning() {
    commands.add( new setAbsolutePositioningCommand());
  } 
  
  public void setCurrentHome(int x, int y, int z){
   commands.add( new setCurrentHomeCommand(x,y,z));
  }
  
  public void waitForToolToHeat(int toolNum ){
       commands.add( new waitForToolToHeatCommand(toolNum));
  }
  
  public void pause(int secs ){
       commands.add( new PauseCommand(secs));
  }
  
  public void generateRaft(){
   commands.add( new RaftCommand());
  }


public void setDefault(){
  
  setToolTemp(0,defaultToolTemp);
  setBuildPlatformTemp(0,defaultBuildPlatformTemp);
  setUnitmm();
  setAbsolutePositioning();
  setCurrentHome(0,0,0);
  setToolSpeed(0,extrudeRate);
  waitForToolToHeat(0);
  
}




  public void printToConsole() {

    for(int i = 0; i < commands.size(); i++) {
      PathCommand command = commands.get(i);
      System.out.println(command.print());
    }
  } 






  public void render() {
    beginShape();
    for(int i = 0; i < commands.size(); i++) {
      PathCommand command = commands.get(i);
      if(command instanceof MoveCommand)
        command.render();
    }
    endShape();
  }







  public void writeToFile(String path) {
    BufferedWriter out = null;

    try {
      out = new BufferedWriter( new FileWriter(path));
    } 
    catch (IOException e) {
      e.printStackTrace();
    }


    for(int i = 0; i < commands.size(); i++) {
      PathCommand command = commands.get(i);

      try {
        out.write(command.print()+"\n");
      } 
      catch (IOException e) {
        e.printStackTrace();
      }
    }


    try {
      out.flush();
      out.close();
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}



public void controlsCheck() {

  if (!disableControls) {

    int cPointColor = backgroundColor;    

    checkSectionHover();

    if (useCSurface) {
      hoveredCPoint = checkSplineHandles(cSurface, selectedSection);
      cPointColor = cyan;
    }
    if (hoveredCPoint == null && useMSurface) {
      hoveredCPoint = checkSplineHandles(mSurface, selectedSection);
      cPointColor = magenta;
    }
    if (hoveredCPoint == null && useYSurface) {
      hoveredCPoint = checkSplineHandles(ySurface, selectedSection);
      cPointColor = yellow;
    }

    overlay = false;

    //fingerWidthInput
    if(dist(mouseX,mouseY,topViewX, topViewY) < 40) {
      fingerWidthInputFill = guiColor;
      controlSelector = 1;
    } 
    //ringHeightInput
    else if (dist(mouseX,mouseY,crossSectionX+80, crossSectionY) < 40) {
      ringHeightInputFill = guiColor;
      controlSelector = 2;
    } 
    //controlSpline handles
    else if (hoveredCPoint != null) {
      controlSelector = 3;

      pushMatrix();
      translate(crossSectionX, crossSectionY);

      float x = -hoveredCPoint.r * cScale;
      float y = (ringHeight/2 - hoveredCPoint.h) * cScale;

      stroke(cPointColor);
      strokeWeight(1);
      ellipse(x,y,13,13);

      popMatrix();
    }
    // select a section
    else if (sectionHandleHover) {
      controlSelector = 4;
    }     
    // add a section
    else if (addSectionHover) {
      controlSelector = 5;
    }
    //export button
    else if (dist(mouseX,mouseY,exportX, exportY) < 70) {
      exportButtonFill = guiColor;
      controlSelector = 6;
    }
    //control buttons  ===============================//
    /*
    int cSelectOp;
     int mSelectOp;
     int ySelectOp;
     int cAddOp;
     int mAddOp;
     int yAddOp;
     int cSubOp;
     int mSubOp;
     int ySubOp;
     */

    // cyan toggle
    else if (dist(mouseX,mouseY,buttonsX-45, buttonsY) < 20) {
      cSelectOp = 255;
      controlSelector = 7;
    }
    // magenta toggle
    else if (dist(mouseX,mouseY,buttonsX, buttonsY) < 20) {
      mSelectOp = 255;
      controlSelector = 8;
    }  
    // yellow toggle
    else if (dist(mouseX,mouseY,buttonsX+45, buttonsY) < 20) {
      ySelectOp = 255;
      controlSelector = 9;
    }   

    // cyan add point
    else if (dist(mouseX,mouseY,buttonsX-45, buttonsY-40) < 15) {
      cAddOp = 255;
      controlSelector = 10;
    }
    // magenta add point
    else if (dist(mouseX,mouseY,buttonsX, buttonsY-40) < 15) {
      mAddOp = 255;
      controlSelector = 11;
    }  
    // yellow add point
    else if (dist(mouseX,mouseY,buttonsX+45, buttonsY-40) < 15) {
      yAddOp = 255;
      controlSelector = 12;
    }   
    
     // cyan subtract point
    else if (dist(mouseX,mouseY,buttonsX-45, buttonsY+40) < 15) {
      cSubOp = 255;
      controlSelector = 13;
    }
    // magenta subtract point
    else if (dist(mouseX,mouseY,buttonsX, buttonsY+40) < 15) {
      mSubOp = 255;
      controlSelector = 14;
    }  
    // yellow subtract point
    else if (dist(mouseX,mouseY,buttonsX+45, buttonsY+40) < 15) {
      ySubOp = 255;
      controlSelector = 15;
    }   


    else {
      cSelectOp = 30;
      mSelectOp = 50;
      ySelectOp = 30;

      if(useCSurface) {
        cSelectOp = 200;
      }
      if(useMSurface) {
        mSelectOp = 200;
      }    
      if(useYSurface) {
        ySelectOp = 200;
      }    

      cAddOp = 30;
      mAddOp = 50;
      yAddOp = 30;
      cSubOp = 30;
      mSubOp = 50;
      ySubOp = 30;


      fingerWidthInputFill = backgroundColor;
      ringHeightInputFill = backgroundColor;
      exportButtonFill = backgroundColor;
      controlSelector = 0;
    }
  } 

  else if (editingPoint) {    
    float newR = -(mouseX - crossSectionX)/cScale;
    if (newR > fingerWidth/2 && newR < maxRingDiameter/2) {
      hoveredCPoint.r = newR;
    }

    if (hoveredCPoint.h > 0 && hoveredCPoint.h < ringHeight) {
      float newH =  ringHeight/2 - (mouseY - crossSectionY)/cScale;
      if (newH > 0 && newH < ringHeight) {
        hoveredCPoint.h = newH;
      }
    }
  }

  else if (editingDimensions) {
    if(controlSelector == 1) {
      scrollFingerWidth();
    }
    if(controlSelector == 2) {
      scrollRingHeight();
    }
  }

  else if (editingSection) {
    moveSection(cSurface);
    moveSection(mSurface);
    moveSection(ySurface);
  }

  else if (false) {
    overlay = true;
  }
}



public void mousePressed() { 

  switch(controlSelector) {
  case 0:
    disableControls = false; // select nothing
    break;
  case 1: 
    disableControls = true;  // select fingerWidth input
    editingDimensions = true;
    pMouseY = mouseY;
    break;
  case 2:
    disableControls = true;  // select ringHeight input
    editingDimensions = true;
    pMouseY = mouseY;
    break;
  case 3:
    disableControls = true;  // select controlSpline point
    editingPoint = true;
    break;
  case 4:
    disableControls = true;  // select section
    selectedSection = sectionHovered;
    editingSection = true;
    break;
  case 5:
    disableControls = true;  // add section
    numSections++;
    sectionAngle = append(sectionAngle, addSectionAngle);
    addSection(cSurface, addSectionAngle);
    addSection(mSurface, addSectionAngle);
    addSection(ySurface, addSectionAngle);
    selectedSection = numSections-1;
    break;
  case 6:
    disableControls = true;  // export Gcode
    exportGcode();
    disableControls = false;
    controlSelector = 0;
    break;
  case 7:
    useCSurface = !useCSurface;  // toggle cyan surface
    break;
  case 8:
    useMSurface = !useMSurface;  // toggle cyan surface
    break;
  case 9:
    useYSurface = !useYSurface;  // toggle cyan surface
    break;
  case 10:
    cNumCpoints++;  // add cyan point
    if (cNumCpoints > maxNumCpoints) {
      cNumCpoints = maxNumCpoints;
    }
    cSurface = new Surface(cyan, 1, cNumCpoints, cNumVertices);
    break;
  case 11:
    mNumCpoints++;  // add magenta point
    if (mNumCpoints > maxNumCpoints) {
      mNumCpoints = maxNumCpoints;
    }
    mSurface = new Surface(magenta, 1.5f, mNumCpoints, mNumVertices);
    break;
  case 12:
    yNumCpoints++;  // add yellow point
    if (yNumCpoints > maxNumCpoints) {
      yNumCpoints = maxNumCpoints;
    }
    ySurface = new Surface(yellow, 2, yNumCpoints, yNumVertices);
    break; 
  case 13:
    cNumCpoints--;  // sub cyan point
    if (cNumCpoints < minNumCpoints) {
      cNumCpoints = minNumCpoints;
    }
    cSurface = new Surface(cyan, 1, cNumCpoints, cNumVertices);
    break;
  case 14:
    mNumCpoints--;  // sub magenta point
    if (mNumCpoints < minNumCpoints) {
      mNumCpoints = minNumCpoints;
    }
    mSurface = new Surface(magenta, 1.5f, mNumCpoints, mNumVertices);
    break;
  case 15:
    yNumCpoints--;  // sub yellow point
    if (yNumCpoints < minNumCpoints) {
      yNumCpoints = minNumCpoints;
    }
    ySurface = new Surface(yellow, 2, yNumCpoints, yNumVertices);
    break;     
  } 
}

public void mouseReleased() {
  editingPoint = false;
  editingSection = false;
  editingDimensions = false;
  addSectionHover = false;
  sectionHandleHover = false;
  disableControls = false;
  overlay = false;
  controlSelector = 0;

  guiPrint = "";
}


public ControlPoint checkSplineHandles(Surface surface, int sectionNum) {

  ControlSpline cSpline = (ControlSpline) surface.cs.get(sectionNum);
  ControlPoint cPoints[] = new ControlPoint[cSpline.cp.size()];

  ControlPoint thePoint = cPoints[0];

  if (surface.sectionSharp) {

    for (int i=0; i<cSpline.cp.size(); i++) {
      cPoints[i] = (ControlPoint) cSpline.cp.get(i);

      float x = -cPoints[i].r * cScale + crossSectionX;                      // cannot use matrix translation together with mouse coords.
      float y = (ringHeight/2 - cPoints[i].h) * cScale + crossSectionY;

      if (dist(mouseX,mouseY,x,y) < 10) {
        thePoint = cPoints[i];
      }
    }
  }

  return(thePoint);
}


public void scrollFingerWidth() {
  if (fingerWidth<=maxRingDiameter-3 && fingerWidth>=minFingerWidth) {
    fingerWidth += (mouseY - pMouseY) * 0.1f;
  }
  if (fingerWidth>maxRingDiameter-3) {
    fingerWidth = maxRingDiameter-3;
  }
  if (fingerWidth<minFingerWidth) {
    fingerWidth = minFingerWidth;
  }
  pMouseY = mouseY;

  initialiseSurfaces();
}

public void scrollRingHeight() {
  if (ringHeight<=maxRingHeight && ringHeight>=minRingHeight) {
    ringHeight += (mouseY - pMouseY) * 0.05f;
  }
  if (ringHeight>maxRingHeight) {
    ringHeight = maxRingHeight;
  }
  if (ringHeight<minRingHeight) {
    ringHeight = minRingHeight;
  }
  pMouseY = mouseY;

  initialiseSurfaces();
}



// =================================================================================== //
//                                    MISC LABLES/BUTTONS
// =================================================================================== //

public void drawButtons() {
  
  
   // Export gCode Button
  pushMatrix();
  translate(exportX, exportY);

  strokeWeight(1);
  stroke(guiColor);  
  fill(backgroundColor);
  roundedRect(0,0,150,30,10);
  fill(exportButtonFill, 100);
  roundedRect(0,0,150,30,10);
  fill(guiColor);
  textFont(font, 15); 
  text("Export G-Code", 0, 5);
  noFill();

  popMatrix();
 
 
   // Console line
  fill(guiColor);
  textFont(font, 12); 
  text(guiPrint, exportX, exportY + 40);
  noFill();
  
   // SplinePoint/Surface buttons;
    
  pushMatrix();
  translate(buttonsX, buttonsY);

  strokeWeight(2);
  stroke(cyan,cSelectOp);  
  fill(cyan,cSelectOp/2);
  roundedRect(-45,0,30,30,10);
  noFill();
  strokeWeight(2);
  stroke(magenta,mSelectOp);  
  fill(magenta,mSelectOp/2);
  roundedRect(0,0,30,30,10);
  noFill();
  strokeWeight(2);
  stroke(yellow,ySelectOp);  
  fill(yellow,ySelectOp/2);
  roundedRect(45,0,30,30,10);
  noFill();
  
  strokeWeight(2);
  stroke(cyan,cAddOp);
  ellipse(-45,-40,20,20);
  noFill();
  strokeWeight(2);
  stroke(magenta,mAddOp);
  ellipse(0,-40,20,20);
  noFill();
  strokeWeight(2);
  stroke(yellow,yAddOp);
  ellipse(45,-40,20,20);
  noFill();
  
  strokeWeight(2);
  stroke(cyan,cSubOp);
  ellipse(-45,40,20,20);
  noFill();
  strokeWeight(2);
  stroke(magenta,mSubOp);
  ellipse(0,40,20,20);
  noFill();
  strokeWeight(2);
  stroke(yellow,ySubOp);
  ellipse(45,40,20,20);
  noFill();

  popMatrix();   
  
  
  
}


// =================================================================================== //
//                                    CROSS SECTION
// =================================================================================== //

public void drawCrossSection() {

  pushMatrix();
  translate(crossSectionX, crossSectionY);

  // Draw Diagramming ===================//

  //hatch to represent finger
  stroke(guiColor, opacity1);
  strokeWeight(2);
  line(-fingerWidth/2*cScale, -maxRingHeight/2*cScale-2, -fingerWidth/2*cScale, maxRingHeight/2*cScale+2);
  stroke(guiColor, opacity1);
  strokeWeight(line1);
  hatch45Fill(-fingerWidth/2*cScale, -maxRingHeight/2*cScale, fingerWidth/2*cScale+30, maxRingHeight*cScale, 10);
  noStroke();
  for (int i=0; i<= (fingerWidth/2*cScale+30)/4; i++) {
    fill(backgroundColor, i*(255/((fingerWidth/2*cScale+30)/4)));
    rect(-fingerWidth/2*cScale + 2 + i*4, 0, 4, maxRingHeight*cScale+10);
  }

  //centreLine
  stroke(guiColor, opacity1);
  strokeWeight(line2);
  line(0,-maxRingHeight/2*cScale-5, 0, -80);
  line(0,-70,0,-60);
  line(0,-50,0,-40);
  line(0,-30, 0, 30);
  line(0, 50,0, 40);
  line(0, 70,0, 60);
  line(0, 80, 0, maxRingHeight/2*cScale+5);

  //top/bottom guidelines
  stroke(guiColor, opacity1);
  strokeWeight(line1);
  line(-maxRingDiameter/2*cScale, -ringHeight/2*cScale, 100, -ringHeight/2*cScale);
  line(-maxRingDiameter/2*cScale, ringHeight/2*cScale, 100, ringHeight/2*cScale);

  //arrow and inputbox
  pushMatrix();
  translate(80,0);

  strokeWeight(2);
  stroke(guiColor);  
  arrow(0,0,ringHeight*cScale-4,0);

  strokeWeight(1);
  stroke(guiColor);  
  fill(backgroundColor);
  roundedRect(0,0,60,30,10);
  fill(ringHeightInputFill, 100);
  roundedRect(0,0,60,30,10);
  fill(guiColor);
  textFont(font, 20); 
  text(""+PApplet.parseFloat(PApplet.parseInt(ringHeight*10))/10,0,6);
  noFill();

  popMatrix();


  // Draw Section ================================== //

  if (useCSurface) {
    cSurface.renderSection(selectedSection);
  }
  if (useMSurface) {  
    mSurface.renderSection(selectedSection);
  }
  if (useYSurface) {
    ySurface.renderSection(selectedSection);
  }

  popMatrix();
}




// =================================================================================== //
//                                    TOP VIEW
// =================================================================================== //

public void drawTopView() {

  pushMatrix();
  translate(topViewX, topViewY);



  // Draw Diagramming ===================//

  stroke(guiColor, opacity1);
  strokeWeight(line1);
  hatch45Fill(-fingerWidth/2*rScale-10, -fingerWidth/2*rScale-10, fingerWidth*rScale+20, fingerWidth*rScale+20, 10);

  noStroke();
  fill(backgroundColor);
  beginShape();
  curveVertex(0,fingerWidth/2*rScale);
  for (int i=0; i<22; i++) {
    curveVertex(fingerWidth/2*rScale*sin(i*(TWO_PI/20)),  fingerWidth/2*rScale*cos(i*(TWO_PI/20)));
  }
  vertex(0,fingerWidth/2*rScale+20);
  vertex(-fingerWidth/2*rScale-20,fingerWidth/2*rScale+20);
  vertex(-fingerWidth/2*rScale-20,-fingerWidth/2*rScale-20); 
  vertex(fingerWidth/2*rScale+20,-fingerWidth/2*rScale-20);
  vertex(fingerWidth/2*rScale+20,fingerWidth/2*rScale+20);
  vertex(0,fingerWidth/2*rScale+20);
  endShape();

  stroke(guiColor, opacity1);
  strokeWeight(2);
  noFill();
  ellipse(0,0,fingerWidth*rScale,fingerWidth*rScale);
  strokeWeight(2);
  stroke(guiColor);
  arrow(0,0,fingerWidth*rScale-4,-PI/6);


  //input box
  strokeWeight(1);
  stroke(guiColor);
  fill(backgroundColor);
  roundedRect(0,0,60,30,10);
  fill(fingerWidthInputFill, 100);
  roundedRect(0,0,60,30,10);
  fill(guiColor);
  textFont(font, 20); 
  text(""+PApplet.parseFloat(PApplet.parseInt(fingerWidth*10))/10,0,6);
  noFill();

  strokeWeight(line1);
  stroke(guiColor, opacity1);
  dashedCircle(0,0,maxRingDiameter*rScale,8,13);

  // Draw Section Handles ===============//  


  for (int i=0; i<numSections; i++) {
    float sPx = (maxRingDiameter/2*rScale+20)*sin(sectionAngle[i]);
    float sPy = (maxRingDiameter/2*rScale+20)*cos(sectionAngle[i]);
    float sCx = (fingerWidth/2*rScale-10)*sin(sectionAngle[i]);
    float sCy = (fingerWidth/2*rScale-10)*cos(sectionAngle[i]);

    stroke(guiColor2);
    if (i == selectedSection) {
      strokeWeight(3);
      line(sCx, sCy, sPx, sPy);
      strokeWeight(2.5f);
      fill(backgroundColor);
      ellipse(sPx,sPy,15,15);
      fill(guiColor2,100);
      ellipse(sPx,sPy,15,15);
      noFill();
    }
    else {
      strokeWeight(1.5f);
      line(sCx, sCy, sPx, sPy);
      fill(backgroundColor);
      strokeWeight(2);
      ellipse(sPx,sPy,10,10);
      noFill();
    }

    if (sectionHandleHover && i == sectionHovered && selectedSection != sectionHovered) {
      strokeWeight(1);
      ellipse(sPx,sPy,20,20);
    }
  }



  if(addSectionHover) {
    float sPx = (maxRingDiameter/2*rScale+20)*sin(addSectionAngle);
    float sPy = (maxRingDiameter/2*rScale+20)*cos(addSectionAngle);
    float sCx = (fingerWidth/2*rScale-10)*sin(addSectionAngle);
    float sCy = (fingerWidth/2*rScale-10)*cos(addSectionAngle);
    stroke(guiColor2, 100);
    strokeWeight(0.5f);
    line(sCx, sCy, sPx, sPy);
    fill(backgroundColor);
    strokeWeight(1);
    ellipse(sPx,sPy,10,10);
    noFill();
  }

  // Draw Control Point Sweeps ===============//  

  for (int i=0; i<numLayers; i++) {
    if (useCSurface) {
      cSurface.renderSweeps(i, false);
    }
    if (useMSurface) {  
      mSurface.renderSweeps(i, false);
    }
    if (useYSurface) {
      ySurface.renderSweeps(i, false);
    }
  }




  popMatrix();
}



// =================================================================================== //
//                               DRAWING FUNCTIONS
// =================================================================================== //


public void arrow(int x, int y, float arrowLength, float rotation) {
  pushMatrix();
  translate(x,y);
  rotate(rotation);
  line(0, -arrowLength/2, 0, arrowLength/2);
  line(0, -arrowLength/2, -7, -arrowLength/2+10);
  line(0, -arrowLength/2, 7, -arrowLength/2+10);
  line(0, arrowLength/2, -7, arrowLength/2-10);
  line(0, arrowLength/2, 7, arrowLength/2-10);
  popMatrix();
}


public void roundedRect(float x, float y, float w, float h, float r) {

  x = x-w/2;
  y = y-h/2;
  float rx = r;
  float ry = r;

  //  Thank you, 'cefnhoile' for this function.
  //  http://forum.processing.org/topic/rounded-rectangle

  beginShape();
  vertex(x,y+ry); //top of left side 
  bezierVertex(x,y,x,y,x+rx,y); //top left corner
  vertex(x+w-rx,y); //right of top side 
  bezierVertex(x+w,y,x+w,y,x+w,y+ry); //top right corner
  vertex(x+w,y+h-ry); //bottom of right side
  bezierVertex(x+w,y+h,x+w,y+h,x+w-rx,y+h); //bottom right corner
  vertex(x+rx,y+h); //left of bottom side
  bezierVertex(x,y+h,x,y+h,x,y+h-ry); //bottom left corner
  endShape(CLOSE);
} 

public void dashedCircle(float x, float y, float diameter, float dash, float gap) {

  float perimeter = PI*diameter;

  int numDashes = floor(perimeter/(dash+gap));

  float theta = TWO_PI/numDashes;
  float thetaDash = theta * dash/(dash+gap);

  for (int i=0; i<numDashes; i++) {
    arc(x, y, diameter, diameter, i*theta, i*theta + thetaDash);
  }
}


//Note: works as if rectMode(CORNER)
public void hatch45Fill (float x, float y, float rectWidth, float rectHeight, float gap) {

  for (int i=0; i < ceil((rectWidth+rectHeight)/gap); i++) {
    if(i*gap < rectWidth) {
      line(x,y+i*gap, x+i*gap, y);
    } 
    else if(i*gap < rectHeight) {
      line(x,y+i*gap, x+rectWidth, y+i*gap-rectWidth);
    } 
    else {
      line(x+i*gap-rectHeight, y+rectHeight, x+rectWidth, y+i*gap-rectWidth);
    }
  }
}



// =================================================================================== //
//                               3D VISUALISATION
// =================================================================================== //

public void drawPreview() {

  preview.beginDraw();
  preview.background(backgroundColor);
  preview.perspective(PI/10, previewWidth/previewHeight, 1, 1000);
  preview.pushMatrix();
  preview.translate(previewWidth/2,previewHeight/2, -1300);
  preview.rotateX(PI/2 - 0.5f);
  preview.translate(0,0, -80);

  //preview.smooth();
  preview.stroke(0xff000000);
  preview.strokeWeight(1);
  preview.fill(0xffFF0000,100);
  for (int i=0; i<numLayers; i++) {
    preview.translate(0,0,carve*pScale);
    //preview.ellipse(0,0,30*pScale,30*pScale);
    if (useCSurface) {
      cSurface.renderSweeps(i, true);
    }
    if (useMSurface) {  
      mSurface.renderSweeps(i, true);
    }
    if (useYSurface) {
      ySurface.renderSweeps(i, true);
    }
  }
  preview.noFill();

  preview.popMatrix();
  preview.endDraw();

  image(preview, previewX-previewWidth/2, previewY-previewHeight/2);

  stroke(guiColor);
  //rect(previewX, previewY, previewWidth, previewHeight);
}





class PathCommand{
  
  
  
  
  public String print(){
   return ""; 
  }
  
  
  public void render(){
  }
  
} 


class MoveCommand extends PathCommand{
  float x, y, z;
  float speed;
  
 MoveCommand(float x, float y, float z, float speed){
  this.x = x;
  this.y = y;
  this.z = z;
  this.speed = speed;
 }
 
 public String print(){
  return "G1 X"+x+" Y"+y+" Z"+z+" F"+speed; 
 }
 
 public void render(){
  vertex(x,y,z); 
 }
}


class MillimeterCommand extends PathCommand{ 
 public String print(){
  return "G21"; 
 }
}


class InchCommand extends PathCommand{ 
 public String print(){
  return "G20"; 
 }
}

class PauseCommand extends PathCommand{ 
  int pauseSecs ;
  PauseCommand(int sec){
    this.pauseSecs = sec;
    
  }
 public String print(){
  return "G4 P"+pauseSecs; 
 }
}




class ExtruderOnFwdCommand extends PathCommand{ 
 public String print(){
  return "M101"; 
 }
}

class ExtruderOnRevCommand extends PathCommand{ 
 public String print(){
  return "M102"; 
 }
}

class ExtruderOffCommand extends PathCommand{ 
 public String print(){
  return "M103"; 
 }
}


class RaftCommand extends PathCommand{ 
 public String print(){
  return ""; 
 }
}


class setAbsolutePositioningCommand extends PathCommand{ 
 public String print(){
  return "G90"; 
 }
}

class waitForToolToHeatCommand extends PathCommand{ 
  int toolNum;
  waitForToolToHeatCommand(int toolNum){
    this.toolNum = toolNum;
  }
 public String print(){
  return "M6 T"+toolNum; 
 }
}


class setCurrentHomeCommand extends PathCommand{ 
  int x,y,z;
  setCurrentHomeCommand(int x, int y, int z){
   this.x = x;
  this.y = y;
 this.z = z; 
  }
 public String print(){
  return "G92 X"+x+" Y"+y+" Z"+z+" "; 
 }
}




class ToolTempCommand extends PathCommand{ 
  int temp;
  int toolNum;
  ToolTempCommand(int toolNum, int temp){
   this.temp = temp; 
   this.toolNum = toolNum;
  }
 public String print(){
  return "M104 S"+temp+" T"+toolNum; 
 }
}

class ToolSpeedCommand extends PathCommand{ 
  int speed;
  int toolNum;
  ToolSpeedCommand(int toolNum, int speed){
   this.speed = speed; 
   this.toolNum = toolNum;
  }
 public String print(){
  return "M"+(1+toolNum)+"08 S"+speed; 
 }
 
}

class BuildPlatformTempCommand extends PathCommand{ 
  int temp;
  int toolNum;
  BuildPlatformTempCommand(int toolNum, int temp){
   this.temp = temp; 
   this.toolNum = toolNum;
  }
 public String print(){
  return "M109 S"+temp+" T"+toolNum; 
 }
 
}









public void checkSectionHover() {

  if(dist(mouseX, mouseY, topViewX, topViewY) < (maxRingDiameter/2*rScale+50) && dist(mouseX, mouseY, topViewX, topViewY) > (fingerWidth/2*rScale)) {
    addSectionAngle = atan2(mouseX - topViewX, mouseY - topViewY);

    sectionHandleHover = false;
    addSectionHover = false;

    for (int i=0; i<numSections; i++) {
      if (abs(addSectionAngle - sectionAngle[i]) < 0.1f) {
        sectionHandleHover = true;
        sectionHovered = i;
      }
    }

    if (!sectionHandleHover) {
      addSectionHover = true;
    }
  } 
  else {
    addSectionHover = false;
    sectionHandleHover = false;
  }
  
}



public void addSection(Surface surface, float angle) {

  surface.cs.add(new ControlSpline(angle, surface.numCPoints, surface.initR));
  surface.rSharp = new float[numSections][numLayers];
}



public void moveSection(Surface surface) {
   sectionAngle[selectedSection] = atan2(mouseX - topViewX, mouseY - topViewY);
   ControlSpline cSpline = (ControlSpline) surface.cs.get(selectedSection);
   cSpline.sectionAngle = sectionAngle[selectedSection];
   
}


class ControlSpline {

  ArrayList cp = new ArrayList();

  float sectionAngle;

  ControlSpline(float angle, int numPoints, float radius) {
    
    sectionAngle = angle;
    
    for (int i=0; i<numPoints; i++) {
      cp.add(new ControlPoint());
    }
    
    ControlPoint cPoints[] = new ControlPoint[cp.size()];
    
    for (int i=0; i<cPoints.length; i++) {
      cPoints[i] = (ControlPoint) cp.get(i);
    }
    
    // Set point heights, and radii    
      
    cPoints[0].h = 0;                        // bottom point
    cPoints[0].r = radius;
    cPoints[cPoints.length-1].h = ringHeight;  // top point
    cPoints[cPoints.length-1].r = radius;
    
    for (int i=1; i<cPoints.length-1; i++) {
      cPoints[i].h = i*(ringHeight/(cPoints.length-1));
      cPoints[i].r = radius;
    }
    
  }
  
  
} //END SPLINE CLASS




class ControlPoint {

  float h; // y pos.
  float r; // x pos (radius/distance from centerline)
  
  
  ControlPoint() {
  }
  
  
} //END POINT CLASS

class Surface {

  int surfaceColor;

  boolean sectionSharp;  // currently always true, smooth sections (bezier curves in sections) not yet implemented

  float rSharp[][];   // points along section for build layers.
  //float rSmooth[][];
  float path[][];            // faceted toolpath
  float bezierPath[][][];    // smooth bezier Path

  float sortedAngles[];  // array for sorting section order for creating paths

  float initR;
  int numCPoints;
  int numVertices;

  ArrayList cs = new ArrayList();

  Surface(int inColor, float offset, int numPoints, int inNumVertices) {

    numVertices = inNumVertices;
    surfaceColor = inColor;
    numCPoints = numPoints;   // number of initial control points on a section
    initR = fingerWidth/2 + offset;  // initial radii of all points (fingerRadius + offset)

    sectionSharp = true;

    rSharp = new float[numSections][numLayers];
    //rSmooth = new float[numSections][numLayers];

    for (int i=0; i<numSections; i++) {                             //initialise with one section;
      cs.add(new ControlSpline(sectionAngle[0], numCPoints, initR));
    }

    this.calcPath();
  }



  // Calculate points for tool path ===================================== //

  public void calcPath() {

    // divide section lines into points //
    
    //ScaleMap logMap;
    //logMap=new ScaleMap(log(expectedMinValue),log(expectedMaxValue),0,width);

    if (sectionSharp) {

      for (int i=0; i<numSections; i++) {

        int layer = 0;

        ControlSpline cSpline = (ControlSpline) cs.get(i);
        ControlPoint pcPoint = (ControlPoint) cSpline.cp.get(0);

        for (int j=1; j<cSpline.cp.size(); j++) {
          ControlPoint cPoint = (ControlPoint) cSpline.cp.get(j);
          float m = (cPoint.h - pcPoint.h)/(cPoint.r - pcPoint.r); // gradient on line segment | m=dy/dx
          while(layer*carve < cPoint.h && layer < numLayers) {
            rSharp[i][layer] = -(layer*carve - pcPoint.h)/m - pcPoint.r;   // y = mx+c  ==>  x = (y-c)/m
            layer++;
          }
          pcPoint = cPoint;
        }
      }
    }


    // order and sweep between control splines // 

    if(numSections > 1) {

      sortedAngles = new float[numSections];

      for (int i=0; i<numSections; i++) {
        sortedAngles[i] = sectionAngle[i];
      }

      sortedAngles = sort(sortedAngles);

      path = new float[numLayers][numVertices];
      bezierPath = new float[numLayers][numSections][4];
      //..[0] = angle, [1] = radius, [2] = counter-clockwise handle length, [3] = clockwise handle length

      for (int layer=0; layer<numLayers; layer++) {

        int index = 0;
        float dAngleRev;
        float dAngleFwd;
        
        for (int j=0; j<numSections; j++) {

          for (int k=0; k<numSections; k++) {
            if (sortedAngles[j] == sectionAngle[k]) {
              index = k;
            }
          }

          bezierPath[layer][j][0] = sortedAngles[j];//sectionAngle[index];
          bezierPath[layer][j][1] = -rSharp[index][layer];
          
          dAngleFwd = abs(sortedAngles[(j+1)%(numSections)]-sortedAngles[j]);
          dAngleRev = abs(sortedAngles[(j+numSections-1)%(numSections)]-sortedAngles[j]);
          
          if(j==0)
          dAngleRev = TWO_PI-dAngleRev;
          
          if(j==numSections-1)
          dAngleFwd = TWO_PI-dAngleFwd;
          
          bezierPath[layer][j][3] = map(dAngleRev,0,PI,0,pow(dAngleRev,0.22f)*fingerWidth*0.6f);//0.5522);
          bezierPath[layer][j][2] = map(dAngleFwd,0,PI,0,pow(dAngleFwd,0.22f)*fingerWidth*0.6f);//0.5522);
          
        }
      }
    }//endif > 1 section.
    
    else {
      path = new float[numLayers][numVertices];

      for (int i=0; i<numLayers; i++) {
        for (int j=0; j<numVertices; j++) {
          path[i][j] = rSharp[0][i];
        }
      }
    }
    
  } // end calcPath



  // Function for Rendering Cross Section ================================ //

  public void renderSection(int sectionNum) {

    ControlSpline cSpline = (ControlSpline) cs.get(sectionNum);
    ControlPoint cPoints[] = new ControlPoint[cSpline.cp.size()];

    if (sectionSharp) {

      for (int i=0; i<cSpline.cp.size(); i++) {
        cPoints[i] = (ControlPoint) cSpline.cp.get(i);

        float x = -cPoints[i].r * cScale;
        float y = (ringHeight/2 - cPoints[i].h ) * cScale;

        stroke(surfaceColor);
        strokeWeight(2);
        ellipse(x,y,5,5);

        if(i>0) {
          float px = -cPoints[i-1].r * cScale;
          float py = (ringHeight/2 - cPoints[i-1].h) * cScale;
          stroke(surfaceColor);
          strokeWeight(1.5f);
          line(px,py,x,y);
        }
      }

      // render layers in section
      /*
      for (int i=0; i<numLayers; i++) {
       float x = rSharp[sectionNum][i] * cScale;
       float y = (ringHeight/2 - i*carve) * cScale;
       stroke(surfaceColor);
       strokeWeight(1);
       line(x,y,0,y);
       }*/
    }
  }

  // Function for Rendering Cross Section ================================ //
  public void renderSweeps(int layer, boolean inPreview) {

    /*  ControlSpline cSplines[] = new ControlSpline[cs.size()];              // this might not be neccessary...
     for(int i=0; i<cs.size(); i++) {
     ControlPoint cPoints[] = new ControlPoint[cSplines[i].cp.size()];
     }*/

    if(numSections > 1) {

      strokeWeight(1);
      for (int j=0; j<numSections; j++) {

        //..[0] = angle, [1] = radius, [2] = counter-clockwise handle length, [3] = clockwise handle length

        int j2 = (j+1)%(numSections);

        if(inPreview) {
          
        float x1 = bezierPath[layer][j][1]*pScale*sin(bezierPath[layer][j][0]);
        float y1 = bezierPath[layer][j][1]*pScale*cos(bezierPath[layer][j][0]);

        // handles are kept tangent > pythagoras to get radius and sin-1 to get angle

        float c1r = sqrt(sq(bezierPath[layer][j][2]) + sq(bezierPath[layer][j][1]));
        float c1a = bezierPath[layer][j][0] + asin(bezierPath[layer][j][2]/c1r);

        float c1x = c1r*pScale*sin(c1a);
        float c1y = c1r*pScale*cos(c1a);

        float c2r = sqrt(sq(bezierPath[layer][j2][3]) + sq(bezierPath[layer][j2][1])); 
        float c2a = bezierPath[layer][j2][0] - asin(bezierPath[layer][j2][3]/c2r);

        float c2x = c2r*pScale*sin(c2a);
        float c2y = c2r*pScale*cos(c2a);


        float x2 = bezierPath[layer][j2][1]*pScale*sin(bezierPath[layer][j2][0]);
        float y2 = bezierPath[layer][j2][1]*pScale*cos(bezierPath[layer][j2][0]);
          
          preview.noFill();
          preview.stroke(surfaceColor);
          preview.strokeWeight(4);
          preview.bezier(x1,y1,c1x,c1y,c2x,c2y,x2,y2);
          
        
        }
        else {
          
        float x1 = bezierPath[layer][j][1]*rScale*sin(bezierPath[layer][j][0]);
        float y1 = bezierPath[layer][j][1]*rScale*cos(bezierPath[layer][j][0]);

        // handles are kept tangent > pythagoras to get radius and sin-1 to get angle

        float c1r = sqrt(sq(bezierPath[layer][j][2]) + sq(bezierPath[layer][j][1]));
        float c1a = bezierPath[layer][j][0] + asin(bezierPath[layer][j][2]/c1r);

        float c1x = c1r*rScale*sin(c1a);
        float c1y = c1r*rScale*cos(c1a);

        float c2r = sqrt(sq(bezierPath[layer][j2][3]) + sq(bezierPath[layer][j2][1])); 
        float c2a = bezierPath[layer][j2][0] - asin(bezierPath[layer][j2][3]/c2r);

        float c2x = c2r*rScale*sin(c2a);
        float c2y = c2r*rScale*cos(c2a);


        float x2 = bezierPath[layer][j2][1]*rScale*sin(bezierPath[layer][j2][0]);
        float y2 = bezierPath[layer][j2][1]*rScale*cos(bezierPath[layer][j2][0]);
          
          
          stroke(surfaceColor);
          strokeWeight(1);     
          bezier(x1,y1,c1x,c1y,c2x,c2y,x2,y2);


          // Bezier Debugging //
          /*
          strokeWeight(0.01);
          line(x1,y1,x2,y2);
          ellipse(x2,y2,10,10);
          line(x1,y1,c1x,c1y);
          line(c2x,c2y,x2,y2);
          //  ellipse(x1,y1,5,5);
          //  ellipse(x2,y2,5,5);
          //  ellipse(c1x,c1y,3,3);
          ellipse(c2x,c2y,3,3); 
          */
        }
      }
    }//end if sections < 1
    else {




      float r = path[layer][0];

      for (int i=0; i<numVertices; i++) {

        float a1 = i*(TWO_PI/numVertices) + sectionAngle[0];
        float a2 = (i+1)*(TWO_PI/numVertices) + sectionAngle[0];

        if(inPreview) {
          preview.stroke(surfaceColor);
          preview.strokeWeight(4);
          float x1 = r*pScale*sin(a1);
          float y1 = r*pScale*cos(a1);
          float x2 = r*pScale*sin(a2);
          float y2 = r*pScale*cos(a2);
          preview.line(x1,y1,x2,y2);
        } 
        else {
          stroke(surfaceColor);
          strokeWeight(1);
          float x1 = r*rScale*sin(a1);
          float y1 = r*rScale*cos(a1);
          float x2 = r*rScale*sin(a2);
          float y2 = r*rScale*cos(a2);
          line(x1,y1,x2,y2);
        }
      }
      
    }
  }
  
} // CLASS END


CodeThread gcode = new CodeThread();

public float truncate(float a){
 if ( a > 0 )
   return PApplet.parseFloat(floor(a * pathPrecision))/pathPrecision;
 else
   return PApplet.parseFloat(ceil(a * pathPrecision))/pathPrecision;
}


public void makeRaft(int raftWidth, int raftHeight, int baseFeed, int interfaceFeed, float baseStep, float interfaceStep) {

  int numBaseLines = floor(raftWidth/baseStep);
  int numInterfaceLines = floor(raftHeight/interfaceStep);

  gcode.moveTo(-raftWidth/2, -40, 0, interfaceFeed);
  gcode.extruderOnFwd();

  for (int i=0; i<numBaseLines; i+=2) {
    gcode.moveTo(-raftWidth/2+i*baseStep, -raftHeight/2, 0.35f, baseFeed);    
    gcode.moveTo(-raftWidth/2+i*baseStep, raftHeight/2, 0.35f, baseFeed);
    gcode.moveTo(-raftWidth/2+(i+1)*baseStep, raftHeight/2, 0.35f, baseFeed);
    gcode.moveTo(-raftWidth/2+(i+1)*baseStep, -raftHeight/2, 0.35f, baseFeed);
  }
  gcode.extruderOff();
  gcode.moveTo(raftWidth/2, raftHeight/2+1, 0.35f, interfaceFeed);
  gcode.extruderOnFwd();
  for (int i=0; i<numInterfaceLines; i+=2) {
    gcode.moveTo(raftWidth/2, raftHeight/2-i*interfaceStep, 0.95f, interfaceFeed);    
    gcode.moveTo(-raftWidth/2, raftHeight/2-i*interfaceStep, 0.95f, interfaceFeed);
    gcode.moveTo(-raftWidth/2, raftHeight/2-(i+1)*interfaceStep, 0.95f, interfaceFeed);
    gcode.moveTo(raftWidth/2, raftHeight/2-(i+1)*interfaceStep, 0.95f, interfaceFeed);
  }
  gcode.extruderOff();
}


public void exportGcode() {

  guiPrint = (String)("Generating G-code...");

  int feed = feedRate;
  float firstLayerHeight = 1.5f;

  gcode.setDefault();

  makeRaft(25, 25, 560, 2000, 2.5f, 0.7f);

  float r;
  gcode.moveTo(0, 0, firstLayerHeight, feed);
  gcode.extruderOnFwd();

  for (int layer=0; layer<numLayers; layer++) {

    if(useCSurface) {
      r = cSurface.path[layer][0];    
      for (int i=0; i<cNumVertices; i++) {
        float a1 = i*(TWO_PI/cNumVertices) + sectionAngle[0];
        float x = truncate(r*sin(a1));
        float y = truncate(r*cos(a1));
        float z = truncate(layer*carve + firstLayerHeight);
        gcode.moveTo(x, y, z, feed);
      }
      float a1 = sectionAngle[0];
      float x = truncate(r*sin(a1));
      float y = truncate(r*cos(a1));
      float z = truncate(layer*carve + firstLayerHeight);
      gcode.moveTo(x, y, z, feed);
    }

    if(useMSurface) {    
      r = mSurface.path[layer][0];        
      for (int i=0; i<mNumVertices; i++) {
        float a1 = i*(TWO_PI/mNumVertices) + sectionAngle[0];
        float x = truncate(r*sin(a1));
        float y = truncate(r*cos(a1));
        float z = truncate(layer*carve + firstLayerHeight);
        gcode.moveTo(x, y, z, feed);
      }
      float a1 = sectionAngle[0];
      float x = truncate(r*sin(a1));
      float y = truncate(r*cos(a1));
      float z = truncate(layer*carve + firstLayerHeight);
      gcode.moveTo(x, y, z, feed);
    }

    if(useYSurface) {    
      r = ySurface.path[layer][0];     
      for (int i=0; i<yNumVertices; i++) {
        float a1 = i*(TWO_PI/yNumVertices) + sectionAngle[0];
        float x = truncate(r*sin(a1));
        float y = truncate(r*cos(a1));
        float z = truncate(layer*carve + firstLayerHeight);
        gcode.moveTo(x, y, z, feed);
      }
      float a1 = sectionAngle[0];
      float x = truncate(r*sin(a1));
      float y = truncate(r*cos(a1));
      float z = truncate(layer*carve + firstLayerHeight);
      gcode.moveTo(x, y, z, feed);
    }
  }

  gcode.extruderOff(); 
  gcode.moveTo(0, 0, ringHeight + firstLayerHeight, feed);
  gcode.moveTo(0, 0, ringHeight + firstLayerHeight + 10, feed);


  String savePath = selectOutput("Save G-code...");  // Opens file chooser
  if (savePath == null) {
    guiPrint = (String)("Export Failed :(");
  } 
  else {
    gcode.writeToFile(savePath+".gcode");
    guiPrint = (String)("Exported: "+ savePath+".gcode");
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "paranel_v0_1_2" });
  }
}
