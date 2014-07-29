package com.neverwinterdp.server.shell;

import com.neverwinterdp.server.gateway.Command;

@ShellCommandConfig(name = "sparkngin")
public class SparknginShellCommand extends ShellCommand {
  public SparknginShellCommand() {
    add("hello", HelloSparknginCommand.class);
  }
  
  static public class HelloSparknginCommand extends ShellSubCommand {
    HelloSparkngin.Options options = new HelloSparkngin.Options();
    
    public void execute(Shell shell, ShellContext ctx, Command command) {
      try {
        command.mapAll(options);
        new HelloSparkngin().run(options);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}