import { css, html, LitElement } from 'https://unpkg.com/lit@3?module';
import { unsafeHTML } from 'https://unpkg.com/lit@3/directives/unsafe-html.js?module';
import { marked } from 'https://cdn.jsdelivr.net/npm/marked/lib/marked.esm.js';

export class DemoChat extends LitElement {
    // ---------- Styles ----------
    static styles = css`
    :host {
      display: flex;
      flex-direction: column;
      height: 100%;
      width: 100%;
      overflow: hidden;
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
    }
    .message-bubble.user {
      background-color: #fff;
      color: #333;
      align-self: flex-end;
      border: 1px solid #ddd;
    }
    .message-bubble.bot {
      background-color: #fff;
      color: #333;
      align-self: flex-start;
      border: 1px solid #ddd;
    }
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
    .mode-selector label:hover {
      background: #e9ecef;
      color: #007bff;
    }
    .mode-selector label:has(input:checked) {
      background: #007bff;
      color: white;
    }
    .input-row {
      display: flex;
      gap: 1rem;
    }
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
    .input-row button:hover {
      background: #0056b3;
    }
  `;

    // ---------- Reactive properties ----------
    static properties = {
        messages: { type: Array },
        selectedMode: { type: String }
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
    }

    connectedCallback() {
        super.connectedCallback();
        this._connect();
    }

    // ---------- WebSocket ----------
    _connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
        const url = `${protocol}://${window.location.host}/customer-support-agent`;
        this.socket = new WebSocket(url);

        this.socket.onopen = () => {
            this._reconnectAttempts = 0;
            // Optionally announce active mode
            // this.socket.send(JSON.stringify({ type: 'mode', mode: this.selectedMode }));
        };

        this.socket.onmessage = (event) => this._handleServerMessage(event.data);

        this.socket.onclose = () => {
            const backoff = Math.min(3000 * Math.pow(1.5, this._reconnectAttempts++), 15000);
            setTimeout(() => this._connect(), backoff);
        };

        this.socket.onerror = () => {
            try { this.socket.close(); } catch (_) {}
        };
    }

    _handleServerMessage(raw) {
        const { cleanText, thinkingText } = this._stripThink(raw);

        // Bubble out the <think> content (hidden chain-of-thought UI)
        if (thinkingText) {
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
            // Clear "thinking" panel at the end of a response
            this.dispatchEvent(new CustomEvent('thinking-updated', {
                detail: { thinkingText: '' },
                bubbles: true,
                composed: true
            }));
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
        return html`
      <div class="chat-header">
        <img src="niby.png" alt="Niby Icon">
        <span>Niby</span>
      </div>

      <div class="chat-messages" part="messages">
        ${this.messages.map(
            (msg) => html`
            <div class="message-bubble ${msg.sender}">
              ${msg.sender === 'bot'
                ? html`<img src="niby.png" alt="bot">`
                : html``}
              <span>${unsafeHTML(marked(msg.text))}</span>
            </div>
          `
        )}
      </div>

      <div class="chat-input">
        <div class="mode-selector" role="radiogroup" aria-label="Assistant modes">
          <label>
            <input
              type="radio"
              name="mode"
              value="basic"
              .checked=${this.selectedMode === 'basic'}
              @change=${() => this._setMode('basic')}
            />
            Basic
          </label>
          <label>
            <input
              type="radio"
              name="mode"
              value="plan"
              .checked=${this.selectedMode === 'plan'}
              @change=${() => this._setMode('plan')}
            />
            Plan
          </label>
          <label>
            <input
              type="radio"
              name="mode"
              value="act"
              .checked=${this.selectedMode === 'act'}
              @change=${() => this._setMode('act')}
            />
            Act
          </label>
        </div>

        <div class="input-row">
          <input
            type="text"
            placeholder="Type your message here..."
            @keydown=${this._handleKeydown}
          />
          <button @click=${this._sendMessage}>Send</button>
        </div>
      </div>
    `;
    }

    // ---------- UI handlers ----------
    _setMode(mode) {
        this.selectedMode = mode;

        // Optionally notify server of mode change:
        // if (this.socket?.readyState === WebSocket.OPEN) {
        //   this.socket.send(JSON.stringify({ type: 'mode', mode: this.selectedMode }));
        // }
    }

    _handleKeydown = (e) => {
        if (e.key === 'Enter') this._sendMessage();
    };

    _sendMessage = () => {
        const input = this.shadowRoot.querySelector('.input-row input');
        const message = (input?.value || '').trim();
        if (!message) return;

        // Clear previous thoughts panel
        this.dispatchEvent(
            new CustomEvent('thinking-updated', {
                detail: { thinkingText: '' },
                bubbles: true,
                composed: true
            })
        );

        // Show user bubble immediately
        this.messages.push({ text: message, sender: 'user' });

        // Send as plain text to avoid breaking existing backends.
        // If your server supports JSON, you can send:
        // this.socket.send(JSON.stringify({ message, mode: this.selectedMode }));
        if (this.socket?.readyState === WebSocket.OPEN) {
            this.socket.send(message);
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

customElements.define('demo-chat', DemoChat);