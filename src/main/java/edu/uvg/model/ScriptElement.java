package edu.uvg.model;

import java.util.Arrays;

/**
 * Representa un elemento de la pila de evaluación de Bitcoin Script.
 * Internamente es un byte[] que puede interpretarse como booleano,
 * entero o dato según el contexto del opcode.
 *
 * Convenciones de Bitcoin Script adoptadas:
 *   - FALSE : array vacío new byte[0]
 *   - TRUE  : cualquier array no vacío con al menos un byte != 0
 *   - Enteros: little-endian con bit de signo en el byte más significativo
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class ScriptElement {

    public static final ScriptElement FALSE = new ScriptElement(new byte[0]);
    public static final ScriptElement TRUE  = new ScriptElement(new byte[]{0x01});

    private final byte[] data;

    /**
     * @param data contenido en bytes del elemento; no puede ser null
     * @throws IllegalArgumentException si data es null
     */
    public ScriptElement(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("El contenido de un ScriptElement no puede ser null.");
        }

        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * @param value entero a codificar
     * @return ScriptElement que representa ese entero
     */
    public static ScriptElement fromInt(int value) {
        if (value == 0) return FALSE;

        boolean negative = value < 0;
        int absValue = Math.abs(value);

        byte[] result = new byte[5];
        int length = 0;

        while (absValue > 0) {
            result[length++] = (byte) (absValue & 0xFF);
            absValue >>= 8;
        }

        if ((result[length - 1] & 0x80) != 0) {
            result[length++] = negative ? (byte) 0x80 : 0x00;
        } else if (negative) {
            result[length - 1] |= 0x80;
        }

        return new ScriptElement(Arrays.copyOf(result, length));
    }

    /**
     * Interpreta el elemento como booleano según las reglas de Bitcoin Script.
     * Es false si el array está vacío o contiene solo ceros.
     *
     * @return true si el elemento representa un valor verdadero
     */
    public boolean toBoolean() {
        if (data.length == 0) return false;
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] != 0x00) return true;
        }
        // último byte: 0x00 o 0x80 (cero negativo) son ambos falsos
        byte last = data[data.length - 1];
        return last != 0x00 && last != (byte) 0x80;
    }

    /**
     * @return valor entero representado
     * @throws ArithmeticException si el elemento tiene más de 4 bytes
     */
    public int toInt() {
        if (data.length == 0) return 0;
        if (data.length > 4) {
            throw new ArithmeticException("El elemento supera el tamaño máximo para enteros (4 bytes).");
        }

        int result = 0;
        for (int i = 0; i < data.length; i++) {
            result |= (data[i] & 0xFF) << (8 * i);
        }

        if ((data[data.length - 1] & 0x80) != 0) {
            result &= ~(0x80 << (8 * (data.length - 1)));
            result = -result;
        }

        return result;
    }

    /**
     * Retorna una copia defensiva del contenido en bytes.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * @return cantidad de bytes del elemento
     */
    public int size() {
        return data.length;
    }

    /**
     * Crea una copia independiente de este elemento.
     * Usado por OP_DUP para no compartir referencias.
     */
    public ScriptElement copy() {
        return new ScriptElement(this.data);
    }

    /**
     * Compara el contenido en bytes de dos elementos.
     * Usado por OP_EQUAL y OP_EQUALVERIFY.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScriptElement)) return false;
        return Arrays.equals(this.data, ((ScriptElement) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Representación hexadecimal del contenido.
     */
    @Override
    public String toString() {
        if (data.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (byte b : data) {
            sb.append(String.format("%02x", b)).append(" ");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }
}
