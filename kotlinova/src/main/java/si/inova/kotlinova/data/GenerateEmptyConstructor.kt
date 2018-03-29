package si.inova.kotlinova.data

/**
 * All classes with that annotation will receive synthetic empty constructor so they can be
 * created from external sources such as Firebase or Gson.
 *
 * @author Matej Drobnic
 */
annotation class GenerateEmptyConstructor