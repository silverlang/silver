package driver

import args.CLIArgsLoader
import args.CLIArgsLoaderScope
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import display.DisplayManager
import display.DisplayScope

class CompilerScope(
    private val cliArgsLoaderScope: CLIArgsLoaderScope,
    private val displayScope: DisplayScope,
){
    fun loadArgs(block: CLIArgsLoaderScope.()->Unit){
        cliArgsLoaderScope.block()
        cliArgsLoaderScope.run()
    }

    fun display(block: DisplayScope.()->Unit){
        displayScope.block()
    }
}

fun newCompiler(args: Array<String>, block: CompilerScope.()->Unit){
    val cliArgsLoader = CLIArgsLoader(args)
    val driver = Driver(cliArgsLoader)
    val cliArgsLoaderScope = CLIArgsLoaderScope(driver, driver.cliArgsLoader, driver.displayManager)
    val displayScope = DisplayScope(driver.displayManager)

    CompilerScope(cliArgsLoaderScope, displayScope).apply {
        block()
    }
}