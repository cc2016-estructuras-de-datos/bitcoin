package edu.uvg.interpreter;

import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;
import edu.uvg.model.ScriptElement;
import edu.uvg.model.ScriptToken;
import edu.uvg.opcodes.DataOpcodes;
import edu.uvg.stack.EvaluationStack;

import java.util.List;

/**
 * Motor principal de ejecución del intérprete de Bitcoin Script.
 *
 * Responsabilidad:
 *   - Recibe una lista de ScriptTokens (ya parseados).
 *   - Itera de izquierda a derecha.
 *   - Para cada token: si es DATA hace push; si es OPCODE consulta
 *     el OpcodeRegistry y ejecuta el handler correspondiente.
 *   - Maneja directamente OP_IF / OP_NOTIF / OP_ELSE / OP_ENDIF
 *     mediante un contador de bloque condicional activo.
 *   - Al terminar, determina si el script es válido:
 *       pila no vacía Y cima == TRUE.
 *   - En modo --trace imprime el estado de la pila tras cada instrucción.
 *
 * Diseño:
 *   - Usa EvaluationStack como pila interna.
 *   - Usa OpcodeRegistry (tabla de despacho) para resolver handlers.
 *   - Usa ScriptToken (modelo de token parseado).
 *
 * @author Franco
 * @version 1.0
 */
public class ScriptInterpreter {

    private final EvaluationStack stack;
    private final OpcodeRegistry  registry;
    private final boolean         traceMode;

    /**
     * @param traceMode si es true, imprime el estado de la pila
     *                  tras cada instrucción ejecutada
     */
    public ScriptInterpreter(boolean traceMode) {
        this.stack     = new EvaluationStack();
        this.registry  = new OpcodeRegistry();
        this.traceMode = traceMode;
    }

    /**
     * Ejecuta el script y retorna el resultado de la validación.
     *
     * @param tokens lista de ScriptToken producida por ScriptParser
     * @return true si la validación fue exitosa, false en caso contrario
     */
    public boolean execute(List<ScriptToken> tokens) {

        // ── Estado para OP_IF / OP_NOTIF / OP_ELSE / OP_ENDIF ──────────
        // conditionStack almacena si el bloque actual debe ejecutarse (true)
        // o ignorarse (false). Permite bloques anidados.
        java.util.Deque<Boolean> conditionStack = new java.util.ArrayDeque<>();

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      Intérprete de Bitcoin Script — UVG          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        try {
            for (ScriptToken token : tokens) {

                //  Decidir si el token debe ejecutarse
                boolean shouldExecute = conditionStack.isEmpty() || conditionStack.peek();

                // Manejo especial de tokens de control de flujo
                if (token.isOpcode()) {
                    OpcodeType op = token.getOpcode();

                    switch (op) {

                        case OP_IF:
                            if (shouldExecute) {
                                // Evalúa la cima: si es TRUE entra al bloque
                                byte[] top = stack.pop(OpcodeType.OP_IF);
                                boolean condition = new ScriptElement(top).toBoolean();
                                conditionStack.push(condition);
                            } else {
                                // Bloque padre falso: este bloque también falso
                                conditionStack.push(false);
                            }
                            traceIfEnabled(token.getRaw());
                            continue;

                        case OP_NOTIF:
                            if (shouldExecute) {
                                byte[] top = stack.pop(OpcodeType.OP_NOTIF);
                                boolean condition = !new ScriptElement(top).toBoolean();
                                conditionStack.push(condition);
                            } else {
                                conditionStack.push(false);
                            }
                            traceIfEnabled(token.getRaw());
                            continue;

                        case OP_ELSE:
                            if (!conditionStack.isEmpty()) {
                                // Invertir el bloque activo
                                boolean current = conditionStack.pop();
                                // Solo invertir si el bloque padre se estaba ejecutando
                                boolean parentExecuting = conditionStack.isEmpty() || conditionStack.peek();
                                conditionStack.push(parentExecuting && !current);
                            }
                            traceIfEnabled(token.getRaw());
                            continue;

                        case OP_ENDIF:
                            if (!conditionStack.isEmpty()) {
                                conditionStack.pop();
                            }
                            traceIfEnabled(token.getRaw());
                            continue;

                        default:
                            break;
                    }
                }

                //Si el bloque está desactivado, ignorar el token
                if (!shouldExecute) {
                    continue;
                }

                //  Ejecuta el token
                if (token.isData()) {
                    // Dato: empujar directamente a la pila
                    OpcodeHandler pushHandler = DataOpcodes.pushData();
                    pushHandler.execute(stack.getInternalDeque(), token.getOperand());

                } else {
                    // Opcode: buscar en el registry y ejecutar
                    OpcodeType opcode = token.getOpcode();
                    OpcodeHandler handler = registry.getHandler(opcode);

                    if (handler == null) {
                        throw new ScriptExecutionException(opcode,
                                "Opcode no implementado: " + opcode.name());
                    }

                    handler.execute(stack.getInternalDeque(), token.getOperand());
                }

                // Traza
                traceIfEnabled(token.getRaw());
            }

        } catch (ScriptExecutionException e) {
            System.out.println();
            System.out.println("✗ SCRIPT FALLIDO: " + e.getMessage());
            return false;
        }

        // Verificación del resultado final
        return checkResult();
    }

    /**
     * Verifica el estado final de la pila.
     *
     * Según el protocolo Bitcoin:
     *   - La pila debe tener al menos un elemento.
     *   - El elemento en la cima debe ser TRUE (no vacío y no todo-cero).
     *
     * @return true si el script es válido
     */
    private boolean checkResult() {
        System.out.println();

        if (stack.isEmpty()) {
            System.out.println(" SCRIPT FALLIDO: la pila está vacía al finalizar.");
            return false;
        }

        byte[] top;
        try {
            top = stack.peek(null);
        } catch (ScriptExecutionException e) {
            System.out.println("✗ SCRIPT FALLIDO: error al leer la cima.");
            return false;
        }

        ScriptElement topElement = new ScriptElement(top);
        if (!topElement.toBoolean()) {
            System.out.println("SCRIPT FALLIDO: la cima de la pila es FALSE.");
            System.out.println("  Estado final de la pila: " + stack);
            return false;
        }

        System.out.println(" SCRIPT VÁLIDO: la cima de la pila es TRUE.");
        System.out.println("  Estado final de la pila: " + stack);
        return true;
    }

    /**
     * Imprime el estado actual de la pila si el modo traza está activo.
     *
     * @param instruction la instrucción que acaba de ejecutarse
     */
    private void traceIfEnabled(String instruction) {
        if (traceMode) {
            System.out.printf("[TRACE] %-25s → %s%n", instruction, stack);
        }
    }
}
