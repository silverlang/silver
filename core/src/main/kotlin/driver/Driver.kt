package driver

import args.CLIArgsLoader
import display.DisplayManager

class Driver(
    val cliArgsLoader: CLIArgsLoader,
//    val database: Database
) {
    val displayManager: DisplayManager = DisplayManager(this)

    var errorOccurred = false
}