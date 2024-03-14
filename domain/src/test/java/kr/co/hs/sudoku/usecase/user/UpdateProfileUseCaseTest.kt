package kr.co.hs.sudoku.usecase.user

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class UpdateProfileUseCaseTest {
    private lateinit var testRepository: ProfileRepository
    private lateinit var usecase: UpdateProfileUseCase

    @Before
    fun before() {
        testRepository = TestProfileRepository()
        usecase = UpdateProfileUseCase(testRepository)
    }

    @Test
    fun do_test() = runTest(timeout = Duration.INFINITE) {
        val entity = ProfileEntityImpl("0", "updated0")
        val updatedEntity = usecase(entity, this)

        assertEquals("updated0", updatedEntity.displayName)
        assertEquals("message0", updatedEntity.message)
        assertEquals("https://cdn-icons-png.flaticon.com/512/21/21104.png", updatedEntity.iconUrl)
    }
}