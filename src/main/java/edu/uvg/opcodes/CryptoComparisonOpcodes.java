package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Implementación de OP_EQUAL, OP_EQUALVERIFY, OP_HASH160 y OP_CHECKSIG (mock).
 *
 * @author James Sipac
 */
public final class CryptoComparisonOpcodes {

    private CryptoComparisonOpcodes() {
    }

    /**
     * Compara los dos elementos superiores de la pila. Empuja 1 si son iguales,
     * vacío si no.
     */
    public static OpcodeHandler opEqual() {
        return (stack, operand) -> {

            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_EQUAL);
            }

            byte[] a = stack.pop();
            byte[] b = stack.pop();

            boolean equals = Arrays.equals(a, b);

            stack.push(equals ? new byte[]{1} : new byte[0]);
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
     * Simulación de verificación de firma. Empuja 1 si firma y clave no están
     * vacías.
     */
    public static OpcodeHandler opCheckSigMock() {
        return (stack, operand) -> {

            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_CHECKSIG);
            }

            byte[] signature = stack.pop();
            byte[] publicKey = stack.pop();

            boolean valid = signature.length > 0
                    && publicKey.length > 0;

            stack.push(valid ? new byte[]{1} : new byte[0]);
        };
    }

    /**
     * OP_SHA256 (0xa8) Aplica SHA-256 al elemento de la cima y empuja el
     * resultado (32 bytes).
     *
     * Antes: [ data | ... ] Después: [ sha256(data) | ... ]
     */
    public static OpcodeHandler opSha256() {
        return (stack, operand) -> {
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_SHA256);
            }
            byte[] data = stack.pop();
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                stack.push(sha256.digest(data));
            } catch (NoSuchAlgorithmException e) {
                throw new ScriptExecutionException(OpcodeType.OP_SHA256,
                        "SHA-256 no disponible.");
            }
        };
    }

    /**
     * OP_HASH256 (0xaa) Aplica SHA-256 dos veces: SHA256(SHA256(data)). Produce
     * 32 bytes. Se usa en Bitcoin para calcular TXIDs.
     *
     * Antes: [ data | ... ] Después: [ hash256(data) | ... ]
     */
    public static OpcodeHandler opHash256() {
        return (stack, operand) -> {
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_HASH256);
            }
            byte[] data = stack.pop();
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] firstPass = sha256.digest(data);
                byte[] secondPass = sha256.digest(firstPass);
                stack.push(secondPass);
            } catch (NoSuchAlgorithmException e) {
                throw new ScriptExecutionException(OpcodeType.OP_HASH256,
                        "SHA-256 no disponible.");
            }
        };
    }

    /**
     * OP_CHECKSIGVERIFY (0xad) Igual que OP_CHECKSIG (mock) pero además
     * verifica el resultado. Si la firma es inválida, lanza
     * ScriptExecutionException. No deja nada en la pila si tiene éxito.
     *
     * Antes: [ firma | pubKey | ... ] Después: [ ... ] (si firma válida) FALLO
     * (si firma inválida)
     */
    public static OpcodeHandler opCheckSigVerifyMock() {
        return (stack, operand) -> {
            if (stack.size() < 2) {
                throw new EmptyStackException(OpcodeType.OP_CHECKSIGVERIFY);
            }
            byte[] signature = stack.pop();
            byte[] publicKey = stack.pop();
            boolean valid = signature.length > 0 && publicKey.length > 0;
            if (!valid) {
                throw new ScriptExecutionException(
                        OpcodeType.OP_CHECKSIGVERIFY,
                        "Verificación de firma fallida (mock): firma o clave vacía.");
            }
            // No empuja nada a la pila si tiene éxito
        };
    }

    /**
     * OP_CHECKMULTISIG (mock) (0xae)
     *
     * Implementación mock de verificación de firma múltiple M-de-N. Protocolo
     * de la pila (de cima a fondo): [ 0 | firma1 | ... | firmaM | M | pubKey1 |
     * ... | pubKeyN | N ]
     *
     * La simulación aprueba si: - M firmas requeridas <= N claves disponibles -
     * Todas las M firmas son no-vacías - Al menos M claves son no-vacías
     *
     * NOTA: Bitcoin tiene un bug histórico que requiere un OP_0 extra al inicio
     * del scriptSig; esta implementación lo consume.
     *
     * Antes: [ OP_0 | firma_1..firma_M | M | pubKey_1..pubKey_N | N | ... ]
     * Después: [ TRUE | ... ] (si mock válido) [ FALSE | ... ] (si mock
     * inválido)
     */
    public static OpcodeHandler opCheckMultiSigMock() {
        return (stack, operand) -> {
            // Consumir el OP_0 extra (bug histórico de Bitcoin)
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_CHECKMULTISIG);
            }
            stack.pop();

            // Leer M (cantidad de firmas requeridas)
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_CHECKMULTISIG);
            }
            int m = new edu.uvg.model.ScriptElement(stack.pop()).toInt();
            if (m < 0 || m > 20) {
                throw new ScriptExecutionException(OpcodeType.OP_CHECKMULTISIG,
                        "OP_CHECKMULTISIG: M inválido: " + m);
            }

            // Leer M firmas
            if (stack.size() < m) {
                throw new EmptyStackException(OpcodeType.OP_CHECKMULTISIG);
            }
            byte[][] signatures = new byte[m][];
            for (int i = 0; i < m; i++) {
                signatures[i] = stack.pop();
            }

            // Leer N (cantidad de claves públicas)
            if (stack.isEmpty()) {
                throw new EmptyStackException(OpcodeType.OP_CHECKMULTISIG);
            }
            int n = new edu.uvg.model.ScriptElement(stack.pop()).toInt();
            if (n < 0 || n > 20) {
                throw new ScriptExecutionException(OpcodeType.OP_CHECKMULTISIG,
                        "OP_CHECKMULTISIG: N inválido: " + n);
            }
            if (m > n) {
                throw new ScriptExecutionException(OpcodeType.OP_CHECKMULTISIG,
                        "OP_CHECKMULTISIG: M mayor que N (M=" + m + ", N=" + n + ")");
            }

            // Leer N claves públicas
            if (stack.size() < n) {
                throw new EmptyStackException(OpcodeType.OP_CHECKMULTISIG);
            }
            byte[][] pubKeys = new byte[n][];
            for (int i = 0; i < n; i++) {
                pubKeys[i] = stack.pop();
            }

            // Validación mock: todas las firmas son no-vacías y al menos M claves no-vacías
            int validSignatures = 0;

            for (byte[] sig : signatures) {
                if (sig.length == 0) continue;

                    for (byte[] key : pubKeys) {
                        if (key.length == 0) continue;

                        // Mock: si ambos no están vacíos, cuenta como válida
                        validSignatures++;
                        break; // una firma se usa una vez
    }
}

boolean valid = validSignatures >= m;

            stack.push(valid ? new byte[]{1} : new byte[0]);
        };
    }
}
