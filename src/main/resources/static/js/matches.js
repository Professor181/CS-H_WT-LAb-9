async function loadMatchPage() {
  try {
    const teams = await api('/teams');
    const options = teams.map(team => `<option value="${team.id}">${esc(team.name)} (${esc(team.shortName)})</option>`).join('');
    document.querySelector('#team1').innerHTML = options; document.querySelector('#team2').innerHTML = options;
    document.querySelector('#date').value ||= new Date().toISOString().slice(0, 10);
    const matches = await api('/matches');
    document.querySelector('#match-count').textContent = `${matches.length} matches`;
    const rows = await Promise.all(matches.map(async match => {
      let score = '';
      if (match.status !== 'UPCOMING') { try { const value = await api(`/matches/${match.id}/score`); score = ` · ${value.runs}/${value.wickets} (${value.overs})`; } catch (_) { } }
      const controls = match.status === 'UPCOMING' ? `<button class="btn-primary btn-small" data-action="start" data-id="${match.id}">Start</button><button class="btn-danger" data-action="delete" data-id="${match.id}">Delete</button>` : match.status === 'LIVE' ? `<a class="btn-outline btn-small" href="match.html?id=${match.id}">View</a><button class="btn-outline btn-small" data-action="complete" data-id="${match.id}">Complete</button>` : `<a class="btn-outline btn-small" href="match.html?id=${match.id}">Summary</a>`;
      return `<tr><td><strong>${esc(match.title)}</strong>${score ? `<div class="muted">${score}</div>` : ''}</td><td>${esc(match.venue)}</td><td>${esc(match.matchDate || '—')}</td><td>${statusTag(match.status)}</td><td><div class="table-actions">${controls}</div></td></tr>`;
    }));
    document.querySelector('#matches-body').innerHTML = rows.join('') || '<tr><td colspan="5" class="empty-state">No matches yet. Create the first fixture above.</td></tr>';
  } catch (error) { notice(`Could not load matches: ${error.message}`); }
}
document.addEventListener('DOMContentLoaded', () => {
  loadMatchPage();
  document.querySelector('#match-form').addEventListener('submit', async event => {
    event.preventDefault(); hideNotice();
    try {
      const team1Id = Number(document.querySelector('#team1').value); const team2Id = Number(document.querySelector('#team2').value);
      if (team1Id === team2Id) throw new Error('Choose two different teams.');
      await api('/matches', { method: 'POST', body: JSON.stringify({ title: document.querySelector('#title').value, venue: document.querySelector('#venue').value, matchDate: document.querySelector('#date').value, team1Id, team2Id }) });
      event.target.reset(); await loadMatchPage();
    } catch (error) { notice(error.message); }
  });
  document.querySelector('#matches-body').addEventListener('click', async event => {
    const button = event.target.closest('[data-action]'); if (!button) return;
    const action = button.dataset.action; const id = button.dataset.id;
    try {
      if (action === 'start') await api(`/matches/${id}/start`, { method: 'POST' });
      if (action === 'complete') await api(`/matches/${id}/complete`, { method: 'POST' });
      if (action === 'delete') { if (!confirm('Delete this upcoming match?')) return; await api(`/matches/${id}`, { method: 'DELETE' }); }
      await loadMatchPage();
    } catch (error) { notice(error.message); }
  });
});