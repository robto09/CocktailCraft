package com.cocktailcraft.android.ui.components

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CocktailThumbnailUrlTest {

    private val photo = "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg"

    @Test
    fun `small slots get the medium variant`() {
        assertEquals("$photo/medium", cocktailThumbnailUrl(photo, targetSize = 200))
        assertEquals("$photo/medium", cocktailThumbnailUrl(photo, targetSize = 350))
    }

    @Test
    fun `large or unsized slots keep the original`() {
        assertEquals(photo, cocktailThumbnailUrl(photo, targetSize = 351))
        assertEquals(photo, cocktailThumbnailUrl(photo, targetSize = null))
    }

    @Test
    fun `other urls are left alone`() {
        assertNull(cocktailThumbnailUrl(null, targetSize = 200))
        assertEquals("$photo/small", cocktailThumbnailUrl("$photo/small", targetSize = 200))
        val other = "https://example.com/images/media/drink/a.jpg"
        assertEquals(other, cocktailThumbnailUrl(other, targetSize = 200))
    }
}
