package edu.uvg.model;

/**
 * Representa un token del script ya parseado, listo para ejecutarse.
 *
 * Un token puede ser:
 *   - Un OPCODE: token con su OpcodeType y sin operand.
 *   - Un DATO:token sin OpcodeType y con operand (el byte[] del dato).
 *
 * Esta separación permite que el ScriptParser devuelva una lista plana
 * de ScriptTokens que el ScriptInterpreter puede ejecutar secuencialmente
 * sin necesidad de re-parsear nada durante la ejecución.
 *
 * @author Franco
 * @version 1.0
 */
public class ScriptToken {

    /** Tipo de token */
    public enum TokenType { OPCODE, DATA }

    private final TokenType tokenType;
    // null si es DATA
    private final OpcodeType opcode;
    // null si es OPCODE sin dato asociado
    private final byte[] operand;
    // string original (para mensajes de error y traza)
    private final String raw;

    /**
     * Constructor para tokens de tipo OPCODE.
     *
     * @param opcode  el opcode correspondiente
     * @param raw     string original del token (ej: "OP_DUP")
     */
    public ScriptToken(OpcodeType opcode, String raw) {
        this.tokenType = TokenType.OPCODE;
        this.opcode    = opcode;
        this.operand   = null;
        this.raw       = raw;
    }

    /**
     * Constructor para tokens de tipo DATA.
     *
     * @param operand  el byte[] del dato a empujar
     * @param raw      string original del token (ej: "3045022100...")
     */
    public ScriptToken(byte[] operand, String raw) {
        this.tokenType = TokenType.DATA;
        this.opcode    = null;
        this.operand   = operand;
        this.raw       = raw;
    }

    /**
     * @return el tipo de token (OPCODE o DATA)
     */
    public TokenType getTokenType() { return tokenType; }

    /**
     * @return el opcode asociado si es token OPCODE, null de otro modo
     */
    public OpcodeType getOpcode()   { return opcode; }

    /**
     * @return el operando en bytes si es token DATA, null de otro modo
     */
    public byte[] getOperand()      { return operand; }

    /**
     * @return representación en texto original del token (p.ej. "OP_DUP")
     */
    public String getRaw()          { return raw; }

    /**
     * @return true si el token es un opcode
     */
    public boolean isOpcode() { return tokenType == TokenType.OPCODE; }

    /**
     * @return true si el token es un dato literal
     */
    public boolean isData()   { return tokenType == TokenType.DATA; }

    @Override
    public String toString() {
        return raw;
    }
}
