package database

import args.CLIArgsLoader
import driver.Driver
import project.Project

@kotlinx.serialization.Serializable
data class Person(val name: String, val age: Int)

fun Database.testLoadCache(){
    loadCacheFile()
    val people = getSection("people")
    println(people)
}

fun Database.testUnloadCache(){
    createSection("people")
    val people = getSection("people")
    createGroup("adults", people)
    val adults = getGroup("adults", people)
    val adult1 = serialize(Person("alex", 24))
    createUnit("alex", adult1, adults)
    unloadCacheFile()
}

fun main(){
    val driver = Driver(CLIArgsLoader(emptyArray()))
    val project = Project("test", "src", "test")
    Database(driver, project).apply{
        testUnloadCache()
    }
    Database(driver, project).apply{
        testLoadCache()
    }
}