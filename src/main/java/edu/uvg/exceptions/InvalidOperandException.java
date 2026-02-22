package edu.uvg.exceptions;

import edu.uvg.model.OpcodeType;

/**
 * Se lanza cuando el operando recibido por un opcode
 * no cumple con el tamaño o formato esperado.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class InvalidOperandException extends ScriptExecutionException {

    private final int expected;
    private final int received;

    /**
     * @param opcode opcode que recibió el operando inválido
     * @param expected tamaño esperado del operando en bytes
     * @param received tamaño real recibido en bytes
     */
    public InvalidOperandException(OpcodeType opcode, int expected, int received) {
        super(opcode, String.format(
                "Operando inválido: se esperaban %d bytes, se recibieron %d.", expected, received
        ));
        this.expected = expected;
        this.received = received;
    }

    public int getExpected() {
        return expected;
    }

    public int getReceived() {
        return received;
    }
}
