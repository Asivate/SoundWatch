import eventlet
eventlet.monkey_patch()

from flask import Flask, render_template, request
from flask_socketio import SocketIO, emit
import logging
import sys
import json

# Set up detailed logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

PORT = 3000

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'

# Initialize SocketIO with more detailed configuration
socketio = SocketIO(
    app, 
    cors_allowed_origins="*",
    async_mode='eventlet',
    logger=True,
    engineio_logger=True
)

@socketio.on('connect')
def handle_connect():
    with app.app_context():
        sid = request.sid
        transport = request.environ.get('wsgi.url_scheme', 'unknown')
        logger.info(f'Client connected - SID: {sid}, Transport: {transport}, IP: {request.remote_addr}')
        emit('connection_response', {'data': 'Connected successfully!'})
        logger.info(f'Sent connection response to client {sid}')

@socketio.on('start_listening')
def handle_start_listening():
    with app.app_context():
        sid = request.sid
        logger.info(f'Start listening request received from client {sid}')
        try:
            test_data = {
                'label': 'Test Sound',
                'accuracy': '1.0'
            }
            logger.info(f'Emitting test audio label to client {sid}: {json.dumps(test_data)}')
            emit('audio_label', test_data)
            logger.info(f'Audio label emitted successfully to client {sid}')
        except Exception as e:
            logger.error(f'Error handling start_listening for client {sid}: {str(e)}')
            emit('error', {'message': f'Error processing request: {str(e)}'})

@socketio.on('disconnect')
def handle_disconnect():
    with app.app_context():
        sid = request.sid
        logger.info(f'Client disconnected - SID: {sid}')

@socketio.on_error()
def error_handler(e):
    sid = request.sid if hasattr(request, 'sid') else 'Unknown'
    logger.error(f'SocketIO error for client {sid}: {str(e)}')
    emit('error', {'message': f'Server error: {str(e)}'})

if __name__ == '__main__':
    try:
        logger.info(f'Starting server on http://0.0.0.0:{PORT}')
        logger.info('Server configured with:')
        logger.info(f' - CORS: Allowed all origins')
        logger.info(f' - Transport: WebSocket and Polling')
        logger.info(f' - Async Mode: eventlet')
        
        socketio.run(
            app, 
            host='0.0.0.0', 
            port=PORT,
            debug=False,
            use_reloader=False,
            log_output=True
        )
    except Exception as e:
        logger.error(f'Failed to start server: {e}')
        sys.exit(1)
