import { css, html, LitElement } from 'https://unpkg.com/lit@3?module';
import { unsafeHTML } from 'https://unpkg.com/lit@3/directives/unsafe-html.js?module';
import { marked } from 'https://cdn.jsdelivr.net/npm/marked/lib/marked.esm.js';
import DOMPurify from 'https://cdn.jsdelivr.net/npm/dompurify@3.1.6/dist/purify.es.mjs';

export class ThinkingPanel extends LitElement {
    static styles = css`
        :host {
            display: block;
            width: 100%;
        }
        .thinking-container {
            background: #fff3cd;
            border: 1px solid #ffc107;
            border-radius: 8px;
            padding: 1rem;
            margin: 1rem;
            display: none;
            flex-direction: column;
            gap: 0.5rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .thinking-container.visible {
            display: flex;
        }
        .thinking-header {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-weight: bold;
            color: #856404;
            font-size: 0.9rem;
        }
        .thinking-icon {
            animation: pulse 1.5s ease-in-out infinite;
        }
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        .thinking-content {
            color: #856404;
            font-size: 0.85rem;
            line-height: 1.5;
            padding: 0.5rem;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 4px;
            max-height: 200px;
            overflow-y: auto;
        }
        .thinking-content:empty {
            display: none;
        }
    `;

    static properties = {
        thinkingText: { type: String }
    };

    constructor() {
        super();
        this.thinkingText = '';
        this._handleThinkingUpdate = this._handleThinkingUpdate.bind(this);
    }

    connectedCallback() {
        super.connectedCallback();
        window.addEventListener('thinking-updated', this._handleThinkingUpdate);
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        window.removeEventListener('thinking-updated', this._handleThinkingUpdate);
    }

    _handleThinkingUpdate(event) {
        console.log('ThinkingPanel received event:', event.detail);
        this.thinkingText = event.detail.thinkingText || '';
        this.requestUpdate();
    }

    render() {
        return html`
            <div class="thinking-container ${this.thinkingText ? 'visible' : ''}">
                <div class="thinking-header">
                    <span class="thinking-icon">💭</span>
                    <span>Thinking...</span>
                </div>
                <div class="thinking-content">
                    ${this.thinkingText ? unsafeHTML(DOMPurify.sanitize(marked.parse(this.thinkingText))) : ''}
                </div>
            </div>
        `;
    }
}

if (!customElements.get('thinking-panel')) {
    customElements.define('thinking-panel', ThinkingPanel);
}
