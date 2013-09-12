

void controlsCheck() {

  if (!disableControls) {

    color cPointColor = backgroundColor;    

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



void mousePressed() { 

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
    mSurface = new Surface(magenta, 1.5, mNumCpoints, mNumVertices);
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
    mSurface = new Surface(magenta, 1.5, mNumCpoints, mNumVertices);
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

void mouseReleased() {
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


ControlPoint checkSplineHandles(Surface surface, int sectionNum) {

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


void scrollFingerWidth() {
  if (fingerWidth<=maxRingDiameter-3 && fingerWidth>=minFingerWidth) {
    fingerWidth += (mouseY - pMouseY) * 0.1;
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

void scrollRingHeight() {
  if (ringHeight<=maxRingHeight && ringHeight>=minRingHeight) {
    ringHeight += (mouseY - pMouseY) * 0.05;
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

