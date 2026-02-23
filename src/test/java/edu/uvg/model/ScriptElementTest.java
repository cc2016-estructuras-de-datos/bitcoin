package edu.uvg.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para ScriptElement: conversiones boolean/int y casos borde. */
class ScriptElementTest {

    // ── toBoolean ─────────────────────────────────────────────────────

    @Test
    void emptyArray_isFalse() {
        assertFalse(new ScriptElement(new byte[0]).toBoolean());
    }

    @Test
    void allZeroArray_isFalse() {
        assertFalse(new ScriptElement(new byte[]{0x00, 0x00}).toBoolean());
    }

    @Test
    void negativeZero_0x80_isFalse() {
        // Bitcoin Script: 0x80 es "cero negativo", también es FALSE
        assertFalse(new ScriptElement(new byte[]{(byte) 0x80}).toBoolean());
    }

    @Test
    void nonZeroByte_isTrue() {
        assertTrue(new ScriptElement(new byte[]{0x01}).toBoolean());
    }

    @Test
    void staticTRUE_isTrue() {
        assertTrue(ScriptElement.TRUE.toBoolean());
    }

    @Test
    void staticFALSE_isFalse() {
        assertFalse(ScriptElement.FALSE.toBoolean());
    }

    // ── toInt ─────────────────────────────────────────────────────────

    @Test
    void emptyArray_toInt_isZero() {
        assertEquals(0, new ScriptElement(new byte[0]).toInt());
    }

    @Test
    void fromInt_1_roundTrips() {
        ScriptElement el = ScriptElement.fromInt(1);
        assertEquals(1, el.toInt());
    }

    @Test
    void fromInt_negative_roundTrips() {
        ScriptElement el = ScriptElement.fromInt(-5);
        assertEquals(-5, el.toInt());
    }

    @Test
    void fromInt_zero_isFalse() {
        ScriptElement el = ScriptElement.fromInt(0);
        assertFalse(el.toBoolean());
    }

    @Test
    void toInt_moreThan4Bytes_throwsArithmetic() {
        byte[] big = new byte[5];
        big[4] = 0x01;
        assertThrows(ArithmeticException.class,
                () -> new ScriptElement(big).toInt());
    }

    // ── equals ───────────────────────────────────────────────────────

    @Test
    void sameContent_isEqual() {
        ScriptElement a = new ScriptElement(new byte[]{0x01, 0x02});
        ScriptElement b = new ScriptElement(new byte[]{0x01, 0x02});
        assertEquals(a, b);
    }

    @Test
    void differentContent_isNotEqual() {
        ScriptElement a = new ScriptElement(new byte[]{0x01});
        ScriptElement b = new ScriptElement(new byte[]{0x02});
        assertNotEquals(a, b);
    }

    // ── copy ─────────────────────────────────────────────────────────

    @Test
    void copy_isIndependent() {
        ScriptElement original = new ScriptElement(new byte[]{0x01});
        ScriptElement copy = original.copy();
        assertEquals(original, copy);
        assertNotSame(original, copy);
    }

    // ── constructor null ──────────────────────────────────────────────

    @Test
    void nullData_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScriptElement(null));
    }
}
