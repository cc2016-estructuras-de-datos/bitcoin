package edu.uvg.interpreter;

import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;
import edu.uvg.opcodes.CryptoComparisonOpcodes;
import edu.uvg.opcodes.DataOpcodes;
import edu.uvg.opcodes.StackOpcodes;
import edu.uvg.opcodes.ControlOpcodes;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registro central que mapea cada {@link OpcodeType}
 * a su {@link OpcodeHandler} correspondiente.
 *
 * Actúa como una tabla de despacho (dispatch table):
 * el ScriptInterpreter consulta aquí qué handler ejecutar
 * para cada opcode, sin necesidad de un switch gigante.
 *
 * Patrón: Command + Registry.
 *   Cada OpcodeHandler es un Command (función que opera sobre la pila).
 *   El OpcodeRegistry es el registro que los organiza.
 *
 * Extensibilidad:
 *   Para agregar un nuevo opcode basta con:
 *     1. Añadirlo a OpcodeType.
 *     2. Implementar su OpcodeHandler en la clase de opcodes correspondiente.
 *     3. Registrarlo aquí con un put().
 *
 * @author Franco
 * @version 1.0
 */
public class OpcodeRegistry {

    /** Mapa principal: OpcodeType a OpcodeHandler */
    private final Map<OpcodeType, OpcodeHandler> registry;

    /**
     * Constructor: registra todos los opcodes soportados.
     */
    public OpcodeRegistry() {
        registry = new EnumMap<>(OpcodeType.class);
        registerAll();
    }

    /**
     * Registra todos los opcodes del intérprete.
     * Organizado por categorías para facilitar la lectura.
     */
    private void registerAll() {

        // ── Datos y literales (Franco) ─────────────────────────────────
        registry.put(OpcodeType.OP_0,       DataOpcodes.opFalse());
        registry.put(OpcodeType.OP_FALSE,   DataOpcodes.opFalse());
        registry.put(OpcodeType.OP_1,       DataOpcodes.opTrue());
        registry.put(OpcodeType.OP_TRUE,    DataOpcodes.opTrue());
        registry.put(OpcodeType.OP_2,       DataOpcodes.opN(2));
        registry.put(OpcodeType.OP_3,       DataOpcodes.opN(3));
        registry.put(OpcodeType.OP_4,       DataOpcodes.opN(4));
        registry.put(OpcodeType.OP_5,       DataOpcodes.opN(5));
        registry.put(OpcodeType.OP_6,       DataOpcodes.opN(6));
        registry.put(OpcodeType.OP_7,       DataOpcodes.opN(7));
        registry.put(OpcodeType.OP_8,       DataOpcodes.opN(8));
        registry.put(OpcodeType.OP_9,       DataOpcodes.opN(9));
        registry.put(OpcodeType.OP_10,      DataOpcodes.opN(10));
        registry.put(OpcodeType.OP_11,      DataOpcodes.opN(11));
        registry.put(OpcodeType.OP_12,      DataOpcodes.opN(12));
        registry.put(OpcodeType.OP_13,      DataOpcodes.opN(13));
        registry.put(OpcodeType.OP_14,      DataOpcodes.opN(14));
        registry.put(OpcodeType.OP_15,      DataOpcodes.opN(15));
        registry.put(OpcodeType.OP_16,      DataOpcodes.opN(16));

        // ── Pila (Franco) ──────────────────────────────────────────────
        registry.put(OpcodeType.OP_DUP,     StackOpcodes.opDup());
        registry.put(OpcodeType.OP_DROP,    StackOpcodes.opDrop());
        registry.put(OpcodeType.OP_SWAP,    StackOpcodes.opSwap());
        registry.put(OpcodeType.OP_OVER,    StackOpcodes.opOver());

        // ── Control de flujo (Franco) ──────────────────────────────────
        registry.put(OpcodeType.OP_VERIFY,  ControlOpcodes.opVerify());
        registry.put(OpcodeType.OP_RETURN,  ControlOpcodes.opReturn());
        // OP_IF / OP_NOTIF / OP_ELSE / OP_ENDIF: manejados directamente
        // por ScriptInterpreter

        // ── Comparación y criptografía  ─────────────────────────
        registry.put(OpcodeType.OP_EQUAL,        CryptoComparisonOpcodes.opEqual());
        registry.put(OpcodeType.OP_EQUALVERIFY,  CryptoComparisonOpcodes.opEqualVerify());
        registry.put(OpcodeType.OP_HASH160,      CryptoComparisonOpcodes.opHash160());
        registry.put(OpcodeType.OP_CHECKSIG,     CryptoComparisonOpcodes.opCheckSigMock());
    }

    /**
     * Retorna el OpcodeHandler asociado al opcode dado.
     *
     * @param opcode el opcode a buscar
     * @return el handler correspondiente, o null si no está registrado
     */
    public OpcodeHandler getHandler(OpcodeType opcode) {
        return registry.get(opcode);
    }

    /**
     * Verifica si un opcode tiene handler registrado.
     *
     * @param opcode el opcode a verificar
     * @return true si está registrado
     */
    public boolean isRegistered(OpcodeType opcode) {
        return registry.containsKey(opcode);
    }
}
