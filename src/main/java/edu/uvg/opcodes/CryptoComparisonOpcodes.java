package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Implementación de OP_EQUAL, OP_EQUALVERIFY,
 * OP_HASH160 y OP_CHECKSIG (mock).
 *
 * @author James Sipac
 */
public final class CryptoComparisonOpcodes {

    private CryptoComparisonOpcodes() {
    }

    /**
     * Compara los dos elementos superiores de la pila.
     * Empuja 1 si son iguales, vacío si no.
     */
    public static OpcodeHandler opEqual() {
        return (stack, operand) -> {

            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_EQUAL);
            }

            byte[] a = stack.pop();
            byte[] b = stack.pop();

            boolean equals = Arrays.equals(a, b);

            stack.push(equals ? new byte[] { 1 } : new byte[0]);
        };
    }

    /**
     * Verifica igualdad. Si no son iguales, falla.
     */
    public static OpcodeHandler opEqualVerify() {
        return (stack, operand) -> {

            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_EQUALVERIFY);
            }

            byte[] a = stack.pop();
            byte[] b = stack.pop();

            if (!Arrays.equals(a, b)) {
                throw new ScriptExecutionException(
                        OpcodeType.OP_EQUALVERIFY,
                        "Los valores no son iguales.");
            }
        };
    }

    /**
     * Aplica HASH160: RIPEMD160(SHA256(data)).
     */
    public static OpcodeHandler opHash160() {
        return (stack, operand) -> {

            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_HASH160);
            }

            byte[] data = stack.pop();

            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] shaHash = sha256.digest(data);

                MessageDigest ripemd160 = MessageDigest.getInstance("RIPEMD160");
                byte[] result = ripemd160.digest(shaHash);

                stack.push(result);

            } catch (NoSuchAlgorithmException e) {
                throw new ScriptExecutionException(
                        OpcodeType.OP_HASH160,
                        "Algoritmo criptográfico no disponible.");
            }
        };
    }

    /**
     * Simulación de verificación de firma.
     * Empuja 1 si firma y clave no están vacías.
     */
    public static OpcodeHandler opCheckSigMock() {
        return (stack, operand) -> {

            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_CHECKSIG);
            }

            byte[] signature = stack.pop();
            byte[] publicKey = stack.pop();

            boolean valid = signature.length > 0 &&
                    publicKey.length > 0;

            stack.push(valid ? new byte[] { 1 } : new byte[0]);
        };
    }
}