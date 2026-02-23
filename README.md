# Bitcoin Script Interpreter

Intérprete de **Bitcoin Script** desarrollado en Java como proyecto académico para el curso de Estructuras de Datos — Universidad del Valle de Guatemala (UVG).

## Descripción

Bitcoin Script es el lenguaje de scripting basado en pila (*stack-based*) que utiliza la red Bitcoin para definir las condiciones bajo las cuales un output puede ser gastado. Este proyecto implementa un intérprete funcional capaz de parsear y ejecutar scripts reales, incluyendo el patrón de transacción **P2PKH** (*Pay-to-Public-Key-Hash*).

## Autores

| Nombre | Contribución principal |
|---|---|
| Franco | ScriptInterpreter, StackOpcodes, ControlOpcodes, OpcodeRegistry |
| Weslly Cabrera | EvaluationStack, ScriptElement, OpcodeType, OpcodeHandler |
| James Sipac | CryptoComparisonOpcodes (OP_EQUAL, OP_HASH160, OP_CHECKSIG) |

## Características

- Parseo de scripts desde listas de strings (`OP_XXX`, datos hex, datos mock `<...>`)
- Evaluación basada en pila con `ArrayDeque<byte[]>`
- Soporte de bloques condicionales anidados (`OP_IF` / `OP_NOTIF` / `OP_ELSE` / `OP_ENDIF`)
- Criptografía real: `SHA-256` + `RIPEMD-160` via **BouncyCastle**
- Modo traza (`--trace`) que imprime el estado de la pila tras cada instrucción
- Tabla de despacho extensible mediante el patrón **Command + Registry**
- Suite completa de tests unitarios e integración con **JUnit 5**

## Requisitos

- Java 17 o superior
- Gradle 8.x (incluido el wrapper `gradlew`)
- Conexión a internet para la primera descarga de dependencias

## Instalación y ejecución

### Compilar el proyecto

```bash
./gradlew build
```

### Ejecutar la demo principal

```bash
./gradlew run
```

O bien directamente con Java:

```bash
java -cp build/classes/java/main:build/libs/* edu.uvg.Main
```

### Ejecutar con modo traza (--trace)

```bash
./gradlew run --args="--trace"
```

El modo traza imprime el estado de la pila después de cada instrucción ejecutada:

```
[TRACE] OP_DUP                    → [ [3c 70 75 62 4b 65 79 3e] [3c 70 75 62 4b 65 79 3e] [3c 66 69 72 6d 61 3e] ]
[TRACE] OP_HASH160                 → [ [a1 b2 c3 ...] [3c 70 75 62 4b 65 79 3e] [3c 66 69 72 6d 61 3e] ]
```

### Ejecutar los tests

```bash
./gradlew test
```

## Estructura del proyecto

```
bitcoin-script/
├── src/
│   ├── main/java/edu/uvg/
│   │   ├── Main.java                        # Punto de entrada — demo con 5 pruebas
│   │   ├── exceptions/
│   │   │   ├── EmptyStackException.java     # Pila vacía al ejecutar un opcode
│   │   │   ├── InvalidOperandException.java # Operando inválido
│   │   │   └── ScriptExecutionException.java# Error general de ejecución
│   │   ├── interfaces/
│   │   │   ├── OpcodeHandler.java           # Interfaz funcional para cada opcode
│   │   │   └── ScriptException.java         # Interfaz base de excepciones
│   │   ├── interpreter/
│   │   │   ├── OpcodeRegistry.java          # Tabla de despacho (Command + Registry)
│   │   │   ├── ScriptInterpreter.java       # Motor principal de ejecución
│   │   │   └── ScriptParser.java            # Convierte strings en ScriptTokens
│   │   ├── model/
│   │   │   ├── OpcodeType.java              # Enum de todos los opcodes (con código hex)
│   │   │   ├── ScriptElement.java           # Elemento de la pila (byte[] con semántica)
│   │   │   └── ScriptToken.java             # Token parseado (dato u opcode)
│   │   ├── opcodes/
│   │   │   ├── ControlOpcodes.java          # OP_VERIFY, OP_RETURN
│   │   │   ├── CryptoComparisonOpcodes.java # OP_EQUAL, OP_EQUALVERIFY, OP_HASH160, OP_CHECKSIG
│   │   │   ├── DataOpcodes.java             # OP_0, OP_1..OP_16, OP_TRUE/FALSE, PUSHDATA
│   │   │   └── StackOpcodes.java            # OP_DUP, OP_DROP, OP_SWAP, OP_OVER
│   │   └── stack/
│   │       └── EvaluationStack.java         # Pila de evaluación (ArrayDeque<byte[]>)
│   └── test/java/edu/uvg/
│       ├── BaseTest.java
│       ├── interpreter/
│       │   ├── ScriptInterpreterTest.java   # Tests de integración del intérprete
│       │   └── ScriptParserTest.java        # Tests del parser
│       ├── model/
│       │   ├── OpcodeTypeTest.java
│       │   └── ScriptElementTest.java
│       ├── opcodes/
│       │   ├── ControlOpcodesTest.java
│       │   ├── CryptoComparisonOpcodesTest.java
│       │   ├── DataOpcodesTest.java
│       │   └── StackOpcodesTest.java
│       └── stack/
│           └── EvaluationStackTest.java
├── build.gradle
├── settings.gradle
└── gradlew / gradlew.bat
```

## Opcodes implementados

### Datos y literales

