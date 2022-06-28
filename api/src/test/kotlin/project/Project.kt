package project

import arrow.core.getOrElse
import project.canon.toCanonicalName
import project.modules.module

fun main(){
    Project("Test", "src", "test").apply {
        project {
            makeSourceModule("test_files") {
                makeTestTargetModule(this@makeSourceModule.canonicalName.name) {
                    loadFile("test"){
                        input.tap {
                            makeTestTargetFile(this@loadFile.canonName.name, it)
                        }.tapNone {
                            println("No such file 'test'")
                        }
                    }
                    loadFile("test2") {
                        input.tap {
                            makeTestTargetFile(this@loadFile.canonName.name, it)
                        }.tapNone {
                            println("No such file 'test2'")
                        }
                    }
                }
            }
            findSourceFile {
                it.canonName.name == "test"
            }.tap {
                println("Found source file ${it.canonName.name}")
            }.tapNone {
                println("Could not find source file 'test'")
            }
            findSourceModule {
                it.canonName.name == "test_files"
            }.tap { mod ->
                module {
                    mod.findSourceFile { file ->
                        file.canonName.name == "test"
                    }.tap { file ->
                        println("Source data of file ${file.canonName}: ${file.input.getOrElse { "" }}")
                    }
                }
            }.tapNone {
                println("Could not find source module 'test_files'")
            }

            makeSubproject("test_subproject", "Test Subproject") {
                makeSourceModule("test_files") {
                    makeTestTargetModule(canonicalName.name){
                        loadFile("test1") {
                            input.tap {
                                makeTestTargetFile(canonName.name, it)
                            }.tapNone {
                                println("No such file 'test1'")
                            }
                        }
                        loadFile("test2") {
                            input.tap {
                                makeTestTargetFile(canonName.name, it)
                            }.tapNone {
                                println("No such file test2")
                            }
                        }
                    }

                }
            }
            findTargetFile {
                it.canonName.name == "test1"
            }.map { it as TestTargetFileData }.tap {
                println("Data from ${it.canonName}: ${it.data}")
            }
        }
    }

}