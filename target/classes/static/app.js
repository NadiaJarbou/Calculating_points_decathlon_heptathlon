const el = (id) => document.getElementById(id);
const err = el('error');
const msg = el('msg');
const rawInput = el('raw');

const MODE = {
  DEC: 'DEC',
  HEP: 'HEP'
};

const EVENTS_BY_MODE = {
  DEC: [
    { key: '100m', label: '100m (s)' },
    { key: 'longJump', label: 'Long Jump (cm)' },
    { key: 'shotPut', label: 'Shot Put (m)' },
    { key: '400m', label: '400m (s)' },
    { key: '110mHurdles', label: '110m Hurdles (s)' },
    { key: 'discus', label: 'Discus (m)' },
    { key: 'poleVault', label: 'Pole Vault (cm)' },
    { key: 'javelin', label: 'Javelin (m)' },
    { key: '1500m', label: '1500 m (s)' }
  ],
  HEP: [
    { key: '100mHurdles', label: '100m Hurdles (s)' },
    { key: 'highJump',    label: 'High Jump (cm)' },
    { key: 'shotPut',     label: 'Shot Put (m)' },
    { key: '200m',        label: '200m (s)' },
    { key: 'longJump',    label: 'Long Jump (cm)' },
    { key: 'javelin',     label: 'Javelin (m)' },
    { key: '800m',        label: '800m (s)' }
  ]
};

const modeSel = el('mode');
const eventSel = el('event');

function currentMode() {
  return modeSel?.value || MODE.DEC;
}

function rebuildEventSelect() {
  const events = EVENTS_BY_MODE[currentMode()];
  eventSel.innerHTML = events.map(ev => `<option value="${ev.key}">${ev.label}</option>`).join('');
  eventSel.selectedIndex = 0;
}

function rebuildStandingsHeader() {
  const events = EVENTS_BY_MODE[currentMode()];
  const thead = document.querySelector('thead > tr');
  thead.innerHTML = [
    '<th>Name</th>',
    ...events.map(ev => `<th>${ev.label.split(' (')[0]}</th>`),
    '<th>Total</th>'
  ].join('');
}

modeSel.addEventListener('change', async () => {
  rebuildEventSelect();
  rebuildStandingsHeader();
  await renderStandings();
});

rebuildEventSelect();
rebuildStandingsHeader();
renderStandings();

function setError(text) { err.textContent = text; }
function setMsg(text) { msg.textContent = text; }

el('add').addEventListener('click', async (evt) => {
  evt?.preventDefault?.();
  const name = el('name').value;
  try {
    const res = await fetch(`/api/competitors?mode=${currentMode()}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name })
    });
    if (!res.ok) {
      const t = await res.text();
      setError(t || `Failed to add competitor (status ${res.status})`);
    } else {
      setMsg('Added');
    }
    await renderStandings();
  } catch (e) {
    setError('Network error');
  }
});

el('save').addEventListener('click', async () => {
  try {
    const body = {
      name: el('name2').value,
      event: el('event').value,
      raw: Number(rawInput.value),
      mode: currentMode()
    };
    const res = await fetch('/api/score', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const json = await res.json();
    setMsg(`Saved: ${json.points} pts`);
    await renderStandings();
  } catch (e) {
    setError('Score failed');
  }
});

let sortBroken = false;

el('export').addEventListener('click', async () => {
  try {
    const res = await fetch(`/api/export.csv?mode=${currentMode()}`);
    const text = await res.text();
    const blob = new Blob([text], { type: 'text/csv;charset=utf-8' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'results.csv';
    a.click();
    sortBroken = true;
  } catch (e) {
    setError('Export failed');
  }
});

async function renderStandings() {
  try {
    const res = await fetch(`/api/standings?mode=${currentMode()}`);
    const data = await res.json();
    const events = EVENTS_BY_MODE[currentMode()];

    const rows = (sortBroken ? data : data.sort((a,b)=> (b.total||0)-(a.total||0)))
      .map(r => {
        const tds = events.map(ev => `<td>${r.scores?.[ev.key] ?? ''}</td>`).join('');
        return `<tr>
            <td>${escapeHtml(r.name)}</td>
            ${tds}
            <td>${r.total ?? 0}</td>
          </tr>`;
      }).join('');

    el('standings').innerHTML = rows;
  } catch (e) {
    setError('Could not load standings');
  }
}

function escapeHtml(s){
  return String(s).replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
}

renderStandings();
