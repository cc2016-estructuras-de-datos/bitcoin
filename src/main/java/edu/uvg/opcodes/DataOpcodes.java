package edu.uvg.opcodes;

import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.ScriptElement;

/**
 * Implementación de los opcodes de empuje de datos literales:
 * OP_0 / OP_FALSE, OP_1 / OP_TRUE, OP_2..OP_16, PUSHDATA1, PUSHDATA2.
 *
 * Estos opcodes no leen la pila, solo empujan valores constantes.
 *
 * @author Franco
 * @version 1.0
 */
public final class DataOpcodes {

    private DataOpcodes() {}

    /**
     * OP_0 / OP_FALSE (0x00)
     *
     * Empuja un array vacío False a la pila.
     * En Bitcoin Script, el array vacío es el único valor
     * que representa el número cero y el booleano false.
     *
     * Antes:   [ ... ]
     * Después: [ [] | ... ]
     */
    public static OpcodeHandler opFalse() {
        return (stack, operand) -> {
            stack.push(ScriptElement.FALSE.getData());
        };
    }

    /**
     * OP_1 / OP_TRUE (0x51)
     *
     * Empuja el valor 1 (TRUE) a la pila.
     *
     * Antes:   [ ... ]
     * Después: [ [01] | ... ]
     */
    public static OpcodeHandler opTrue() {
        return (stack, operand) -> {
            stack.push(ScriptElement.TRUE.getData());
        };
    }

    /**
     * OP_N — para OP_2 hasta OP_16.
     *
     * Empuja el entero n a la pila codificado en formato
     * little-endian de Bitcoin Script.
     *
     * @param n valor entre 2 y 16 inclusive
     */
    public static OpcodeHandler opN(int n) {
        return (stack, operand) -> {
            stack.push(ScriptElement.fromInt(n).getData());
        };
    }

    /**
     * PUSHDATA — empuja el operand directamente a la pila.
     *
     * Se usa cuando el token del script es un dato arbitrario
     * (firma, clave pública, hash, etc.) que el ScriptParser
     * ya convirtió a byte[] y pasó como operand.
     *
     * Antes:   [ ... ]
     * Después: [ operand | ... ]
     */
    public static OpcodeHandler pushData() {
        return (stack, operand) -> {
            if (operand == null) {
                throw new ScriptExecutionException(null,
                        "PUSHDATA: se recibió un operand nulo.");
            }
            stack.push(operand);
        };
    }
}
