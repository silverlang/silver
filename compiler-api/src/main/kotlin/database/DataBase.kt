package database

const val MAX_DATABASE_SIZE = 1024*1024*8 //8 MB, may raise in the future

/**
 * This class allows for any kind of data to be stored and transformed easily.
 *
 * @param size The max size of data that can be stored. This is the initial capacity of the internal data [ArrayList]
 * @property data This is the data [ArrayList] which is where all the data items will be stored
 * @constructor The constructor is entirely optional and only needs a max capacity size which is defaulted to [MAX_DATABASE_SIZE]
 */
class DataBase<T, K>(size: Int = MAX_DATABASE_SIZE){
    private var data: HashMap<T, K> = HashMap(size)

    /**
     * This method is a basic push method which allows for a simple addition to the internal [data]
     *
     * @param data The data to be pushed to the [DataBase]
     */
    fun push(key: T, data: K){
        this.data[key] = data
    }

    /**
     * This method allows for a callback to be used for querying the [DataBase] to find a specific element.
     *
     * The callback is passed into [data].[find]
     *
     * @param callback A callback to be used for finding an element.
     * @return This method returns whatever [find] returns
     * @author Alex Couch
     * @since 0.0.1-alpha
     * @see find
     */
    fun query(callback: (key: T, value: K)->Boolean) =
        this.data.entries.find{
            callback(it.key, it.value)
        }?.value

    /**
     * This method removes an element given a reference to that element
     *
     * This method passes [ref] into [java.util.Collection.remove] through [ArrayList]
     *
     * @param ref A reference to the element to be removed in the database
     */
    fun remove(ref: T){
        this.data.remove(ref)
    }

    /**
     * This is another form of [remove] which allows for a callback to execute code for dynamically remove multiple elements given a predicate
     *
     * NOTE: This function returns a transformed version of this object, so use this as a functional way of removing multiple elements
     *
     * @param callback This callback is the predicate to be used for removing specific elements
     * @return This returns a version of this class which has been transformed
     */
    fun remove(callback: (key: T, value: K)->Boolean): DataBase<T, K> =
        transform { key, item ->
            if(!callback(key, item)) item else null
        }

    /**
     * This is for transforming the database into a new database, in a functional way
     *
     * @param callback This callback executes the logic used for transforming each element in the database.
     * @return The new value or null to be excluded
     * @author Alex Couch
     * @since 0.0.1-alpha
     */
    fun <R> transform(callback: (T, K)->R?): DataBase<T, R> =
        build {
            for((key, item) in this@DataBase.data){
                this.push(key, callback(key, item) ?: continue)
            }
        }

    companion object{
        fun <T,K> build(callback: DataBase<T, K>.()->Unit): DataBase<T, K>{
            val database = DataBase<T,K>()
            database.callback()
            return database
        }
    }
}