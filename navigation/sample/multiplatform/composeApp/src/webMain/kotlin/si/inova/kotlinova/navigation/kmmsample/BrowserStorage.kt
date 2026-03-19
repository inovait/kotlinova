package si.inova.kotlinova.navigation.kmmsample

external interface BrowserStorage {
    fun setItem(key: String, value: String)
    fun getItem(key: String): String?
    fun removeItem(key: String)
    fun clear()
}

external val sessionStorage: BrowserStorage