| Opcode | Hex | Descripción |
|--------|-----|-------------|
| `OP_0` / `OP_FALSE` | `0x00` | Empuja FALSE (array vacío) |
| `OP_1` / `OP_TRUE` | `0x51` | Empuja TRUE (`0x01`) |
| `OP_2` .. `OP_16` | `0x52`–`0x60` | Empuja el entero correspondiente |
| `PUSHDATA1` | `0x4c` | Empuja datos de hasta 255 bytes |
| `PUSHDATA2` | `0x4d` | Empuja datos de hasta 65535 bytes |

### Manipulación de pila

| Opcode | Hex | Descripción |
|--------|-----|-------------|
| `OP_DUP` | `0x76` | Duplica el elemento en la cima |
| `OP_DROP` | `0x75` | Descarta el elemento en la cima |
| `OP_SWAP` | `0x7c` | Intercambia los dos elementos superiores |
| `OP_OVER` | `0x7b` | Copia el segundo elemento al tope |

### Control de flujo

| Opcode | Hex | Descripción |
|--------|-----|-------------|
| `OP_IF` | `0x63` | Ejecuta el bloque si la cima es TRUE |
| `OP_NOTIF` | `0x64` | Ejecuta el bloque si la cima es FALSE |
| `OP_ELSE` | `0x67` | Bloque alternativo de OP_IF / OP_NOTIF |
| `OP_ENDIF` | `0x68` | Cierra el bloque condicional |
| `OP_VERIFY` | `0x69` | Falla si la cima es FALSE; la descarta si es TRUE |
| `OP_RETURN` | `0x6a` | Invalida el script inmediatamente |

### Comparación y criptografía

| Opcode | Hex | Descripción |
|--------|-----|-------------|
| `OP_EQUAL` | `0x87` | Empuja 1 si los dos elementos superiores son iguales |
| `OP_EQUALVERIFY` | `0x88` | Como OP_EQUAL pero falla si no son iguales |
| `OP_HASH160` | `0xa9` | RIPEMD-160(SHA-256(dato)) — hash real via BouncyCastle |
| `OP_CHECKSIG` | `0xac` | Verifica firma (simulada: válida si firma y clave no están vacías) |

## Arquitectura

```
ScriptParser
    └── List<ScriptToken>
            │
            ▼
    ScriptInterpreter
    ├── EvaluationStack  (ArrayDeque<byte[]>)
    └── OpcodeRegistry   (EnumMap<OpcodeType, OpcodeHandler>)
            ├── DataOpcodes
            ├── StackOpcodes
            ├── ControlOpcodes
            └── CryptoComparisonOpcodes
```

**Patrón de diseño principal:** *Command + Registry*
- Cada `OpcodeHandler` es una función (interfaz funcional) que opera sobre la pila.
- El `OpcodeRegistry` actúa como tabla de despacho: el intérprete consulta el handler correspondiente al opcode sin necesidad de un `switch` centralizado.
- Para agregar un nuevo opcode basta con: (1) añadirlo a `OpcodeType`, (2) implementar su `OpcodeHandler`, (3) registrarlo en `OpcodeRegistry`.

## Semántica de la pila

Los elementos de la pila son arrays de bytes (`byte[]`) que se interpretan según el contexto:

| Valor | Semántica |
|-------|-----------|
| Array vacío `[]` | **FALSE** |
| Array con todos los bytes en `0x00` | **FALSE** |
| Cualquier otro array | **TRUE** |
| Enteros | Little-endian, con bit de signo en el byte más significativo |

Un script es **válido** si, al terminar su ejecución, la pila no está vacía y el elemento en la cima es **TRUE**.

## Demo incluida

La clase `Main` ejecuta 5 pruebas automáticas al correr el proyecto:

| Prueba | Descripción | Resultado esperado |
|--------|-------------|-------------------|
| 1 | P2PKH con hash correcto | VÁLIDA |
| 2 | P2PKH con hash incorrecto | INVÁLIDA |
| 3 | `OP_1` | VÁLIDA |
| 4 | `OP_0` | INVÁLIDA |
| 5 | `01 OP_DUP OP_EQUAL` | VÁLIDA |

### Ejemplo de flujo P2PKH

```
scriptSig:    <firma> <pubKey>
scriptPubKey: OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
```

| Paso | Instrucción | Estado de la pila |
|------|------------|-------------------|
| 1 | `<firma>` | `[firma]` |
| 2 | `<pubKey>` | `[pubKey, firma]` |
| 3 | `OP_DUP` | `[pubKey, pubKey, firma]` |
| 4 | `OP_HASH160` | `[hash(pubKey), pubKey, firma]` |
| 5 | `<pubKeyHash>` | `[pubKeyHash, hash(pubKey), pubKey, firma]` |
| 6 | `OP_EQUALVERIFY` | `[pubKey, firma]` *(falla si no coinciden)* |
| 7 | `OP_CHECKSIG` | `[TRUE]` |

## Dependencias

```groovy
// JUnit 5
testImplementation platform('org.junit:junit-bom:5.10.0')
testImplementation 'org.junit.jupiter:junit-jupiter'

// BouncyCastle — necesario para RIPEMD-160 en OP_HASH160
implementation 'org.bouncycastle:bcprov-jdk18on:1.78'
```

> **Nota:** BouncyCastle se registra como proveedor de seguridad en tiempo de ejecución (`Security.addProvider(new BouncyCastleProvider())`). Es necesario para que `MessageDigest.getInstance("RIPEMD160")` funcione correctamente en cualquier JVM.

## Contexto académico

Este proyecto corresponde al **Proyecto #1** del curso de Estructuras de Datos, Tercer Semestre — Universidad del Valle de Guatemala. Su objetivo es aplicar el uso de estructuras de datos (específicamente pilas) en un contexto real, modelando el motor de scripting de la red Bitcoin.
