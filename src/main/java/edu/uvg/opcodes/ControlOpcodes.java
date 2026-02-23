package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;
import edu.uvg.model.ScriptElement;

/**
 * Implementación de los opcodes de control de flujo:
 * OP_VERIFY, OP_RETURN.
 *
 * NOTA sobre OP_IF / OP_NOTIF / OP_ELSE / OP_ENDIF:
 *   Estos opcodes requieren manejo a nivel del ScriptInterpreter
 *   no se pueden implementar como simples OpcodeHandler sobre la pila,
 *   ya que necesitan saltar bloques enteros de instrucciones.
 *   El ScriptInterpreter los detecta y los maneja directamente
 *   mediante un contador de bloque condicional activo.
 *
 * @author Franco
 * @version 1.0
 */
public final class ControlOpcodes {

    private ControlOpcodes() {}

    /**
     * OP_VERIFY (0x69)
     *
     * Extrae el tope de la pila. Si es FALSE (vacío o cero),
     * lanza ScriptExecutionException y el script falla.
     * Si es TRUE, simplemente lo descarta y la ejecución continúa.
     *
     * Antes:   [ a | ... ]
     * Después: [ ... ]       (si a es TRUE)
     *          FALLO         (si a es FALSE)
     */
    public static OpcodeHandler opVerify() {
        return (stack, operand) -> {
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_VERIFY);
            }
            byte[] top = stack.pop();
            ScriptElement element = new ScriptElement(top);
            if (!element.toBoolean()) {
                throw new ScriptExecutionException(
                        OpcodeType.OP_VERIFY,
                        "El valor en la cima de la pila es FALSE.");
            }
        };
    }

    /**
     * OP_RETURN (0x6a)
     *
     * Marca el script como inválido inmediatamente.
     * Se usa para embeber metadata en outputs no gastables (OP_RETURN outputs).
     * Siempre lanza excepción sin importar el estado de la pila.
     */
    public static OpcodeHandler opReturn() {
        return (stack, operand) -> {
            throw new ScriptExecutionException(
                    OpcodeType.OP_RETURN,
                    "OP_RETURN encontrado: el script es inválido por diseño.");
        };
    }
}
