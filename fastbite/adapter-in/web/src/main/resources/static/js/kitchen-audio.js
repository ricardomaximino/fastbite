/**
 * Kitchen Audio Chime Helper
 * Synthesizes chime alerts via the Web Audio API without requiring external audio assets.
 */
class KitchenChime {
    constructor() {
        this.storageKey = 'fastbite-kitchen-chime-enabled';
        this.audioCtx = null;
        this.enabled = localStorage.getItem(this.storageKey) === 'true';
    }

    init() {
        this.updateUi();
    }

    getAudioContext() {
        if (!this.audioCtx) {
            const AudioContextClass = window.AudioContext || window.webkitAudioContext;
            if (AudioContextClass) {
                this.audioCtx = new AudioContextClass();
            }
        }
        if (this.audioCtx && this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }
        return this.audioCtx;
    }

    toggle() {
        this.enabled = !this.enabled;
        localStorage.setItem(this.storageKey, this.enabled);
        const ctx = this.getAudioContext();
        this.updateUi();
        if (this.enabled) {
            this.play();
        }
        return this.enabled;
    }

    updateUi() {
        const icon = document.getElementById('kitchenAudioIcon');
        const text = document.getElementById('kitchenAudioText');
        const btn = document.getElementById('kitchenAudioToggleBtn');
        if (!btn) return;

        if (this.enabled) {
            if (icon) icon.className = 'fas fa-bell me-1 text-warning';
            if (text) text.textContent = 'Chime On';
            btn.classList.add('btn-warning-subtle');
        } else {
            if (icon) icon.className = 'fas fa-bell-slash me-1';
            if (text) text.textContent = 'Chime Off';
            btn.classList.remove('btn-warning-subtle');
        }
    }

    play() {
        if (!this.enabled) return;
        try {
            const ctx = this.getAudioContext();
            if (!ctx) return;

            const now = ctx.currentTime;
            
            // Note 1: D5 (587.33 Hz)
            const osc1 = ctx.createOscillator();
            const gain1 = ctx.createGain();
            osc1.type = 'sine';
            osc1.frequency.setValueAtTime(587.33, now);
            gain1.gain.setValueAtTime(0.3, now);
            gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.35);
            osc1.connect(gain1);
            gain1.connect(ctx.destination);
            osc1.start(now);
            osc1.stop(now + 0.35);

            // Note 2: A5 (880 Hz) - pleasant two-tone ding-dong chime
            const osc2 = ctx.createOscillator();
            const gain2 = ctx.createGain();
            osc2.type = 'sine';
            osc2.frequency.setValueAtTime(880, now + 0.15);
            gain2.gain.setValueAtTime(0.4, now + 0.15);
            gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.65);
            osc2.connect(gain2);
            gain2.connect(ctx.destination);
            osc2.start(now + 0.15);
            osc2.stop(now + 0.65);
        } catch (e) {
            console.warn('Unable to play kitchen chime', e);
        }
    }
}

window.kitchenChime = new KitchenChime();

function toggleKitchenAudio() {
    if (window.kitchenChime) {
        window.kitchenChime.toggle();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (window.kitchenChime) {
        window.kitchenChime.init();
    }
});
