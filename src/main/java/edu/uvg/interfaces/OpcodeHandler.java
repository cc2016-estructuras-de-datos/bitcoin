package edu.uvg.interfaces;

import edu.uvg.exceptions.ScriptExecutionException;

import java.util.Deque;

/**
 * Contrato funcional que debe implementar todo opcode del intérprete.
 * Cada implementación recibe la pila de evaluación y un operando opcional
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
@FunctionalInterface
public interface OpcodeHandler {

    /**
     * Ejecuta la lógica del opcode sobre la pila de evaluación.
     *
     * @param stack pila principal de evaluación (ArrayDeque<byte[]>)
     * @param operand datos adicionales del opcode, null si no aplica
     * @throws ScriptExecutionException si la ejecución del opcode falla
     */
    void execute(Deque<byte[]> stack, byte[] operand) throws ScriptExecutionException;
}
