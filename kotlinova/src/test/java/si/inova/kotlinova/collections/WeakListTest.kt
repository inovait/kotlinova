package si.inova.kotlinova.collections

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import org.mockito.InOrder
import si.inova.kotlinova.testing.LocalFunction1

/**
 * @author Matej Drobnic
 */
class WeakListTest {
    private lateinit var list: WeakList<Int>
    private lateinit var actionTarget: LocalFunction1<Int, Unit>
    private lateinit var actionOrder: InOrder

    @Before
    fun init() {
        list = WeakList()

        actionTarget = mock()
        actionOrder = inOrder(actionTarget)
    }

    @Test
    fun testStrongAdd() {
        list.add(10)
        list.add(20)
        list.add(30)

        list.forEach(actionTarget)
        actionOrder.verify(actionTarget).invoke(10)
        actionOrder.verify(actionTarget).invoke(20)
        actionOrder.verify(actionTarget).invoke(30)
        actionOrder.verifyNoMoreInteractions()
    }

    @Test
    fun testStrongAddAndRemove() {
        list.add(10)
        list.add(20)
        list.add(30)

        list.remove(20)

        list.forEach(actionTarget)
        actionOrder.verify(actionTarget).invoke(10)
        actionOrder.verify(actionTarget).invoke(30)
        actionOrder.verifyNoMoreInteractions()
    }

    @Test
    fun testStrongAddAndClear() {
        list.add(10)
        list.add(20)
        list.add(30)

        list.clear()

        list.forEach(actionTarget)
        actionOrder.verifyNoMoreInteractions()
    }

    @Test
    fun testWeakAdd() {
        addWeak(10)
        addWeak(20)
        list.add(30)

        // Force garbage collector to remove weak references
        System.gc()

        list.forEach(actionTarget)
        actionOrder.verify(actionTarget).invoke(30)
        actionOrder.verifyNoMoreInteractions()
    }

    private fun addWeak(number: Int) {
        // Add number in separate method, so we loose all strong references to it
        list.add(number)
    }
}