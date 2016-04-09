package reaper

class SystemTerminationReaper extends Reaper {
  // Shutdown
  def allSoulsReaped(): Unit = context.system.terminate()
}