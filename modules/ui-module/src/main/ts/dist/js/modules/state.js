class State {
    constructor(initialState = {}) {
        this.state = initialState;
        this.observers = [];
        return new Proxy(this, {
            get: (target, prop) => {
                if (prop in target.state) {
                    return target.state[prop];
                }
                return target[prop];
            },
            set: (target, prop, value) => {
                if (prop in target.state) {
                    if (target.state[prop] !== value) {
                        target.state[prop] = value;
                        this.observers.forEach(({ observer, dependencies }) => {
                            if (dependencies.has(prop)) {
                                observer(this.state);
                            }
                        });
                    }
                }
                else {
                    target[prop] = value;
                }
            },
        });
    }
    observe(observer) {
        const dependencies = new Set();
        const proxy = new Proxy(this.state, {
            get: (target, prop) => {
                dependencies.add(prop);
                return target[prop];
            },
        });
        observer(proxy);
        this.observers.push({ observer, dependencies });
    }
    unobserve(observer) {
        const index = this.observers.findIndex(entry => entry.observer === observer);
        if (index !== -1) {
            this.observers.splice(index, 1);
        }
    }
}
