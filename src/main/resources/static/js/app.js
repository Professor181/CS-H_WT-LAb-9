const API = '/api';
async function api(path, options = {}) {
  const response = await fetch(`${API}${path}`, { headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try { message = (await response.json()).message || message; } catch (_) { }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}
function esc(value) { return String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char])); }
function notice(message, target = '#notice') {
  const box = document.querySelector(target);
  if (!box) return;
  box.textContent = message;
  box.classList.add('show');
}
function hideNotice(target = '#notice') { document.querySelector(target)?.classList.remove('show'); }
function statusTag(status) {
  const klass = status === 'LIVE' ? '' : status === 'UPCOMING' ? 'status-upcoming' : 'status-completed';
  return `<span class="status-tag ${klass}">${esc(status)}</span>`;
}
function matchCard(match, score) {
  const scoreText = score ? `${esc(score.battingTeam)} ${score.runs}/${score.wickets} <small>(${esc(score.overs)} ov)</small>` : 'Score not started';
  return `<a class="match-card" href="match.html?id=${match.id}"><div class="match-card-top"><span>${esc(match.venue)}</span>${statusTag(match.status)}</div><h3>${esc(match.title)}</h3><p>${esc(match.matchDate || 'Date to be announced')}</p><div class="match-card-score"><span>${scoreText}</span><span>View &#8594;</span></div></a>`;
}
async function loadDashboard() {
  const live = document.querySelector('#live-list');
  try {
    const [matches, teams] = await Promise.all([api('/matches'), api('/teams')]);
    document.querySelector('#team-count').textContent = `${teams.length} teams`;
    const byStatus = status => matches.filter(match => match.status === status);
    for (const status of ['LIVE', 'UPCOMING', 'COMPLETED']) {
      const list = byStatus(status);
      const target = document.querySelector(`#${status.toLowerCase()}-list`);
      document.querySelector(`#${status.toLowerCase()}-count`).textContent = `${list.length} matches`;
      const cards = await Promise.all(list.map(async match => {
        let score = null;
        if (status !== 'UPCOMING') { try { score = await api(`/matches/${match.id}/score`); } catch (_) { } }
        return matchCard(match, score);
      }));
      target.innerHTML = cards.join('') || '<div class="empty-state">No matches in this section yet.</div>';
    }
    const liveMatches = byStatus('LIVE');
    if (liveMatches.length) {
      const match = liveMatches[0];
      const score = await api(`/matches/${match.id}/score`);
      document.querySelector('#featured').innerHTML = `<div class="score-hero"><div class="score-hero-top"><span class="match-label">${esc(match.venue)} &nbsp;·&nbsp; ${esc(match.matchDate || '')}</span><span class="live-badge"><i class="live-dot"></i> LIVE NOW</span></div><h2 class="hero-title">${esc(match.title)}</h2><div class="hero-venue">${esc(score.battingTeam)} batting · innings ${score.innings}</div><div class="score-line"><div><div class="score-team">${esc(score.battingTeam)}</div><div class="score-numbers">${score.runs}/${score.wickets} <small>(${esc(score.overs)} ov)</small></div></div><div class="score-runrate">CURRENT RUN RATE<strong>${score.runRate}</strong></div><a class="btn-primary" href="match.html?id=${match.id}">Open match &#8594;</a></div></div>`;
    } else {
      document.querySelector('#featured').innerHTML = '<div class="panel empty-state">No match is live right now. Create a fixture or browse upcoming matches.</div>';
    }
  } catch (error) {
    notice(`Could not connect to the API. Start the Spring Boot application and check the database configuration. ${error.message}`);
    live.innerHTML = '<div class="empty-state">Dashboard data is unavailable.</div>';
  }
}
document.addEventListener('DOMContentLoaded', () => { if (document.querySelector('#live-list')) loadDashboard(); });