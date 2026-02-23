package edu.uvg.data;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.Deque;

/**
 * Implementación de OP_DUP (0x76).
 * Duplica el elemento en el tope de la pila y empuja la copia.
 * El elemento original permanece en su posición.
 *
 * Es el opcode más frecuente en transacciones P2PKH:
 *   scriptSig:    <firma> <pubKey>
 *   scriptPubKey: OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
 *
 * Complejidad: O(k) donde k = tamaño del tope en bytes.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpDup implements OpcodeHandler {

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {

        if (stack.isEmpty()) {
            throw new EmptyStackException(OpcodeType.OP_DUP);
        }

        // peek() retorna referencia al tope — clonar para no compartir memoria
        byte[] top = stack.peek();
        stack.push(top.clone());
    }
}
