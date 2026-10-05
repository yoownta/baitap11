(() => {
  const video = document.getElementById('lesson-player');
  if (!video) return;
  const key = 'baitap11.video.' + new URL(location.href).searchParams.get('id');
  const message = document.getElementById('player-message');
  let lastSaved = 0;
  video.addEventListener('loadedmetadata', () => {
    try {
      const saved = Number(localStorage.getItem(key));
      if (saved > 0 && saved < video.duration - 5) {
        video.currentTime = saved;
        message.textContent = 'Tiếp tục tại ' + Math.floor(saved / 60) + ':' + String(Math.floor(saved % 60)).padStart(2, '0') + '. Bạn có thể chọn Xem từ đầu.';
      }
    } catch (_) { /* Playback remains available when browser storage is disabled. */ }
  });
  const save = () => {
    if (!Number.isFinite(video.currentTime)) return;
    try { localStorage.setItem(key, String(video.currentTime)); } catch (_) {}
  };
  video.addEventListener('timeupdate', () => { if (Math.abs(video.currentTime - lastSaved) >= 5) { save(); lastSaved = video.currentTime; } });
  video.addEventListener('pause', save);
  video.addEventListener('ended', () => { try { localStorage.removeItem(key); } catch (_) {} });
  video.addEventListener('error', () => { message.textContent = 'Không tải được video. Kiểm tra file hoặc đường dẫn nguồn với quản trị viên.'; });
  document.getElementById('playback-speed').addEventListener('change', e => { video.playbackRate = Number(e.target.value); });
  document.getElementById('restart-video').addEventListener('click', () => { video.currentTime = 0; save(); video.play().catch(() => {}); });
})();
