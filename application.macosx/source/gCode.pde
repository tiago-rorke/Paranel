
CodeThread gcode = new CodeThread();

float truncate(float a){
 if ( a > 0 )
   return float(floor(a * pathPrecision))/pathPrecision;
 else
   return float(ceil(a * pathPrecision))/pathPrecision;
}


void makeRaft(int raftWidth, int raftHeight, int baseFeed, int interfaceFeed, float baseStep, float interfaceStep) {

  int numBaseLines = floor(raftWidth/baseStep);
  int numInterfaceLines = floor(raftHeight/interfaceStep);

  gcode.moveTo(-raftWidth/2, -40, 0, interfaceFeed);
  gcode.extruderOnFwd();

  for (int i=0; i<numBaseLines; i+=2) {
    gcode.moveTo(-raftWidth/2+i*baseStep, -raftHeight/2, 0.35, baseFeed);    
    gcode.moveTo(-raftWidth/2+i*baseStep, raftHeight/2, 0.35, baseFeed);
    gcode.moveTo(-raftWidth/2+(i+1)*baseStep, raftHeight/2, 0.35, baseFeed);
    gcode.moveTo(-raftWidth/2+(i+1)*baseStep, -raftHeight/2, 0.35, baseFeed);
  }
  gcode.extruderOff();
  gcode.moveTo(raftWidth/2, raftHeight/2+1, 0.35, interfaceFeed);
  gcode.extruderOnFwd();
  for (int i=0; i<numInterfaceLines; i+=2) {
    gcode.moveTo(raftWidth/2, raftHeight/2-i*interfaceStep, 0.95, interfaceFeed);    
    gcode.moveTo(-raftWidth/2, raftHeight/2-i*interfaceStep, 0.95, interfaceFeed);
    gcode.moveTo(-raftWidth/2, raftHeight/2-(i+1)*interfaceStep, 0.95, interfaceFeed);
    gcode.moveTo(raftWidth/2, raftHeight/2-(i+1)*interfaceStep, 0.95, interfaceFeed);
  }
  gcode.extruderOff();
}


void exportGcode() {

  guiPrint = (String)("Generating G-code...");

  int feed = feedRate;
  float firstLayerHeight = 1.5;

  gcode.setDefault();

  makeRaft(25, 25, 560, 2000, 2.5, 0.7);

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

