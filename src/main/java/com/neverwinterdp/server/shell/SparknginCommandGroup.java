package com.neverwinterdp.server.shell;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@CommandGroupConfig(name = "sparkngin")
public class SparknginCommandGroup extends CommandGroup {
  public SparknginCommandGroup() {
    add("hello", HelloSparknginCommand.class);
  }
  
  @Parameters(commandDescription = "execute sparkngin hello command")
  static public class HelloSparknginCommand extends Command {
    @ParametersDelegate
    HelloSparkngin.Options options = new HelloSparkngin.Options();
    
    public void execute(ShellContext ctx) {
      try {
        new HelloSparkngin().run(options);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}