(() => {
  const $ = (id) => document.getElementById(id);
  const chatEl = $('chat');
  const apiBaseEl = $('apiBase');
  const modelEl = $('model');
  const nameEl = $('name');
  const promptEl = $('prompt');
  const sendBtn = $('sendBtn');
  const loadingEl = $('loading');

  // Read query params for defaults
  const params = new URLSearchParams(location.search);
  const isDebug = document.documentElement.classList.contains('debug');
  if (params.get('apiBase') && apiBaseEl) apiBaseEl.value = params.get('apiBase');
  if (params.get('model') && modelEl) modelEl.value = params.get('model');
  if (params.get('name') && nameEl) nameEl.value = params.get('name');
  if ((params.get('q') || params.get('prompt')) && promptEl) {
    promptEl.value = params.get('q') || params.get('prompt');
  }

  // Multi-turn conversation history (Ollama-style)
  const messagesHistory = [];

  function ensureSystemPrompt() {
    if (!messagesHistory.some(m => m.role === 'system')) {
      messagesHistory.push({ role: 'system', content: buildSystemPrompt() });
    }
  }

  function setLoading(on, text = '思考中…') {
    if (!loadingEl) return;
    const t = loadingEl.querySelector('.text');
    if (t && text) t.textContent = text;
    loadingEl.classList.toggle('show', !!on);
  }

  function renderMarkdown(md) {
    try {
      if (window.marked) {
        if (!window.__markedConfigured) {
          window.marked.setOptions({ gfm: true, breaks: true });
          window.__markedConfigured = true;
        }
        return window.marked.parse(md);
      }
    } catch {}
    return md.replace(/\n/g, '<br/>');
  }

  function appendMessage(role, content) {
    const msg = document.createElement('div');
    msg.className = `msg ${role}`;
    const avatar = document.createElement('div');
    avatar.className = `avatar ${role === 'user' ? 'user' : 'assistant'}`;
    avatar.textContent = role === 'user' ? '我' : 'AI';
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.innerHTML = renderMarkdown(content);
    msg.appendChild(avatar);
    msg.appendChild(bubble);
    chatEl.appendChild(msg);
    chatEl.scrollTop = chatEl.scrollHeight;
  }

  // Parse markdown table to scores: { 维度: 分数 }
  function parseMdScores(mdText) {
    const lines = mdText.split(/\r?\n/);
    const scores = {};
    for (const raw of lines) {
      const line = raw.trim();
      if (!line.includes('|')) continue;
      if (line.startsWith('| ----')) continue;
      const parts = line.replace(/^\|/, '').replace(/\|$/, '').split('|').map(s => s.trim());
      if (parts.length === 2 && parts[0] && parts[0] !== '维度') {
        scores[parts[0]] = parts[1];
      }
    }
    return scores;
  }

  async function fetchLocalMarkdown(empName) {
    const filename = `${empName} - 绩效评分表.md`;
    const url = `${location.origin}/data/HR/${encodeURIComponent(filename)}`;
    const res = await fetch(url, { cache: 'no-cache' });
    if (!res.ok) throw new Error(`无法读取本地绩效文件: ${res.status}`);
    return res.text();
  }

  function buildSystemPrompt() {
    return [
      '你是资深 HR 评估助手。请优先使用可用的 MCP 工具 queryEmployeeScore(name) 获取绩效表数据。',
      '必须以 Markdown 返回最终答案：使用 ##/### 标题、无序/有序列表等；不要返回 JSON；不要使用任何代码块围栏（例如 ```markdown 或 ```）；不添加多余前后说明。',
      '格式要求：',
      '## 优势',
      '- 若干要点',
      '## 需优化能力',
      '- 若干要点',
      '## 不足',
      '- 若干要点',
      '## 改进建议',
      '1. 多条可执行建议',
      '语气客观中立，条理清晰，仅输出 Markdown。'
    ].join('\n');
  }

  function looksLikeToolCallString(s) {
    try {
      const o = JSON.parse(s);
      return o && o.name === 'queryEmployeeScore' && o.parameters && o.parameters.name;
    } catch { return false; }
  }

  async function send() {
    const apiBase = apiBaseEl ? apiBaseEl.value.trim().replace(/\/$/, '') : (params.get('apiBase') || 'http://localhost:8000');
    const model = modelEl ? modelEl.value.trim() : (params.get('model') || 'qwen3:8b');
    const empName = nameEl ? nameEl.value.trim() : (params.get('name') || '');
    const userText = (promptEl?.value.trim()) || (params.get('q') || params.get('prompt')) || '请根据以上绩效表给出优势、需优化能力、不足与改进建议。';

    if (!apiBase || !model || !empName) {
      appendMessage('assistant', '请填写 API Base / Model / 员工姓名。');
      return;
    }

    // Show user message and add to history
    const userMsg = `员工：${empName}\n\n${userText}`;
    appendMessage('user', userMsg);
    ensureSystemPrompt();
    messagesHistory.push({ role: 'user', content: `name=${empName}\n\n${userText}` });

    if (sendBtn) sendBtn.disabled = true;
    setLoading(true, '思考中…');

    try {
      const res = await fetch(`${apiBase}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'accept': 'application/json' },
        body: JSON.stringify({
          model,
          messages: messagesHistory,
          stream: false,
          think: false,
          tool_choice: 'auto'
        })
      });

      if (!res.ok) {
        const txt = await res.text().catch(() => '');
        throw new Error(`HTTP ${res.status}: ${txt || res.statusText}`);
      }

      const data = await res.json();
      const content = data?.message?.content ?? data?.content ?? '';

      let assistantContent = content;

      // If model returned a tool-call JSON string, do NOT fetch local files in the browser.
      // Keep data access on server via /api/chat (tools). Show a helpful hint instead.
      if (looksLikeToolCallString(content)) {
        assistantContent = '模型返回了工具调用 JSON，但未实际执行工具。请使用支持 tool_calls 的模型，或在系统提示中明确要求以 tool_calls 方式直接调用 queryEmployeeScore，然后仅输出最终 Markdown。';
      }

      // Show assistant message and add to history for continuity
      appendMessage('assistant', assistantContent || '（空响应）');
      messagesHistory.push({ role: 'assistant', content: assistantContent || '' });
    } catch (err) {
      const msg = `请求失败：${err.message || err}`;
      appendMessage('assistant', msg);
      messagesHistory.push({ role: 'assistant', content: msg });
    } finally {
      setLoading(false);
      if (sendBtn) sendBtn.disabled = false;
      if (promptEl) { promptEl.value = ''; promptEl.focus(); }
    }
  }

  sendBtn?.addEventListener('click', send);
  promptEl?.addEventListener('keydown', (e) => {
    // Enter to send, Shift+Enter for newline
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      if (!sendBtn?.disabled) send();
    }
  });
})();