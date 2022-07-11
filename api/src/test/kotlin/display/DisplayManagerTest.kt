package display

import driver.newCompiler

fun main(args: Array<String>){
    newCompiler(args){
        loadArgs {
            require(minLength = 1)
            arg {
                it.name == "test" && it.value == "true"
            }
        }
        display {
            error {
                source = "Hello, world!"
                line = 1
                pos = 0 .. 4
                message = "Expected keyword, but instead found 'Hello'"
                file = "test.ag"
            }
            warning {
                source = "Hello, world!"
                line = 1
                pos = 5 .. 5
                message = "Expected whitespace but instead found ','"
                file = "test.ag"
            }
            info {
                source = "Hello, world!"
                line = 1
                pos = 7 .. 11
                message = "'world' can be capitalized"
                file = "test.ag"
            }
        }
    }
}