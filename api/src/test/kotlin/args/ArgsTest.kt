package args

import driver.newCompiler

fun main(args: Array<String>){
    newCompiler(args){
        loadArgs{
            require(minLength = 2)
            arg{
                it.name == "friend"
            }
            arg {
                it.name == "test" && it.value == "true"
            }
        }
    }
}