class Surface {

  color surfaceColor;

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

  Surface(color inColor, float offset, int numPoints, int inNumVertices) {

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

  void calcPath() {

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
          
          bezierPath[layer][j][3] = map(dAngleRev,0,PI,0,pow(dAngleRev,0.22)*fingerWidth*0.6);//0.5522);
          bezierPath[layer][j][2] = map(dAngleFwd,0,PI,0,pow(dAngleFwd,0.22)*fingerWidth*0.6);//0.5522);
          
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

  void renderSection(int sectionNum) {

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
          strokeWeight(1.5);
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
  void renderSweeps(int layer, boolean inPreview) {

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

