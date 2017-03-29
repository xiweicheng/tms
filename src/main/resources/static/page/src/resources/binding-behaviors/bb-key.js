const keyCodes = {
    'esc': 27,
    'tab': 9,
    'enter': 13,
    'space': 32,
    'up': 38,
    'left': 37,
    'right': 39,
    'down': 40,
    'backspace': 8,
    'delete': 46
}

function keyHandler(event) {

    if (event && event.ctrlKey == this.keyState.ctrl && event.altKey == this.keyState.alt && event.shiftKey == this.keyState.shift && event.keyCode == this.keyState.keyCode) {
        this.originalMethod(event);
    } else {
        // console.warn(`Unknown event [${event}] for KeyBindingBehavior!`);
    }

    return true;
}

export class KeyBindingBehavior {

    bind(binding, source, key = 13, metaKeys) {
        // determine which method to throttle.
        let methodName = 'updateTarget'; // one-way bindings or interpolation bindings
        if (binding.callSource) {
            methodName = 'callSource'; // listener and call bindings
        } else if (binding.updateSource && binding.mode === bindingMode.twoWay) {
            methodName = 'updateSource'; // two-way bindings
        }

        // stash the original method and it's name.
        // note: a generic name like "originalMethod" is not used to avoid collisions
        // with other binding behavior types.
        binding.originalMethod = binding[methodName];
        binding.originalMethod.originalName = methodName;

        // replace the original method with the throttling version.
        binding[methodName] = keyHandler;
        let keyCode = _.isInteger(key) ? key : (key.length === 1 ? key.charCodeAt(0) : keyCodes[key]);
        if (_.isUndefined(keyCode)) {
            console.warn(`Unmapping keyCode for KeyBindingBehavior!`);
        }
        binding.keyState = {
            ctrl: _.includes(metaKeys, 'ctrl'),
            alt: _.includes(metaKeys, 'alt'),
            shift: _.includes(metaKeys, 'shift'),
            keyCode: keyCode,
        };
    }

    unbind(binding, source) {
        // restore the state of the binding.
        binding[binding.originalMethod.originalName] = binding.originalMethod;
        binding.originalMethod = null;
    }
}
