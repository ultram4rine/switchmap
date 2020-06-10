import scala.sys.process.Process

val Success = 0
val Error = 1

PlayKeys.playRunHooks += baseDirectory.map(FrontendRunHook.apply).value

val isWindows = System.getProperty("os.name").toLowerCase().contains("win")

def runOnCommandline(script: String)(implicit dir: File): Int = {
  if (isWindows) { Process("cmd /c " + script, dir) }
  else { Process(script, dir) }
} !

def isNodeModulesInstalled(implicit dir: File): Boolean =
  (dir / "node_modules").exists()

def runNpmInstall(implicit dir: File): Int =
  if (isNodeModulesInstalled) Success
  else runOnCommandline(FrontendCommands.dependencyInstall)

def ifNodeModulesInstalled(task: => Int)(implicit dir: File): Int =
  if (runNpmInstall == Success) task
  else Error

def executeProdBuild(implicit dir: File): Int =
  ifNodeModulesInstalled(runOnCommandline(FrontendCommands.build))

lazy val `ui-test` = TaskKey[Unit]("run ui tests when testing application.")

lazy val `ui-prod-build` =
  TaskKey[Unit]("run ui build when packaging the application.")

`ui-prod-build` := {
  implicit val userInterfaceRoot: File = baseDirectory.value / "ui"
  if (executeProdBuild != Success)
    throw new Exception("oops! ui build crashed.")
}

dist := (dist dependsOn `ui-prod-build`).value

stage := (stage dependsOn `ui-prod-build`).value
