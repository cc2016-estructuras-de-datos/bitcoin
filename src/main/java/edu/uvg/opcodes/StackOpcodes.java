package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.Arrays;

/**
 * Implementación de los opcodes de manipulación de pila:
 * OP_DUP, OP_DROP, OP_SWAP, OP_OVER.
 *
 * Estos opcodes no consumen datos externos (operand = null),
 * solo reordenan o duplican los elementos existentes en la pila.
 *
 * @author Franco
 * @version 1.0
 */
public final class StackOpcodes {

    private StackOpcodes() {}

    /**
     * OP_DUP (0x76)
     *
     * Duplica el elemento en la cima de la pila.
     * Se genera una copia independiente (no se comparte la referencia)
     * para evitar efectos secundarios si un opcode posterior modifica el array.
     *
     * Antes:   [ a | ... ]
     * Después: [ a | a | ... ]
     *
     * Uso en P2PKH: duplica la clave pública antes de OP_HASH160,
     * de modo que el original quede disponible para OP_CHECKSIG.
     */
    public static OpcodeHandler opDup() {
        return (stack, operand) -> {
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_DUP);
            }
            // peek devuelve copia defensiva en EvaluationStack → push directo
            byte[] top = stack.peek();
            stack.push(Arrays.copyOf(top, top.length));
        };
    }

    /**
     * OP_DROP (0x75)
     *
     * Elimina el elemento de la cima de la pila sin usarlo.
     *
     * Antes:   [ a | ... ]
     * Después: [ ... ]
     */
    public static OpcodeHandler opDrop() {
        return (stack, operand) -> {
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_DROP);
            }
            stack.pop();
        };
    }

    /**
     * OP_SWAP (0x7c)
     *
     * Intercambia los dos elementos superiores de la pila.
     *
     * Antes:   [ a | b | ... ]  (a = cima)
     * Después: [ b | a | ... ]
     */
    public static OpcodeHandler opSwap() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_SWAP);
            }
            byte[] a = stack.pop();
            byte[] b = stack.pop();
            stack.push(a);
            stack.push(b);
        };
    }

    /**
     * OP_OVER (0x7b)
     *
     * Copia el segundo elemento desde la cima y lo empuja al tope.
     * El elemento original no se elimina.
     *
     * Antes:   [ a | b | ... ]  (a = cima)
     * Después: [ b | a | b | ... ]
     */
    public static OpcodeHandler opOver() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_OVER);
            }
            byte[] a = stack.pop();           // extraemos la cima temporalmente
            byte[] b = stack.peek();          // vemos el segundo elemento
            byte[] bCopy = Arrays.copyOf(b, b.length);
            stack.push(a);                    // restauramos la cima
            stack.push(bCopy);               // empujamos la copia de b al tope
        };
    }
}
