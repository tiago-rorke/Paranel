




class CodeThread {
  String codeString = "";
  String endChar = "\n";
  
  int defaultToolTemp = 230;
  int defaultBuildPlatformTemp = 105;
  

  ArrayList<PathCommand> commands = new ArrayList();


  //set unit to milimeters
  void setUnitmm() {
    commands.add( new MillimeterCommand());
  }

  //set units to inches 
  void setUnitInch() {
    commands.add( new InchCommand());
  }

  //move head to absolute position
  void moveTo(float x, float y, float z, float speed) {
    commands.add( new MoveCommand(x,y,z,speed));
  }

  //turn extruder on fwd
  void extruderOnFwd() {
    commands.add( new ExtruderOnFwdCommand());
  }

  //turn extruder on rev
  void extruderOnRev() {
    commands.add( new ExtruderOnRevCommand());
  }

  //turn extruder off
  void extruderOff() {
    commands.add( new ExtruderOffCommand());
  } 

  //set tool temp
  void setToolTemp(int toolNum, int temp) {
    commands.add( new ToolTempCommand( toolNum,  temp));
  } 

  //set build platform temp
  void setBuildPlatformTemp(int toolNum, int temp) {
    commands.add( new BuildPlatformTempCommand( toolNum,  temp));
  } 

  //set tool speed
  void setToolSpeed(int toolNum, int temp) {
    commands.add( new ToolSpeedCommand( toolNum,  temp));
  } 
  
  void setAbsolutePositioning() {
    commands.add( new setAbsolutePositioningCommand());
  } 
  
  void setCurrentHome(int x, int y, int z){
   commands.add( new setCurrentHomeCommand(x,y,z));
  }
  
  void waitForToolToHeat(int toolNum ){
       commands.add( new waitForToolToHeatCommand(toolNum));
  }
  
  void pause(int secs ){
       commands.add( new PauseCommand(secs));
  }
  
  void generateRaft(){
   commands.add( new RaftCommand());
  }


void setDefault(){
  
  setToolTemp(0,defaultToolTemp);
  setBuildPlatformTemp(0,defaultBuildPlatformTemp);
  setUnitmm();
  setAbsolutePositioning();
  setCurrentHome(0,0,0);
  setToolSpeed(0,extrudeRate);
  waitForToolToHeat(0);
  
}




  void printToConsole() {

    for(int i = 0; i < commands.size(); i++) {
      PathCommand command = commands.get(i);
      System.out.println(command.print());
    }
  } 






  void render() {
    beginShape();
    for(int i = 0; i < commands.size(); i++) {
      PathCommand command = commands.get(i);
      if(command instanceof MoveCommand)
        command.render();
    }
    endShape();
  }







  void writeToFile(String path) {
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

