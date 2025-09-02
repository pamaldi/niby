import {css, html, LitElement} from 'https://unpkg.com/lit@3?module';
import { unsafeHTML } from 'https://unpkg.com/lit@3/directives/unsafe-html.js?module';
import { marked } from 'https://cdn.jsdelivr.net/npm/marked/lib/marked.esm.js';

export class DemoChat extends LitElement {

    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            height: 100%;
            width: 100%;
            overflow: hidden;
        }
        .chat-header {
            background: #ffc107; /* Orange header */
            color: #212529;
            padding: 1rem;
            font-weight: bold;
            border-radius: 8px 8px 0 0;
            display: flex;
            align-items: center; /* Align items vertically */
            gap: 10px; /* Add space between icon and title */
        }
        .chat-header img {
            width: 30px;
            height: 30px;
        }
        .chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: 1rem;
            background: #e3f2fd; /* Light blue background */
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
        }
        .chat-input {
            display: flex;
            padding: 1rem;
            background: #fff;
            border-top: 1px solid #ddd;
        }
        .chat-input input {
            flex: 1;
            margin-right: 1rem;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
        }
        .chat-input button {
            padding: 0.75rem 1.5rem;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 1rem;
        }
        .chat-input button:hover {
            background: #0056b3;
        }
    `;

    static properties = {
        messages: { type: Array }
    };

    constructor() {
        super();
        this.messages = [];
        this.socket = null;
    }

    _stripThink(text) {
        const thinkMatch = text.match(/<think>([\s\S]*?)<\/think>/);
        const thinkingText = thinkMatch ? thinkMatch[1].trim() : '';
        const cleanText = text.replace(/<think>[\s\S]*?<\/think>/g, '').trim();
        return { cleanText, thinkingText };
    }

    connectedCallback() {
        super.connectedCallback();
        this.connect();
    }

    connect() {
        const protocol = (window.location.protocol === 'https:') ? 'wss' : 'ws';
        this.socket = new WebSocket(protocol + '://' + window.location.host + '/customer-support-agent');

        this.socket.onmessage = (event) => {
            const { cleanText, thinkingText } = this._stripThink(event.data);

            if (thinkingText) {
                this.dispatchEvent(new CustomEvent('thinking-updated', {
                    detail: { thinkingText },
                    bubbles: true,
                    composed: true
                }));
            }

            if (!cleanText) return;

            let lastMessage = this.messages[this.messages.length - 1];

            if (this.isBotThinking && lastMessage && lastMessage.sender === 'bot') {
                // Append to the last message
                lastMessage.text += cleanText;
            } else {
                // Add a new message
                this.isBotThinking = true; // Bot starts thinking
                this.messages.push({ text: cleanText, sender: 'bot' });
            }
            this.requestUpdate();
        };

        this.socket.onclose = () => {
            console.log("WebSocket closed. Reconnecting...");
            setTimeout(() => this.connect(), 3000);
        };
    }

    render() {
        return html`
            <div class="chat-header">
                <img src="niby.png" alt="Niby Icon">
                <span>Niby</span>
            </div>
            <div class="chat-messages">
                ${this.messages.map(msg => html`
                    <div class="message-bubble ${msg.sender}">
                        ${msg.sender === 'bot' ? html`<img src="niby.png" alt="bot">` : ''}
                        <span>${unsafeHTML(marked(msg.text))}</span>
                    </div>
                `)}
            </div>
            <div class="chat-input">
                <input 
                    type="text"
                    placeholder="Type your message here..."
                    @keydown="${this._handleKeydown}">
                <button @click="${this._sendMessage}">
                    Send
                </button>
            </div>
        `;
    }

    _handleKeydown(e) {
        if (e.key === 'Enter') {
            this._sendMessage();
        }
    }

    _sendMessage() {
        const input = this.shadowRoot.querySelector('input');
        const message = input.value.trim();
        if (message) {
            // Clear previous thoughts
            this.dispatchEvent(new CustomEvent('thinking-updated', {
                detail: { thinkingText: '' },
                bubbles: true,
                composed: true
            }));

            this.messages.push({ text: message, sender: 'user' });
            this.socket.send(message);
            input.value = '';
            this.requestUpdate();
        }
    }

    updated() {
        const messagesContainer = this.shadowRoot.querySelector('.chat-messages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

customElements.define('demo-chat', DemoChat);
