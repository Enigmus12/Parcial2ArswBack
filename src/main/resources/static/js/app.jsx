const WS_URL = (location.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + location.host + '/gameService';

function Square({ value, onClick }) {
  return <button className="square" onClick={onClick}>{value}</button>;
}

function Board({ board, onClick }) {
  const squares = board ? board.split('') : Array(9).fill(' ');
  function renderSquare(i) {
    return <Square key={i} value={squares[i]} onClick={() => onClick(i)} />;
  }
  return (
    <div>
      <div className="board-row">{renderSquare(0)}{renderSquare(1)}{renderSquare(2)}</div>
      <div className="board-row">{renderSquare(3)}{renderSquare(4)}{renderSquare(5)}</div>
      <div className="board-row">{renderSquare(6)}{renderSquare(7)}{renderSquare(8)}</div>
    </div>
  );
}

function App() {
  const [ws, setWs] = React.useState(null);
  const [connected, setConnected] = React.useState(false);
  const [room, setRoom] = React.useState(null);
  const [roomName, setRoomName] = React.useState('');
  const [player, setPlayer] = React.useState('X');
  const [status, setStatus] = React.useState('Desconectado');

  React.useEffect(() => {
    const socket = new WebSocket(WS_URL);
    socket.onopen = () => { setConnected(true); setStatus('WS conectado'); console.log('WS open'); };
    socket.onmessage = (ev) => {
      if (ev.data === 'Connection established.') return;
      try {
        const msg = JSON.parse(ev.data);
        handleMsg(msg);
      } catch (e) {
        console.error('invalid msg', ev.data);
      }
    };
    socket.onclose = () => { setConnected(false); setStatus('WS cerrado'); };
    socket.onerror = (e) => { console.error('ws err', e); };
    setWs(socket);
    return () => { try { socket.close(); } catch(e){} };
  }, []);

  function handleMsg(msg) {
    if (!msg.type) return;
    if (msg.type === 'created' || msg.type === 'joined' || msg.type === 'update') {
      setRoom(msg.room);
      setStatus(`Sala: ${msg.room.name} - Turno: ${msg.room.nextTurn}`);
    } else if (msg.type === 'error') {
      setStatus('Error: ' + msg.message);
    }
  }

  function send(obj) {
    if (!ws || ws.readyState !== WebSocket.OPEN) { setStatus('WS no abierto'); return; }
    ws.send(JSON.stringify(obj));
  }

  function createRoom() {
    send({ type:'create', room: roomName, player });
  }

  function joinRoom() {
    if (!roomName) { setStatus('Ingrese nombre de sala'); return; }
    send({ type:'join', room: roomName, player });
  }

  function handleClick(i) {
    if (!room) { setStatus('No en sala'); return; }
    send({ type:'move', room: room.name, index: i, player });
  }

  function undo() {
    if (!room) { setStatus('No en sala'); return; }
    send({ type:'undo', room: room.name });
  }

  function winner(board) {
    if (!board) return null;
    const s = board.split('');
    const lines = [[0,1,2],[3,4,5],[6,7,8],[0,3,6],[1,4,7],[2,5,8],[0,4,8],[2,4,6]];
    for (let l of lines) {
      const [a,b,c] = l;
      if (s[a] && s[a] !== ' ' && s[a] === s[b] && s[a] === s[c]) return s[a];
    }
    return null;
  }

  return (
    <div>
      <h3>TicTacToe </h3>
      <div>
        <label>Nombre sala: </label>
        <input value={roomName} onChange={e => setRoomName(e.target.value)} />
        <label style={{marginLeft:10}}>Tu símbolo: </label>
        <select value={player} onChange={e => setPlayer(e.target.value)}>
          <option value="X">X</option>
          <option value="O">O</option>
        </select>
        <button style={{marginLeft:10}} onClick={createRoom}>Crear sala</button>
        <button style={{marginLeft:10}} onClick={joinRoom}>Unirse</button>
        <button style={{marginLeft:10}} onClick={undo}>Undo</button>
      </div>

      <div style={{marginTop:10}}>
        <strong>Estado:</strong> {status} &nbsp; <small>WS: {connected ? 'OK' : 'NO'}</small>
      </div>

      <div style={{marginTop:20}}>
        {room ? <Board board={room.board} onClick={handleClick} /> : <div>Crear o unirse a una sala</div>}
      </div>

      <div style={{marginTop:10}}>
        {room ? (winner(room.board) ? <div>Ganador: {winner(room.board)}</div> : <div>Turno: {room.nextTurn}</div>) : null}
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);