// frameMessenger.js
const listeners = new Map();
function send(targetWindow, message, targetOrigin = '*') {
    targetWindow.postMessage({ __frameMessenger: true, ...message }, targetOrigin);
}
function on(type, callback) {
    if (!listeners.has(type)) {
        listeners.set(type, []);
    }
    listeners.get(type).push(callback);
}
function off(type, callback) {
    const callbacks = listeners.get(type);
    if (!callbacks)
        return;
    const index = callbacks.indexOf(callback);
    if (index >= 0) {
        callbacks.splice(index, 1);
    }
}
function handleMessage(event) {
    const data = event.data;
    if (!data || !data.__frameMessenger || !data.type)
        return;
    const callbacks = listeners.get(data.type) || [];
    for (const cb of callbacks) {
        try {
            cb(data.payload, event);
        }
        catch (err) {
            console.error('frameMessenger callback error:', err);
        }
    }
}
window.addEventListener('message', handleMessage);
export default {
    send,
    on,
    off
};
