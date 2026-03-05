package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;
import edu.uvg.model.ScriptElement;

/**
 * Implementación de opcodes aritméticos y lógicos:
 * OP_ADD, OP_SUB, OP_NOT, OP_BOOLAND, OP_BOOLOR,
 * OP_NUMEQUALVERIFY, OP_LESSTHAN, OP_GREATERTHAN.
 *
 * Todos los operandos se interpretan como enteros en formato
 * little-endian con bit de signo (convención Bitcoin Script).
 * El resultado se empuja como ScriptElement.
 *
 * @author James
 */
public final class ArithmeticOpcodes {

    private ArithmeticOpcodes() {}

    /**
     * OP_ADD (0x93)
     * Extrae dos enteros de la pila y empuja su suma.
     *
     * Antes:   [ a | b | ... ]
     * Después: [ b+a | ... ]
     */
    public static OpcodeHandler opAdd() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_ADD);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            stack.push(ScriptElement.fromInt(b + a).getData());
        };
    }

    /**
     * OP_SUB (0x94)
     * Extrae dos enteros de la pila y empuja b - a (segundo - primero).
     *
     * Antes:   [ a | b | ... ]  (a = cima)
     * Después: [ b-a | ... ]
     */
    public static OpcodeHandler opSub() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_SUB);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            stack.push(ScriptElement.fromInt(b - a).getData());
        };
    }

    /**
     * OP_NOT (0x91)
     * Si el tope es 0 empuja 1; si es cualquier otro valor empuja 0.
     * Opera sobre el valor numérico, no como inversión bit a bit.
     *
     * Antes:   [ a | ... ]
     * Después: [ !a | ... ]
     */
    public static OpcodeHandler opNot() {
        return (stack, operand) -> {
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_NOT);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            stack.push(ScriptElement.fromInt(a == 0 ? 1 : 0).getData());
        };
    }

    /**
     * OP_BOOLAND (0x9a)
     * Empuja 1 si ambos elementos son distintos de 0, de lo contrario 0.
     *
     * Antes:   [ a | b | ... ]
     * Después: [ a&&b | ... ]
     */
    public static OpcodeHandler opBoolAnd() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_BOOLAND);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            boolean result = (a != 0) && (b != 0);
            stack.push(ScriptElement.fromInt(result ? 1 : 0).getData());
        };
    }

    /**
     * OP_BOOLOR (0x9b)
     * Empuja 1 si al menos uno de los dos elementos es distinto de 0.
     *
     * Antes:   [ a | b | ... ]
     * Después: [ a||b | ... ]
     */
    public static OpcodeHandler opBoolOr() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_BOOLOR);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            boolean result = (a != 0) || (b != 0);
            stack.push(ScriptElement.fromInt(result ? 1 : 0).getData());
        };
    }

    /**
     * OP_NUMEQUALVERIFY (0x9d)
     * Verifica que los dos enteros en la cima sean iguales.
     * Si no lo son, lanza ScriptExecutionException.
     * No deja nada en la pila (ambos elementos son consumidos).
     *
     * Antes:   [ a | b | ... ]
     * Después: [ ... ]   (si a == b)
     *          FALLO     (si a != b)
     */
    public static OpcodeHandler opNumEqualVerify() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_NUMEQUALVERIFY);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            if (a != b) {
                throw new ScriptExecutionException(
                        OpcodeType.OP_NUMEQUALVERIFY,
                        String.format("Los valores no son iguales numéricamente: %d != %d", b, a));
            }
        };
    }

    /**
     * OP_LESSTHAN (0x9f)
     * Empuja 1 si b < a (el segundo elemento es menor que el primero).
     *
     * Antes:   [ a | b | ... ]  (a = cima)
     * Después: [ b<a | ... ]
     */
    public static OpcodeHandler opLessThan() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_LESSTHAN);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            stack.push(ScriptElement.fromInt(b < a ? 1 : 0).getData());
        };
    }

    /**
     * OP_GREATERTHAN (0xa0)
     * Empuja 1 si b > a (el segundo elemento es mayor que el primero).
     *
     * Antes:   [ a | b | ... ]  (a = cima)
     * Después: [ b>a | ... ]
     */
    public static OpcodeHandler opGreaterThan() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_GREATERTHAN);
            }
            int a = new ScriptElement(stack.pop()).toInt();
            int b = new ScriptElement(stack.pop()).toInt();
            stack.push(ScriptElement.fromInt(b > a ? 1 : 0).getData());
        };
    }
}