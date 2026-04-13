const listeners = {};
export const EventBus = {
    on(event, handler) {
        (listeners[event] || (listeners[event] = [])).push(handler);
    },
    emit(event, payload) {
        (listeners[event] || []).forEach(fn => fn(payload));
    }
};
