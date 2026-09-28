async function loadTeams() {
  try {
    const [teams, players] = await Promise.all([api('/teams'), api('/players')]);
    document.querySelector('#team-count').textContent = `${teams.length} teams`;
    document.querySelector('#teams-body').innerHTML = teams.map(team => `<tr><td><strong>${esc(team.name)}</strong><div class="muted">${esc(team.shortName)}</div></td><td>${esc(team.country)}</td><td>${esc(team.coach || '—')}</td><td>${players.filter(player => player.teamId === team.id).length} players</td><td><div class="table-actions"><a class="btn-outline btn-small" href="players.html?teamId=${team.id}">Roster</a><button class="btn-outline btn-small" data-edit="${team.id}">Edit</button><button class="btn-danger" data-delete="${team.id}">Delete</button></div></td></tr>`).join('') || '<tr><td colspan="5" class="empty-state">No teams have been added.</td></tr>';
  } catch (error) { notice(`Could not load teams: ${error.message}`); }
}
function resetTeamForm() {
  document.querySelector('#team-form').reset(); document.querySelector('#team-id').value = '';
  document.querySelector('#team-form-title').textContent = 'Add team'; document.querySelector('#team-submit').textContent = 'Save team';
  document.querySelector('#team-cancel').classList.add('hidden');
}
document.addEventListener('DOMContentLoaded', () => {
  loadTeams();
  document.querySelector('#team-form').addEventListener('submit', async event => {
    event.preventDefault(); hideNotice();
    const id = document.querySelector('#team-id').value;
    const body = { name: document.querySelector('#team-name').value, shortName: document.querySelector('#short-name').value, country: document.querySelector('#country').value, coach: document.querySelector('#coach').value };
    try { await api(`/teams${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(body) }); resetTeamForm(); await loadTeams(); }
    catch (error) { notice(error.message); }
  });
  document.querySelector('#teams-body').addEventListener('click', async event => {
    const edit = event.target.closest('[data-edit]'); const remove = event.target.closest('[data-delete]');
    try {
      if (edit) {
        const team = await api(`/teams/${edit.dataset.edit}`);
        document.querySelector('#team-id').value = team.id; document.querySelector('#team-name').value = team.name;
        document.querySelector('#short-name').value = team.shortName; document.querySelector('#country').value = team.country;
        document.querySelector('#coach').value = team.coach || ''; document.querySelector('#team-form-title').textContent = 'Edit team';
        document.querySelector('#team-submit').textContent = 'Update team'; document.querySelector('#team-cancel').classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
      if (remove && confirm('Delete this team? Teams used in matches or with players cannot be removed.')) { await api(`/teams/${remove.dataset.delete}`, { method: 'DELETE' }); await loadTeams(); }
    } catch (error) { notice(error.message); }
  });
  document.querySelector('#team-cancel').addEventListener('click', resetTeamForm);
});