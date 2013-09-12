
void checkSectionHover() {

  if(dist(mouseX, mouseY, topViewX, topViewY) < (maxRingDiameter/2*rScale+50) && dist(mouseX, mouseY, topViewX, topViewY) > (fingerWidth/2*rScale)) {
    addSectionAngle = atan2(mouseX - topViewX, mouseY - topViewY);

    sectionHandleHover = false;
    addSectionHover = false;

    for (int i=0; i<numSections; i++) {
      if (abs(addSectionAngle - sectionAngle[i]) < 0.1) {
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



void addSection(Surface surface, float angle) {

  surface.cs.add(new ControlSpline(angle, surface.numCPoints, surface.initR));
  surface.rSharp = new float[numSections][numLayers];
}



void moveSection(Surface surface) {
   sectionAngle[selectedSection] = atan2(mouseX - topViewX, mouseY - topViewY);
   ControlSpline cSpline = (ControlSpline) surface.cs.get(selectedSection);
   cSpline.sectionAngle = sectionAngle[selectedSection];
   
}

