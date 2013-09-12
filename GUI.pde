

// =================================================================================== //
//                                    MISC LABLES/BUTTONS
// =================================================================================== //

void drawButtons() {
  
  
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

void drawCrossSection() {

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
  text(""+float(int(ringHeight*10))/10,0,6);
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

void drawTopView() {

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
  text(""+float(int(fingerWidth*10))/10,0,6);
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
      strokeWeight(2.5);
      fill(backgroundColor);
      ellipse(sPx,sPy,15,15);
      fill(guiColor2,100);
      ellipse(sPx,sPy,15,15);
      noFill();
    }
    else {
      strokeWeight(1.5);
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
    strokeWeight(0.5);
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


void arrow(int x, int y, float arrowLength, float rotation) {
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


void roundedRect(float x, float y, float w, float h, float r) {

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

void dashedCircle(float x, float y, float diameter, float dash, float gap) {

  float perimeter = PI*diameter;

  int numDashes = floor(perimeter/(dash+gap));

  float theta = TWO_PI/numDashes;
  float thetaDash = theta * dash/(dash+gap);

  for (int i=0; i<numDashes; i++) {
    arc(x, y, diameter, diameter, i*theta, i*theta + thetaDash);
  }
}


//Note: works as if rectMode(CORNER)
void hatch45Fill (float x, float y, float rectWidth, float rectHeight, float gap) {

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

void drawPreview() {

  preview.beginDraw();
  preview.background(backgroundColor);
  preview.perspective(PI/10, previewWidth/previewHeight, 1, 1000);
  preview.pushMatrix();
  preview.translate(previewWidth/2,previewHeight/2, -1300);
  preview.rotateX(PI/2 - 0.5);
  preview.translate(0,0, -80);

  //preview.smooth();
  preview.stroke(#000000);
  preview.strokeWeight(1);
  preview.fill(#FF0000,100);
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

