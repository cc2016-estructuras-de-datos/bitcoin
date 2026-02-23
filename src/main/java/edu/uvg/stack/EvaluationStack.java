package edu.uvg.stack;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.model.OpcodeType;
import edu.uvg.model.ScriptElement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Pila principal de evaluación del intérprete de Bitcoin Script.
 * Respaldada por ArrayDeque<byte[]> para garantizar operaciones
 * push/pop/peek en O(1) amortizado sin overhead de sincronización.
 *
 * Convención: el tope de la pila corresponde al frente (head)
 * del ArrayDeque, operado mediante push/pop/peek de Deque.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class EvaluationStack {

    private final ArrayDeque<byte[]> stack;

    public EvaluationStack() {
        this.stack = new ArrayDeque<>();
    }

    /**
     * Empuja un arreglo de bytes al tope de la pila.
     *
     * Complejidad: O(1).
     *
     * @param data bytes a empujar; no puede ser null
     * @throws IllegalArgumentException si data es null
     */
    public void push(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("No se puede empujar null a la pila.");
        }
        stack.push(data.clone());
    }

    /**
     * Extrae y retorna el elemento del tope de la pila.
     *
     * Complejidad: O(1).
     *
     * @param caller opcode que realiza la operación
     * @return bytes del elemento extraído
     * @throws EmptyStackException si la pila está vacía
     */
    public byte[] pop(OpcodeType caller) throws ScriptExecutionException {
        if (stack.isEmpty()) {
            throw new EmptyStackException(caller);
        }
        return stack.pop();
    }

    /**
     * Retorna el elemento del tope sin extraerlo.
     *
     * Complejidad: O(1).
     *
     * @param caller opcode que realiza la operación
     * @return copia defensiva de los bytes del tope
     * @throws EmptyStackException si la pila está vacía
     */
    public byte[] peek(OpcodeType caller) throws ScriptExecutionException {
        if (stack.isEmpty()) {
            throw new EmptyStackException(caller);
        }
        return stack.peek().clone();
    }

    /**
     * Retorna la referencia al Deque interno.
     * El ScriptInterpreter pasa este Deque a los handlers; todas las
     * operaciones sobre él se reflejan en esta pila directamente.
     *
     * @return el ArrayDeque interno
     */
    public Deque<byte[]> getInternalDeque() {
        return stack;
    }

    /**
     * @return true si la pila no contiene elementos
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * @return cantidad de elementos actualmente en la pila
     */
    public int size() {
        return stack.size();
    }

    /**
     * Vacía completamente la pila.
     */
    public void clear() {
        stack.clear();
    }

    /**
     * Retorna una lista con el estado actual de la pila, ordenada de
     * tope a fondo, como ScriptElements. No modifica la pila.
     *
     * Complejidad: O(n).
     *
     * @return lista de ScriptElement representando el estado de la pila
     */
    public List<ScriptElement> snapshot() {
        List<ScriptElement> result = new ArrayList<>(stack.size());
        for (byte[] element : stack) {
            result.add(new ScriptElement(element));
        }
        return result;
    }

    /**
     * Representación legible de la pila para el modo --trace.
     * Formato: [ tope | ... | fondo ]
     */
    @Override
    public String toString() {
        if (stack.isEmpty()) return "[ vacía ]";
        StringBuilder sb = new StringBuilder("[ ");
        boolean first = true;
        for (byte[] element : stack) {
            if (!first) sb.append("| ");
            sb.append(new ScriptElement(element));
            sb.append(" ");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
