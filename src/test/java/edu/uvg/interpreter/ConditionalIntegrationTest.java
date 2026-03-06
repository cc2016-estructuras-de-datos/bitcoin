package edu.uvg.interpreter;

import edu.uvg.model.ScriptToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para condicionales:
 * OP_IF, OP_NOTIF, OP_ELSE, OP_ENDIF anidados y casos borde.
 *
 * Estos tests verifican el manejo correcto del conditionStack
 * en el ScriptInterpreter para estructuras condicionales complejas.
 */
class ConditionalIntegrationTest {

    private ScriptParser parser;

    @BeforeEach
    void setUp() {
        parser = new ScriptParser();
    }

    private boolean run(String... tokens) {
        List<ScriptToken> parsed = parser.parse(Arrays.asList(tokens));
        return new ScriptInterpreter(false).execute(parsed);
    }

    // ── Condicionales simples ─────────────────────────────────────────

    @Test
    void opIf_true_executesBlock_isValid() {
        assertTrue(run("OP_1", "OP_IF", "OP_1", "OP_ENDIF"));
    }

    @Test
    void opIf_false_skipsBlock_isInvalid() {
        assertFalse(run("OP_0", "OP_IF", "OP_1", "OP_ENDIF"));
    }

    @Test
    void opIfElse_trueCondition_executesIfNotElse() {
        // OP_1 → IF ejecuta OP_1, ELSE empuja OP_0 (no se ejecuta) → válido
        assertTrue(run("OP_1", "OP_IF", "OP_1", "OP_ELSE", "OP_0", "OP_ENDIF"));
    }

    @Test
    void opIfElse_falseCondition_executesElseNotIf() {
        // OP_0 → IF salta, ELSE empuja OP_1 → válido
        assertTrue(run("OP_0", "OP_IF", "OP_0", "OP_ELSE", "OP_1", "OP_ENDIF"));
    }

    @Test
    void opNotIf_false_executesBlock() {
        assertTrue(run("OP_0", "OP_NOTIF", "OP_1", "OP_ENDIF"));
    }

    @Test
    void opNotIf_true_skipsBlock() {
        assertFalse(run("OP_1", "OP_NOTIF", "OP_1", "OP_ENDIF"));
    }

    // ── Condicionales anidados ────────────────────────────────────────

    @Test
    void nestedIf_bothTrue_innerBlockExecutes() {
        // OP_1 OP_IF   OP_1 OP_IF   OP_1   OP_ENDIF  OP_ENDIF
        assertTrue(run(
            "OP_1", "OP_IF",
                "OP_1", "OP_IF",
                    "OP_1",
                "OP_ENDIF",
            "OP_ENDIF"
        ));
    }

    @Test
    void nestedIf_outerFalse_innerNeverExecutes() {
        // Outer=false → ningún bloque se ejecuta → pila vacía → inválido
        assertFalse(run(
            "OP_0", "OP_IF",
                "OP_1", "OP_IF",
                    "OP_1",
                "OP_ENDIF",
            "OP_ENDIF"
        ));
    }

    @Test
    void nestedIf_outerTrue_innerFalse_onlyOuterBranchRuns() {
        // Outer=true, inner=false → inner skipped, outer ELSE empuja OP_1
        assertTrue(run(
            "OP_1", "OP_IF",
                "OP_0", "OP_IF",
                    "OP_0",           // no debe ejecutarse
                "OP_ELSE",
                    "OP_1",           // sí debe ejecutarse
                "OP_ENDIF",
            "OP_ENDIF"
        ));
    }

    @Test
    void nestedIfElse_complex_correctBranchSelected() {
        // Outer=false → ELSE ejecuta → inner=true → IF ejecuta OP_1
        assertTrue(run(
            "OP_0", "OP_IF",
                "OP_0",               // no se ejecuta
            "OP_ELSE",
                "OP_1", "OP_IF",
                    "OP_1",           // sí se ejecuta
                "OP_ENDIF",
            "OP_ENDIF"
        ));
    }

    @Test
    void nestedIfElse_allFalse_elsePushesTrue() {
        // Outer=false ELSE → inner=false ELSE → OP_1
        assertTrue(run(
            "OP_0", "OP_IF",
                "OP_0",
            "OP_ELSE",
                "OP_0", "OP_IF",
                    "OP_0",
                "OP_ELSE",
                    "OP_1",
                "OP_ENDIF",
            "OP_ENDIF"
        ));
    }

    // ── Condicionales con aritmética ──────────────────────────────────

    @Test
    void ifWithArithmetic_condition5greaterThan3_isValid() {
        // Empuja 5 y 3, compara 5>3=TRUE → IF empuja OP_1 → válido
        assertTrue(run(
            "05", "03", "OP_GREATERTHAN",
            "OP_IF", "OP_1", "OP_ENDIF"
        ));
    }

    @Test
    void ifWithArithmetic_condition2greaterThan7_isInvalid() {
        // 2>7=FALSE → IF saltado → pila vacía → inválido
        assertFalse(run(
            "02", "07", "OP_GREATERTHAN",
            "OP_IF", "OP_1", "OP_ENDIF"
        ));
    }

    @Test
    void ifElseWithArithmetic_usesBranchBasedOnComparison() {
        // 3 < 5 = TRUE → IF empuja OP_1, ELSE saltado → válido
        assertTrue(run(
            "03", "05", "OP_LESSTHAN",
            "OP_IF", "OP_1", "OP_ELSE", "OP_0", "OP_ENDIF"
        ));
    }

    // ── Casos borde ───────────────────────────────────────────────────

    @Test
    void endif_withoutIf_doesNotCrash() {
        // OP_ENDIF sin IF previo: conditionStack vacío → no crashea
        assertTrue(run("OP_1", "OP_ENDIF"));
    }

    @Test
    void emptyIfBlock_trueCondition_stackEmptyAfter_isInvalid() {
        // IF con bloque vacío → pila vacía al final → inválido
        assertFalse(run("OP_1", "OP_IF", "OP_ENDIF"));
    }

    @Test
    void ifBlock_withDrop_leavesStackEmpty_isInvalid() {
        // IF ejecuta DROP que consume OP_1 → pila vacía → inválido
        assertFalse(run("OP_1", "OP_1", "OP_IF", "OP_DROP", "OP_ENDIF"));
    }
}