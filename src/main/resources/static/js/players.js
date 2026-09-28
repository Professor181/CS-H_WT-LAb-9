async function loadPlayers() {
  try {
    const [players, teams] = await Promise.all([api('/players'), api('/teams')]);
    const selectedTeam = document.querySelector('#team-filter').value;
    const filtered = selectedTeam ? players.filter(player => String(player.teamId) === selectedTeam) : players;
    document.querySelector('#player-count').textContent = `${filtered.length} players`;
    document.querySelector('#players-body').innerHTML = filtered.map(player => `<tr><td><strong>${esc(player.name)}</strong></td><td>${esc(player.team.shortName)}</td><td>${esc(player.role.replace('_', ' '))}</td><td>${esc(player.jerseyNumber ?? '—')}</td><td>${esc(player.battingStyle || '—')}</td><td>${esc(player.bowlingStyle || '—')}</td><td><div class="table-actions"><button class="btn-outline btn-small" data-edit="${player.id}">Edit</button><button class="btn-danger" data-delete="${player.id}">Delete</button></div></td></tr>`).join('') || '<tr><td colspan="7" class="empty-state">No players match this team.</td></tr>';
  } catch (error) { notice(`Could not load players: ${error.message}`); }
}
function resetPlayerForm() {
  document.querySelector('#player-form').reset(); document.querySelector('#player-id').value = '';
  document.querySelector('#player-form-title').textContent = 'Add player'; document.querySelector('#player-submit').textContent = 'Save player';
  document.querySelector('#player-cancel').classList.add('hidden');
}
document.addEventListener('DOMContentLoaded', async () => {
  try {
    const teams = await api('/teams'); const options = teams.map(team => `<option value="${team.id}">${esc(team.name)}</option>`).join('');
    document.querySelector('#player-team').innerHTML = options;
    document.querySelector('#team-filter').innerHTML += teams.map(team => `<option value="${team.id}">${esc(team.name)}</option>`).join('');
    const teamId = new URLSearchParams(location.search).get('teamId'); if (teamId) document.querySelector('#team-filter').value = teamId;
    await loadPlayers();
  } catch (error) { notice(`Could not load teams: ${error.message}`); }
  document.querySelector('#team-filter').addEventListener('change', loadPlayers);
  document.querySelector('#player-form').addEventListener('submit', async event => {
    event.preventDefault(); hideNotice(); const id = document.querySelector('#player-id').value;
    const jersey = document.querySelector('#jersey').value;
    const body = { name: document.querySelector('#player-name').value, role: document.querySelector('#role').value, jerseyNumber: jersey ? Number(jersey) : null, battingStyle: document.querySelector('#batting').value, bowlingStyle: document.querySelector('#bowling').value, teamId: Number(document.querySelector('#player-team').value) };
    try { await api(`/players${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(body) }); resetPlayerForm(); await loadPlayers(); }
    catch (error) { notice(error.message); }
  });
  document.querySelector('#players-body').addEventListener('click', async event => {
    const edit = event.target.closest('[data-edit]'); const remove = event.target.closest('[data-delete]');
    try {
      if (edit) {
        const player = await api(`/players/${edit.dataset.edit}`);
        document.querySelector('#player-id').value = player.id; document.querySelector('#player-name').value = player.name;
        document.querySelector('#player-team').value = player.teamId; document.querySelector('#role').value = player.role;
        document.querySelector('#jersey').value = player.jerseyNumber ?? ''; document.querySelector('#batting').value = player.battingStyle || '';
        document.querySelector('#bowling').value = player.bowlingStyle || ''; document.querySelector('#player-form-title').textContent = 'Edit player';
        document.querySelector('#player-submit').textContent = 'Update player'; document.querySelector('#player-cancel').classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
      if (remove && confirm('Delete this player?')) { await api(`/players/${remove.dataset.delete}`, { method: 'DELETE' }); await loadPlayers(); }
    } catch (error) { notice(error.message); }
  });
  document.querySelector('#player-cancel').addEventListener('click', resetPlayerForm);
});