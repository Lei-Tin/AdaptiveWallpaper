(() => {
  const root = document.documentElement;
  const saved = localStorage.getItem("adaptivewallpaper-language");
  const preferred = navigator.language.toLowerCase().startsWith("zh") ? "zh" : "en";

  function applyLanguage(language) {
    root.dataset.language = language;
    root.lang = language === "zh" ? "zh-CN" : "en";
    document.title = language === "zh"
      ? (location.pathname.startsWith("/privacy") ? "隐私说明 — AdaptiveWallpaper" : "AdaptiveWallpaper — 随系统明暗模式切换壁纸")
      : (location.pathname.startsWith("/privacy") ? "Privacy — AdaptiveWallpaper" : "AdaptiveWallpaper — Wallpaper that follows your system theme");
  }

  applyLanguage(saved === "zh" || saved === "en" ? saved : preferred);

  document.querySelector("[data-language-toggle]")?.addEventListener("click", () => {
    const language = root.dataset.language === "zh" ? "en" : "zh";
    localStorage.setItem("adaptivewallpaper-language", language);
    applyLanguage(language);
  });
})();
