

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
import java.awt.*; // hatched lines?
import toxi.math.*;

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

float fingerWidth = 17.0;
float minFingerWidth = 8.0;
float ringHeight = 10.0;

float maxRingDiameter = 30.0;
float maxRingHeight = 15.0;
float minRingHeight = 1.0;

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

float carve = 0.5;
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

color backgroundColor = #333333;

color guiColor = #cccccc;
color guiColor2 = #ffffff;  // section handles etc.
int opacity1 = 70;
int opacity2 = 40;
float line1 = 0.8;
float line2 = 1;

color cyan = #00aeef;
color magenta = #ec008c;
color yellow = #fff200;


// Buttons ========================== //

color fingerWidthInputFill = backgroundColor;
color ringHeightInputFill = backgroundColor;
color exportButtonFill = backgroundColor;

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

void setup() {
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


void draw() {
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


void setLables_English() {

  lablesEN[1] = "Cross Section";
  lablesEN[2] = "Top View";
  lablesEN[3] = "Preview";

  lablesEN[4] = "Ring Height in mm?";
  lablesEN[5] = "Ring size in mm?";
}




void initialiseSurfaces() {

  setVars();

  cSurface = new Surface(cyan, 1, cNumCpoints, cNumVertices);
  mSurface = new Surface(magenta, 1.5, mNumCpoints, mNumVertices);
  ySurface = new Surface(yellow, 2, yNumCpoints, yNumVertices);
}

void setVars() {

  numLayers = ceil(ringHeight/carve);
  //numVertices = ceil((fingerWidth*PI)/minFacetWidth);
  sectionAngle[0] = -PI/4;
}

