package edu.uvg.exceptions;

import edu.uvg.interfaces.ScriptException;
import edu.uvg.model.OpcodeType;

/**
 * Excepción base del intérprete de Bitcoin Script.
 * Toda falla durante la evaluación de un opcode debe lanzar
 * esta excepción o una subclase de ella.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class ScriptExecutionException extends RuntimeException implements ScriptException {

    private final OpcodeType opcode;
    private final String reason;

    /**
     * @param opcode opcode que causó el fallo
     * @param reason descripción legible del motivo del fallo
     */
    public ScriptExecutionException(OpcodeType opcode, String reason) {
        super(String.format("[%s] %s", opcode != null ? opcode.name() : "UNKNOWN", reason));
        this.opcode = opcode;
        this.reason = reason;
    }

    public OpcodeType getOpcode() {
        return opcode;
    }

    public String getReason() {
        return reason;
    }
}
