# TicTacToe WebSocket — Backend Java + Frontend React

soporte multijugador en tiempo real mediante WebSockets y persistencia en MongoDB. salas, unión a salas, juego entre dos jugadores, persistencia de historial de movimientos y capacidad de "retroceder" (undo) usando la historia almacenada.

En este README describo qué hice, cómo está organizado el proyecto, cómo ejecutarlo localmente (con MongoDB Atlas o local), el protocolo WebSocket, los endpoints REST disponibles, limitaciones y mejoras sugeridas.

Resumen de lo realizado
- Construí un backend Java Spring Boot que expone un endpoint WebSocket estilo `@ServerEndpoint("/gameService")`.
- Implementé persistencia en MongoDB usando Spring Data MongoDB; las salas (Room) contienen la lista de movimientos (Move) embebida.
- Implementé servicios (`GameService`) para crear salas, unirse, jugar movimientos y hacer undo, con la lógica básica de validación (turno, celda libre).
- Adapté un frontend React simple embebido en `src/main/resources/static` (sin build con npm): `index.html` + `js/app.jsx`. 
- Déjé REST endpoints mínimos para listar/obtener salas (`/api/rooms`) y la UI sirve como cliente WebSocket para jugar en tiempo real.
- Incluí instrucciones de configuración para conectar a MongoDB Atlas usando variables de entorno: `DB_URI` y `DB_NAME`.


Estructura principal del proyecto
- src/
  - main/
    - java/com/example/tictactoe/
      - config/  -> configuradores Spring (ServerEndpointExporter, SpringContext helper)
      - model/   -> Room.java, Move.java
      - repo/    -> RoomRepository (MongoRepository)
      - service/ -> GameService.java (lógica de salas / movimientos / undo)
      - endpoint/      -> GameEndpoint.java 
      - controller/ -> RoomController.java (REST para listar/obtener)
      - TictactoeApplication.java (arranque Spring Boot)
    - resources/
      - static/
        - index.html          -> UI React embebida (sirve en /)
        - js/app.jsx          -> aplicación React usando WebSocket nativo
      - application.properties -> configuración (URI Mongo, puerto, etc.)
- pom.xml

- Opcional: exporta las variables de entorno en tu máquina antes de ejecutar en un archivo .env:
```bash
export DB_URI='mongodb+srv://barquita:barquita_decrypted_123@cluster0.2lsyy.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0'
export DB_NAME='Parcial2'
# en Windows PowerShell:
# $Env:DB_URI = '...'
# $Env:DB_NAME = 'Parcial2'
```

Cómo ejecutar (local)
1. Construir:
```bash
mvn -U clean package
```
2. Ejecutar:
```bash
mvn spring-boot:run
```
3. Abrir navegador:
```
http://localhost:8080/
```

Protocolo WebSocket (JSON)
- Endpoint: `ws://localhost:8080/gameService` (o `wss://` si TLS)
- Mensajes cliente -> servidor:
  - Crear sala:
    ```json
    { "type": "create", "room": "nombreSalaOpcional", "player": "X" }
    ```
    Si "room" está vacio se genera `room-<uuid>`.
  - Unirse a sala:
    ```json
    { "type": "join", "room": "nombreSala", "player": "O" }
    ```
  - Movimientos:
    ```json
    { "type": "move", "room": "nombreSala", "player": "X", "index": 4 }
    ```
    `index` es 0..8 (posiciones del tablero en orden fila-major).
  - Undo (retroceder último movimiento en la sala):
    ```json
    { "type": "undo", "room": "nombreSala" }
    ```
- Mensajes servidor -> cliente:
  - Sala creada / unido / update:
    ```json
    {
      "type":"created" | "joined" | "update",
      "room": {
         "id": "...",
         "name": "...",
         "board": "X O  ...", 
         "playerX": "...",
         "playerO": "...",
         "nextTurn": "X",
         "moves": [ { "index": 4, "player": "X", "seq": 0, "playedAt": "..." }, ... ]
      }
    }
    ```
  - Error:
    ```json
    { "type": "error", "message": "descripcion" }
    ```

Pruebas rápidas (manual)
- Abrir dos pestañas del navegador en `http://localhost:8080/`
- En la primera crear sala (o dejar nombre auto-generado), símbolo `X`
- En la segunda unirse con el mismo nombre, símbolo `O`
- Alternar movimientos. Verás que ambos clientes reciben updates en tiempo real.
- Presionar `Undo` para retroceder el último movimiento.


```