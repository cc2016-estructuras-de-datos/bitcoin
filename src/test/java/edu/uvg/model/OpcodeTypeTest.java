package edu.uvg.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para OpcodeType: códigos hex correctos y lookup por código. */
class OpcodeTypeTest {

    @Test
    void op0_hasCorrectHexCode() {
        assertEquals(0x00, OpcodeType.OP_0.getHexCode());
    }

    @Test
    void opDup_hasCorrectHexCode() {
        assertEquals(0x76, OpcodeType.OP_DUP.getHexCode());
    }

    @Test
    void opHash160_hasCorrectHexCode() {
        assertEquals(0xa9, OpcodeType.OP_HASH160.getHexCode());
    }

    @Test
    void opChecksig_hasCorrectHexCode() {
        assertEquals(0xac, OpcodeType.OP_CHECKSIG.getHexCode());
    }

    @Test
    void fromHex_validCode_returnsCorrectOpcode() {
        assertEquals(OpcodeType.OP_DUP, OpcodeType.fromHex(0x76));
    }

    @Test
    void fromHex_unknownCode_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> OpcodeType.fromHex(0xFF));
    }

    @Test
    void toString_containsNameAndHex() {
        String s = OpcodeType.OP_DUP.toString();
        assertTrue(s.contains("OP_DUP"));
        assertTrue(s.contains("76"));
    }
}
