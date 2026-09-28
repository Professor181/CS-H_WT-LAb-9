const matchId = new URLSearchParams(location.search).get('id');
let poller;
function renderScore(score, match) {
  const recent = (score.recentBalls || []).slice().reverse().map(ball => `<span class="ball-pill ${ball.wicket ? 'wicket' : ''}" title="${esc(ball.bowler)}">${esc(ball.label)}</span>`).join('');
  const innings = (score.inningsScores || []).map(item => `<div class="player-stat"><div><strong>${esc(item.team)}</strong><small>Innings ${item.innings} · ${esc(item.overs)} overs</small></div><span>${item.runs}/${item.wickets}</span></div>`).join('');
  const batsmen = [score.striker, score.nonStriker].filter(Boolean).map(player => `<div class="player-stat"><div><strong>${esc(player.name)}</strong><small>${player.balls} balls · ${player.fours} fours · ${player.sixes} sixes</small></div><span>${player.runs}</span></div>`).join('') || '<div class="empty-state">Batting lineup not recorded yet.</div>';
  const bowler = score.bowler ? `<div class="player-stat"><div><strong>${esc(score.bowler.name)}</strong><small>${esc(score.bowler.overs)} overs · ${score.bowler.runs} runs</small></div><span>${score.bowler.wickets} W</span></div>` : '<div class="empty-state">Bowler stats will appear after the first delivery.</div>';
  document.querySelector('#match-content').innerHTML = `<header class="page-heading"><div><div class="eyebrow">${esc(match.venue)} · ${esc(match.matchDate || '')}</div><h1>${esc(match.title)}</h1><p>${statusTag(match.status)} &nbsp; ${match.status === 'LIVE' ? '<span class="nav-live"><i class="live-dot"></i> Live score polling</span>' : ''}</p></div><a class="btn-primary" href="score.html?matchId=${match.id}">Open scorer &#8594;</a></header><div class="score-layout"><section><div class="big-score"><div class="match-label">INNINGS ${score.innings} · ${esc(score.battingTeam)} batting</div><div class="score-numbers">${score.runs}/${score.wickets} <small>(${esc(score.overs)} ov)</small></div><div class="stat-row"><div>RUN RATE<strong>${score.runRate}</strong></div><div>BATTING<strong>${esc(score.battingTeam)}</strong></div><div>BOWLING<strong>${esc(score.bowlingTeam)}</strong></div></div></div><div class="section-head"><h2>Innings</h2><span class="count">${match.status}</span></div><div class="panel">${innings}</div><div class="section-head"><h2>Recent deliveries</h2><span class="count">Newest at right</span></div><div class="panel"><div class="balls">${recent || '<span class="muted">No deliveries recorded.</span>'}</div></div></section><aside><div class="panel"><div class="section-head" style="margin:0 0 5px"><h2>Batters</h2></div>${batsmen}</div><div class="panel" style="margin-top:14px"><div class="section-head" style="margin:0 0 5px"><h2>Current bowler</h2></div>${bowler}</div><div id="summary-panel"></div></aside></div>`;
}
async function loadMatch() {
  try {
    if (!matchId) throw new Error('Choose a match from the match list.');
    const match = await api(`/matches/${matchId}`);
    if (match.status === 'UPCOMING') {
      clearInterval(poller);
      document.querySelector('#match-content').innerHTML = `<header class="page-heading"><div><div class="eyebrow">${esc(match.venue)} · ${esc(match.matchDate || '')}</div><h1>${esc(match.title)}</h1><p>${statusTag(match.status)}</p></div></header><div class="panel">This fixture has not started. The first innings score will appear here when the match goes live.</div>`;
      return;
    }
    const score = await api(`/matches/${matchId}/score`);
    renderScore(score, match);
    if (match.status === 'COMPLETED') {
      clearInterval(poller);
      const summary = await api(`/matches/${matchId}/summary`);
      document.querySelector('#summary-panel').innerHTML = `<div class="panel" style="margin-top:14px"><div class="eyebrow">MATCH SUMMARY</div><h3 style="font:700 17px Manrope;margin:10px 0">${esc(summary.result)}</h3><p class="muted" style="font-size:12px">${esc(summary.venue)} · ${esc(summary.matchDate || '')}</p><strong style="font-size:12px">Top scorer</strong><p class="muted" style="font-size:12px">${summary.topScorer ? `${esc(summary.topScorer.name)} · ${summary.topScorer.runs} (${summary.topScorer.balls})` : 'No batting score'}</p></div>`;
    }
  } catch (error) { notice(`Could not load match: ${error.message}`); }
}
document.addEventListener('DOMContentLoaded', () => { loadMatch(); poller = setInterval(loadMatch, 3000); });