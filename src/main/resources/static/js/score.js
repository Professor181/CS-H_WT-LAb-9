let selectedMatch;
let selectedInnings;
let refreshTimer;
async function loadScoreMatches() {
  try {
    const matches = await api('/matches');
    document.querySelector('#match-select').innerHTML = '<option value="">Choose match</option>' + matches.map(match => `<option value="${match.id}">${esc(match.title)} · ${esc(match.status)}</option>`).join('');
    const fromQuery = new URLSearchParams(location.search).get('matchId');
    if (fromQuery) document.querySelector('#match-select').value = fromQuery;
    if (fromQuery) await selectMatch(fromQuery);
  } catch (error) { notice(`Could not load matches: ${error.message}`); }
}
async function selectMatch(id) {
  clearInterval(refreshTimer); selectedMatch = id;
  if (!id) return;
  try {
    const [match, innings] = await Promise.all([api(`/matches/${id}`), api(`/matches/${id}/innings`)]);
    document.querySelector('#innings-select').innerHTML = innings.map(item => `<option value="${item.id}">Innings ${item.inningsNumber} · ${esc(item.battingTeam.name)}${item.complete ? ' · complete' : ''}</option>`).join('');
    const current = innings.find(item => !item.complete) || innings.at(-1);
    if (!current) throw new Error('Start the match to create its first innings.');
    document.querySelector('#innings-select').value = current.id;
    selectedInnings = current.id;
    await loadLineup(current);
    await refreshScore();
    if (match.status === 'LIVE') refreshTimer = setInterval(refreshScore, 3000);
  } catch (error) { notice(error.message); }
}
async function loadLineup(innings) {
  const [batters, bowlers] = await Promise.all([api(`/teams/${innings.battingTeam.id}/players`), api(`/teams/${innings.bowlingTeam.id}/players`)]);
  const batOptions = batters.map(player => `<option value="${player.id}">${esc(player.name)}</option>`).join('');
  const bowlOptions = bowlers.map(player => `<option value="${player.id}">${esc(player.name)}</option>`).join('');
  document.querySelector('#striker').innerHTML = batOptions; document.querySelector('#non-striker').innerHTML = batOptions;
  document.querySelector('#dismissed').innerHTML = batOptions; document.querySelector('#bowler').innerHTML = bowlOptions;
  if (batters.length > 1) document.querySelector('#non-striker').selectedIndex = 1;
}
async function refreshScore() {
  if (!selectedMatch) return;
  try {
    const score = await api(`/matches/${selectedMatch}/score`);
    document.querySelector('#score-panel').innerHTML = `<div class="match-label">INNINGS ${score.innings} · ${esc(score.battingTeam)} batting</div><div class="score-numbers">${score.runs}/${score.wickets} <small>(${esc(score.overs)} ov)</small></div><div class="stat-row"><div>RUN RATE<strong>${score.runRate}</strong></div><div>BATTER<strong>${score.striker ? `${esc(score.striker.name)} ${score.striker.runs} (${score.striker.balls})` : '—'}</strong></div><div>BOWLER<strong>${score.bowler ? `${esc(score.bowler.name)} ${score.bowler.wickets}/${score.bowler.runs}` : '—'}</strong></div></div>`;
    document.querySelector('#recent-balls').innerHTML = (score.recentBalls || []).slice().reverse().map(ball => `<span class="ball-pill ${ball.wicket ? 'wicket' : ''}" title="${esc(ball.bowler)}">${esc(ball.label)}</span>`).join('') || '<span class="muted">No deliveries yet.</span>';
  } catch (error) { notice(error.message); }
}
async function sendDelivery({ runs = 0, extras = 0, extraType = 'NONE', wicket = false }) {
  hideNotice();
  try {
    if (!selectedInnings) throw new Error('Choose a match with an active innings.');
    const payload = { strikerId: Number(document.querySelector('#striker').value), nonStrikerId: Number(document.querySelector('#non-striker').value), bowlerId: Number(document.querySelector('#bowler').value), runs, extras, extraType, wicket, wicketType: wicket ? document.querySelector('#wicket-type').value : 'NONE', dismissedPlayerId: wicket ? Number(document.querySelector('#dismissed').value) : null };
    await api(`/innings/${selectedInnings}/ball`, { method: 'POST', body: JSON.stringify(payload) });
    await refreshScore();
  } catch (error) { notice(error.message); }
}
document.addEventListener('DOMContentLoaded', () => {
  loadScoreMatches();
  document.querySelector('#match-select').addEventListener('change', event => selectMatch(event.target.value));
  document.querySelector('#innings-select').addEventListener('change', async event => {
    selectedInnings = event.target.value;
    try { const innings = await api(`/matches/${selectedMatch}/innings/${selectedInnings}`); await loadLineup(innings); await refreshScore(); } catch (error) { notice(error.message); }
  });
  document.querySelector('.scoring-grid').addEventListener('click', event => {
    const button = event.target.closest('[data-runs], [data-wicket]'); if (!button) return;
    if (button.dataset.wicket) sendDelivery({ wicket: true }); else sendDelivery({ runs: Number(button.dataset.runs) });
  });
  document.querySelector('.panel .inline-controls').addEventListener('click', event => {
    const type = event.target.dataset.extra; if (type) sendDelivery({ extras: 1, extraType: type });
  });
});