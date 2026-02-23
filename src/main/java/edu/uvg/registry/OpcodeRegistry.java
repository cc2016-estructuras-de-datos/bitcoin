package edu.uvg.registry;

import edu.uvg.data.*;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.EnumMap;

/**
 * Registro central de opcodes del intérprete de Bitcoin Script.
 * Mapea cada OpcodeType a su implementación OpcodeHandler correspondiente
 * mediante un EnumMap, garantizando lookup en O(1) real por indexación directa.
 *
 * Patrón de uso:
 *   OpcodeRegistry registry = OpcodeRegistry.build();
 *   registry.get(OpcodeType.OP_DUP).execute(stack, null);
 *
 * Pueden registrar sus propios handlers mediante
 * register() sin modificar el código de esta clase (principio Open/Closed).
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpcodeRegistry {

    private final EnumMap<OpcodeType, OpcodeHandler> handlers;

    private OpcodeRegistry() {
        this.handlers = new EnumMap<>(OpcodeType.class);
    }

    /**
     * Construye e inicializa el registro con todos los handlers
     * correspondientes.
     * Los demás ingenieros invocan register() sobre la instancia
     * retornada para agregar sus propios handlers.
     *
     * @return instancia de OpcodeRegistry lista para usar
     */
    public static OpcodeRegistry build() {
        OpcodeRegistry registry = new OpcodeRegistry();
        registry.registerDefaults();
        return registry;
    }

    /**
     * Registra los handlers.
     * Se invoca únicamente desde build().
     */
    private void registerDefaults() {

        register(OpcodeType.OP_0,      new OpFalse());
        register(OpcodeType.OP_FALSE,  new OpFalse());

        register(OpcodeType.OP_1,      new OpN(1));
        register(OpcodeType.OP_2,      new OpN(2));
        register(OpcodeType.OP_3,      new OpN(3));
        register(OpcodeType.OP_4,      new OpN(4));
        register(OpcodeType.OP_5,      new OpN(5));
        register(OpcodeType.OP_6,      new OpN(6));
        register(OpcodeType.OP_7,      new OpN(7));
        register(OpcodeType.OP_8,      new OpN(8));
        register(OpcodeType.OP_9,      new OpN(9));
        register(OpcodeType.OP_10,     new OpN(10));
        register(OpcodeType.OP_11,     new OpN(11));
        register(OpcodeType.OP_12,     new OpN(12));
        register(OpcodeType.OP_13,     new OpN(13));
        register(OpcodeType.OP_14,     new OpN(14));
        register(OpcodeType.OP_15,     new OpN(15));
        register(OpcodeType.OP_16,     new OpN(16));
        register(OpcodeType.OP_TRUE,   new OpN(1));

        register(OpcodeType.PUSHDATA1, new OpPushData1());
        register(OpcodeType.PUSHDATA2, new OpPushData2());

        // ── Pila ──────────────────────────────────────────────────────
        register(OpcodeType.OP_DUP,    new OpDup());
        register(OpcodeType.OP_DROP,   new OpDrop());
    }

    /**
     * Registra o sobreescribe el handler de un opcode.
     * Usado para extender el registro.
     *
     * Complejidad: O(1) real.
     *
     * @param type    opcode a registrar
     * @param handler implementación del opcode
     * @throws IllegalArgumentException si algún parámetro es null
     */
    public void register(OpcodeType type, OpcodeHandler handler) {
        if (type == null || handler == null) {
            throw new IllegalArgumentException("OpcodeType y OpcodeHandler no pueden ser null.");
        }
        handlers.put(type, handler);
    }

    /**
     * Retorna el handler asociado al opcode indicado.
     *
     * Complejidad: O(1) real — indexación directa por ordinal del enum.
     *
     * @param type opcode a buscar
     * @return handler correspondiente
     * @throws ScriptExecutionException si el opcode no está registrado
     */
    public OpcodeHandler get(OpcodeType type) throws ScriptExecutionException {
        OpcodeHandler handler = handlers.get(type);
        if (handler == null) {
            throw new ScriptExecutionException(type,
                    "Opcode no registrado en el intérprete.");
        }
        return handler;
    }

    /**
     * @param type opcode a verificar
     * @return true si el opcode tiene un handler registrado
     */
    public boolean contains(OpcodeType type) {
        return handlers.containsKey(type);
    }

    /**
     * @return cantidad de opcodes actualmente registrados
     */
    public int size() {
        return handlers.size();
    }
}
