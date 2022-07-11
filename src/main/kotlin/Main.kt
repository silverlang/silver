import driver.newCompiler

fun main(args: Array<String>){
    newCompiler(args){
        loadArgs {
            arg {
                it.name == "src"
            }
        }
    }
}