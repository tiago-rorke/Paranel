
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

