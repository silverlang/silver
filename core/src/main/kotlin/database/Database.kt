package database

import arrow.core.*
import database.cache.input.CacheInputFile
import database.cache.input.CacheInputModule
import database.cache.output.CacheOutputFile
import database.cache.output.CacheOutputModule
import database.data.DataElement
import database.managers.CacheInputManager
import database.managers.CacheOutputManager
import driver.Driver
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.*
import project.Project
import project.canon.toCanonicalName
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists
class Database(
    private val driver: Driver,
    private val project: Project
) {
    private var rootSection: Option<DataElement.Section> = DataElement.Section(project.projectName).some()

    private val cacheDirPath = Path("${project.path}/target/cache")
    private val cacheFilePath = Path("$cacheDirPath/cache.db")

    private val cacheInputManager = CacheInputManager(cacheDirPath, cacheFilePath, project)
    private val cacheOutputManager = CacheOutputManager(cacheDirPath, cacheFilePath, project)

    fun loadCacheFile(){
        cacheInputManager.getCacheInputFileIfAbsent().let {
            this.rootSection = it.input
        }
    }

    fun createSection(name: String, parent: Option<DataElement.Section> = rootSection): Option<DataElement.Section> =
        parent.map{
            val child = DataElement.Section(name)
            it.children.add(child)
            child
        }

    fun getSection(name: String, parent: Option<DataElement.Section> = rootSection): Option<DataElement.Section> =
        parent.flatMap { parent ->
            val sec = parent.children
                .filterIsInstance<DataElement.Section>()
                .find { it.name == name }

            if(sec == null){
                for(child in parent.children){
                    val ret = when(child){
                        is DataElement.Section -> getSection(name, child.toOption())
                        else -> continue
                    }
                    if(ret.isDefined()){
                        return ret
                    }
                }
            }
            sec.toOption()
        }

    fun createGroup(name: String, parent: Option<DataElement.Section> = rootSection): Option<DataElement.Group> =
        parent.map {
            val child = DataElement.Group(name)
            it.children.add(child)
            child
        }

    fun getGroup(name: String, parent: Option<DataElement.Section> = rootSection): Option<DataElement.Group> =
        parent.flatMap { parent ->
            val sec = parent.children
                .filterIsInstance<DataElement.Group>()
                .find { it.name == name }

            if(sec == null){
                for(child in parent.children){
                    val group = when(child){
                        is DataElement.Section -> getGroup(name, child.toOption())
                        else -> continue
                    }
                    if(group.isDefined()){
                        return@flatMap group
                    }
                }
            }
            sec.toOption()
        }

    fun createUnit(name: String, data: ByteArray, parent: Option<DataElement>): Option<DataElement.Unit> =
        parent.map {
            val unit = DataElement.Unit(name, data)
            when(it){
                is DataElement.Section ->
                    it.children.add(unit)
                is DataElement.Group ->
                    it.children.add(unit)
                else -> Unit
            }
            unit
        }

    fun getUnit(name: String, parent: Option<DataElement.Group>): Option<DataElement.Unit> =
        parent.flatMap { parent ->
            parent.children.find {
                it.name == name
            }.toOption()
        }

    inline fun <reified T> serialize(data: T): ByteArray =
        Cbor.encodeToByteArray(data)

    fun unloadCacheFile(){
        cacheOutputManager
            .getCacheOutputFileIfAbsent(rootSection)
            .dump()
    }
}