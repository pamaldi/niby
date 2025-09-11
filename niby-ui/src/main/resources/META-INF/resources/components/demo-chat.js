import { css, html, LitElement } from 'https://unpkg.com/lit@3?module';
import { unsafeHTML } from 'https://unpkg.com/lit@3/directives/unsafe-html.js?module';
import { marked } from 'https://cdn.jsdelivr.net/npm/marked/lib/marked.esm.js';
import DOMPurify from 'https://cdn.jsdelivr.net/npm/dompurify@3.1.6/dist/purify.es.mjs';

export class DemoChat extends LitElement {
    // ---------- Styles ----------
    static styles = css`
    :host {
      display: flex;
      flex-direction: column;
      height: 100%;
      width: 100%;
      overflow: hidden;
      font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji", "Segoe UI Emoji";
    }
    .chat-header {
      background: #ffc107;
      color: #212529;
      padding: 1rem;
      font-weight: bold;
      border-radius: 8px 8px 0 0;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .chat-header img {
      width: 30px;
      height: 30px;
    }
    .status {
      display: flex;
      align-items: center;
      gap: .5rem;
      font-size: .85rem;
      color: #495057;
      padding: .5rem 1rem;
      background: #fff8e1;
      border-bottom: 1px solid #ffe082;
    }
    .dot {
      width: 8px; height: 8px; border-radius: 50%;
      background: var(--dot, #28a745);
    }
    .status[data-state="open"] { --dot: #28a745; }
    .status[data-state="connecting"] { --dot: #ffc107; }
    .status[data-state="closed"] { --dot: #dc3545; }

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 1rem;
      background: #e3f2fd;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .message-bubble {
      padding: 10px 15px;
      border-radius: 20px;
      max-width: 80%;
      display: flex;
      align-items: center;
      gap: 10px;
      word-break: break-word;
      background-color: #fff;
      color: #333;
      border: 1px solid #ddd;
    }
    .message-bubble.user { align-self: flex-end; }
    .message-bubble.bot { align-self: flex-start; }

    .message-bubble img {
      width: 30px;
      height: 30px;
      border-radius: 50%;
      flex: 0 0 auto;
    }

    .chat-input {
      display: flex;
      flex-direction: column;
      padding: 1rem;
      background: #fff;
      border-top: 1px solid #ddd;
      gap: 0.75rem;
    }
    .mode-selector {
      display: flex;
      gap: 1rem;
      align-items: center;
      padding: 0.5rem;
      background: #f8f9fa;
      border-radius: 6px;
      border: 1px solid #e9ecef;
      flex-wrap: wrap;
    }
    .mode-selector label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
      font-size: 0.9rem;
      color: #495057;
      font-weight: 500;
      user-select: none;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      transition: all 0.2s;
    }
    .mode-selector input[type="radio"] {
      width: 16px;
      height: 16px;
      cursor: pointer;
    }
    .mode-selector label:hover { background: #e9ecef; color: #007bff; }
    /* Preferred selector */
    .mode-selector label:has(input:checked) { background: #007bff; color: white; }
    /* Fallback for environments without :has support */
    .mode-selector label[data-checked="true"] { background: #007bff; color: #fff; }

    .input-row { display: flex; gap: 1rem; }
    .input-row input {
      flex: 1;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1rem;
    }
    .input-row button {
      padding: 0.75rem 1.5rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
    }
    .input-row button[disabled] { opacity: .5; cursor: not-allowed; }
    .input-row button:hover:not([disabled]) { background: #0056b3; }
  `;

    // ---------- Reactive properties ----------
    static properties = {
        messages: { type: Array },
        selectedMode: { type: String },
        _connectionState: { state: true }, // 'connecting' | 'open' | 'closed'
    };

    // ---------- Lifecycle ----------
    constructor() {
        super();
        this.messages = [];
        this.socket = null;

        // Single selected mode (mutually exclusive)
        this.selectedMode = 'basic'; // default: Basic selected

        // Streaming glue
        this._isBotStreaming = false;
        this._streamResetTimer = null;

        // Reconnect control
        this._reconnectAttempts = 0;
        this._reconnectTimer = null;

        this._connectionState = 'connecting';

        // bind handlers so we can add/remove listeners cleanly
        this._onOpen = () => {
            this._reconnectAttempts = 0;
            this._connectionState = 'open';
            this.requestUpdate();
        };

        this._onMessage = (event) => this._handleServerMessage(event.data);

        this._onClose = () => {
            this._connectionState = 'closed';
            this.requestUpdate();
            const backoff = Math.min(3000 * Math.pow(1.5, this._reconnectAttempts++), 15000);
            const jitter = Math.random() * 300; // to avoid reconnection stampedes
            this._reconnectTimer = setTimeout(() => this._connect(), backoff + jitter);
        };

        this._onError = () => {
            try { this.socket?.close(); } catch {}
        };
    }

    connectedCallback() {
        super.connectedCallback();
        this._connect();
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        try { this.socket?.close(); } catch {}
        this.socket = null;
        clearTimeout(this._streamResetTimer);
        clearTimeout(this._reconnectTimer);
    }

