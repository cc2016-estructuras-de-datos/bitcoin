package edu.uvg.interpreter;

import edu.uvg.model.OpcodeType;
import edu.uvg.model.ScriptToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para ScriptParser: parseo de opcodes, datos mock y hexadecimales. */
class ScriptParserTest {

    private ScriptParser parser;

    @BeforeEach
    void setUp() {
        parser = new ScriptParser();
    }

    @Test
    void parseOpcode_returnsOpcodeToken() {
        List<ScriptToken> tokens = parser.parse(List.of("OP_DUP"));
        assertEquals(ScriptToken.TokenType.OPCODE, tokens.get(0).getTokenType());
        assertEquals(OpcodeType.OP_DUP, tokens.get(0).getOpcode());
    }

    @Test
    void parseMockData_returnsDataToken() {
        List<ScriptToken> tokens = parser.parse(List.of("<firma>"));
        assertEquals(ScriptToken.TokenType.DATA, tokens.get(0).getTokenType());
        assertArrayEquals("<firma>".getBytes(), tokens.get(0).getOperand());
    }

    @Test
    void parseHex_returnsDataToken() {
        List<ScriptToken> tokens = parser.parse(List.of("0102"));
        assertEquals(ScriptToken.TokenType.DATA, tokens.get(0).getTokenType());
        assertArrayEquals(new byte[]{0x01, 0x02}, tokens.get(0).getOperand());
    }

    @Test
    void parseHexWithPrefix_stripsPrefix() {
        List<ScriptToken> tokens = parser.parse(List.of("0x0A"));
        assertArrayEquals(new byte[]{0x0A}, tokens.get(0).getOperand());
    }

    @Test
    void parseMultipleTokens_preservesOrder() {
        List<ScriptToken> tokens = parser.parse(
                Arrays.asList("<firma>", "<pubKey>", "OP_DUP", "OP_HASH160"));
        assertEquals(4, tokens.size());
        assertTrue(tokens.get(0).isData());
        assertTrue(tokens.get(1).isData());
        assertTrue(tokens.get(2).isOpcode());
        assertTrue(tokens.get(3).isOpcode());
    }

    @Test
    void parseUnknownOpcode_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(List.of("OP_INVENTADO")));
    }

    @Test
    void parseInvalidToken_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(List.of("notHexNotOpcode!!")));
    }

    @Test
    void parseOddLengthHex_isPaddedCorrectly() {
        // "f" → "0f" → byte 0x0F
        List<ScriptToken> tokens = parser.parse(List.of("f"));
        assertArrayEquals(new byte[]{0x0F}, tokens.get(0).getOperand());
    }
}
