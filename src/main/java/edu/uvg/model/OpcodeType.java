package edu.uvg.model;

/**
 * Enumeración de todos los opcodes soportados por el intérprete.
 * Cada constante almacena su código hexadecimal correspondiente
 * según la especificación de Bitcoin Script.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public enum OpcodeType {

    // ── Datos y literales ─────────────────────────────────────────────
    OP_0        (0x00), // cuando el programador quiere empujar el número cero
    OP_FALSE    (0x00), // se usa cuando quiere empujar explícitamente un valor falso
    OP_1        (0x51),
    OP_2        (0x52),
    OP_3        (0x53),
    OP_4        (0x54),
    OP_5        (0x55),
    OP_6        (0x56),
    OP_7        (0x57),
    OP_8        (0x58),
    OP_9        (0x59),
    OP_10       (0x5a),
    OP_11       (0x5b),
    OP_12       (0x5c),
    OP_13       (0x5d),
    OP_14       (0x5e),
    OP_15       (0x5f),
    OP_16       (0x60),
    OP_TRUE     (0x51),
    PUSHDATA1   (0x4c),
    PUSHDATA2   (0x4d),

    // ── Pila ──────────────────────────────────────────────────────────
    OP_DUP      (0x76),
    OP_DROP     (0x75),
    OP_SWAP     (0x7c),
    OP_OVER     (0x7b),

    // ── Comparación y lógica (Ingeniero 3) ───────────────────────────
    OP_EQUAL        (0x87),
    OP_EQUALVERIFY  (0x88),
    OP_NOT          (0x91),
    OP_BOOLAND      (0x9a),
    OP_BOOLOR       (0x9b),

    // ── Aritmética (Ingeniero 3) ──────────────────────────────────────
    OP_ADD              (0x93),
    OP_SUB              (0x94),
    OP_NUMEQUALVERIFY   (0x9d),
    OP_LESSTHAN         (0x9f),
    OP_GREATERTHAN      (0xa0),

    // ── Control de flujo (Ingeniero 1) ───────────────────────────────
    OP_IF       (0x63),
    OP_NOTIF    (0x64),
    OP_ELSE     (0x67),
    OP_ENDIF    (0x68),
    OP_VERIFY   (0x69),
    OP_RETURN   (0x6a),

    // ── Criptografía (Ingeniero 3) ────────────────────────────────────
    OP_SHA256       (0xa8),
    OP_HASH160      (0xa9),
    OP_HASH256      (0xaa),
    OP_CHECKSIG     (0xac),
    OP_CHECKSIGVERIFY(0xad);

    // ─────────────────────────────────────────────────────────────────

    private final int hexCode;

    OpcodeType(int hexCode) {
        this.hexCode = hexCode;
    }

    /**
     * Retorna el código hexadecimal del opcode según Bitcoin Script.
     */
    public int getHexCode() {
        return hexCode;
    }

    /**
     * Busca el OpcodeType correspondiente a un código hexadecimal.
     *
     * @param code código hexadecimal a buscar
     * @return el OpcodeType correspondiente
     * @throws IllegalArgumentException si el código no corresponde a ningún opcode
     */
    public static OpcodeType fromHex(int code) {
        for (OpcodeType op : values()) {
            if (op.hexCode == code) {
                return op;
            }
        }
        throw new IllegalArgumentException(
                String.format("Opcode desconocido: 0x%02X", code)
        );
    }

    /**
     * Retorna el nombre del opcode junto con su código hex.
     */
    @Override
    public String toString() {
        return String.format("%s(0x%02X)", name(), hexCode);
    }
}