    // ---------- WebSocket ----------
    _connect() {
        this._connectionState = 'connecting';
        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
        // Connect to the backend WebSocket server (niby-be-core) instead of the UI server
        const url = `${protocol}://localhost:8080/niby-ws`;
        this.socket = new WebSocket(url);

        this.socket.addEventListener('open', this._onOpen);
        this.socket.addEventListener('message', this._onMessage);
        this.socket.addEventListener('close', this._onClose);
        this.socket.addEventListener('error', this._onError);
    }

    _handleServerMessage(raw) {
        console.log('Raw server message:', raw);
        const { cleanText, thinkingText } = this._stripThink(raw);
        console.log('Extracted thinking text:', thinkingText);
        console.log('Clean text:', cleanText);

        // Bubble out the <think> content (hidden chain-of-thought UI)
        if (thinkingText) {
            console.log('Dispatching thinking-updated event with:', thinkingText);
            this.dispatchEvent(new CustomEvent('thinking-updated', {
                detail: { thinkingText },
                bubbles: true,
                composed: true
            }));
        }

        if (!cleanText) return;

        // Stream chunks into the latest bot message
        const last = this.messages[this.messages.length - 1];
        if (this._isBotStreaming && last && last.sender === 'bot') {
            last.text += cleanText;
        } else {
            this._isBotStreaming = true;
            this.messages.push({ text: cleanText, sender: 'bot' });
        }

        // Debounce: if no chunks arrive for a short period, consider the bot done
        clearTimeout(this._streamResetTimer);
        this._streamResetTimer = setTimeout(() => {
            this._isBotStreaming = false;
            // Don't clear "thinking" panel automatically - let it persist until next message
        }, 400);

        this.requestUpdate();
    }

    _stripThink(text) {
        const thinkMatch = text.match(/<think>([\s\S]*?)<\/think>/);
        const thinkingText = thinkMatch ? thinkMatch[1].trim() : '';
        const cleanText = text.replace(/<think>[\s\S]*?<\/think>/g, '').trim();
        return { cleanText, thinkingText };
    }

    // ---------- Render ----------
    render() {
        const state = this._connectionState;
        const canSend = this.socket?.readyState === WebSocket.OPEN;

        return html`
      <div class="chat-header">
        <img src="niby.png" alt="Niby Icon">
        <span>Niby</span>
      </div>

      <div class="status" data-state="${state}" aria-live="polite">
        <span class="dot" aria-hidden="true"></span>
        <span>
          ${state === 'open' ? 'Connesso' : state === 'connecting' ? 'Connessione in corso…' : 'Disconnesso – riconnessione automatica'}
        </span>
      </div>

      <div class="chat-messages" part="messages">
        ${this.messages.map((msg) => html`
          <div class="message-bubble ${msg.sender}">
            ${msg.sender === 'bot' ? html`<img src="niby.png" alt="bot">` : html``}
            <span>${unsafeHTML(DOMPurify.sanitize(marked.parse(msg.text)))}</span>
          </div>
        `)}
      </div>

      <div class="chat-input">
        <div class="mode-selector" role="radiogroup" aria-label="Assistant modes">
          ${['basic','plan','act'].map(mode => html`
            <label data-checked="${this.selectedMode === mode}">
              <input
                type="radio"
                name="mode"
                .value=${mode}
                .checked=${this.selectedMode === mode}
                @change=${() => this._setMode(mode)}
              />
              ${mode.charAt(0).toUpperCase() + mode.slice(1)}
            </label>
          `)}
        </div>

        <div class="input-row">
          <input
            type="text"
            placeholder="Type your message here..."
            aria-label="Message to Niby"
            @keydown=${this._handleKeydown}
          />
          <button ?disabled=${!canSend} @click=${this._sendMessage}>${canSend ? 'Send' : 'Wait…'}</button>
        </div>
      </div>
    `;
    }

    // ---------- UI handlers ----------
    _setMode(mode) {
        this.selectedMode = mode;
        // Fallback visual state for label highlight when :has is unsupported
        this.updateComplete.then(() => {
            this.shadowRoot.querySelectorAll('.mode-selector label').forEach(l => {
                const input = l.querySelector('input[type="radio"]');
                l.toggleAttribute('data-checked', !!input?.checked);
            });
        });
    }

    _handleKeydown = (e) => {
        if (e.key === 'Enter') { e.preventDefault(); this._sendMessage(); }
    };

    _sendMessage = () => {
        const input = this.shadowRoot.querySelector('.input-row input');
        const message = (input?.value || '').trim();
        if (!message) return;

        // Clear previous thoughts panel
        this.dispatchEvent(new CustomEvent('thinking-updated', {
            detail: { thinkingText: '' },
            bubbles: true,
            composed: true
        }));

        // Show user bubble immediately
        this.messages.push({ text: message, sender: 'user' });

        // Send as JSON with message and mode
        if (this.socket?.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify({ message, mode: this.selectedMode }));
        } else {
            this.messages.push({ text: '_Riconnessione in corso… riprova fra poco._', sender: 'bot' });
        }

        input.value = '';
        this.requestUpdate();
    };

    updated() {
        const messagesContainer = this.shadowRoot.querySelector('.chat-messages');
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }
}

if (!customElements.get('demo-chat')) {
    customElements.define('demo-chat', DemoChat);
}
