// UI logic: talks to the same API the Postman collection uses.
// The page is served by the backend itself, so the API is always
// on this same server (window.location.origin), locally and on Render.
const $ = (id) => document.getElementById(id);
const urlInput = $("urlInput");
const aliasInput = $("aliasInput");
const expiryInput = $("expiryInput");
const shortenBtn = $("shortenBtn");
const errorBox = $("error");
const resultBox = $("result");
const shortLink = $("shortLink");
const visits = $("visits");

function getApiBase() {
  return window.location.origin;
}

function showError(msg) {
  errorBox.hidden = !msg;
  errorBox.textContent = msg || "";
}

let currentCode = null;

async function refreshStats() {
  if (!currentCode) return;
  try {
    const r = await fetch(`${getApiBase()}/shorten/${currentCode}/stats`);
    if (r.ok) {
      const s = await r.json();
      visits.textContent = `Visits: ${s.accessCount}`;
    }
  } catch (_) { /* stats are best-effort */ }
}

shortenBtn.addEventListener("click", async () => {
  showError("");
  resultBox.hidden = true;
  const url = urlInput.value.trim();
  if (!url) { showError("Paste a link first."); return; }

  const payload = { url };
  const alias = aliasInput.value.trim();
  if (alias) payload.customCode = alias;
  if (expiryInput.value) {
    const days = parseInt(expiryInput.value, 10);
    payload.expiresAt = new Date(Date.now() + days * 86400000).toISOString();
  }

  shortenBtn.disabled = true;
  shortenBtn.textContent = "Shortening…";
  try {
    const res = await fetch(`${getApiBase()}/shorten`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      showError(data.message || `Request failed (${res.status}).`);
      return;
    }
    currentCode = data.shortCode;
    const shortUrl = `${getApiBase()}/${currentCode}`;
    shortLink.href = shortUrl;
    shortLink.textContent = shortUrl;
    resultBox.hidden = false;
    visits.textContent = "Visits: 0";
  } catch (e) {
    showError("Cannot reach API at " + getApiBase() + ". Is the backend running?");
  } finally {
    shortenBtn.disabled = false;
    shortenBtn.textContent = "Shorten URL";
  }
});

$("openBtn").addEventListener("click", () => {
  if (currentCode) window.open(`${getApiBase()}/${currentCode}`, "_blank");
  setTimeout(refreshStats, 500);
});

$("copyBtn").addEventListener("click", async () => {
  try {
    await navigator.clipboard.writeText(shortLink.textContent);
    $("copyBtn").textContent = "Copied!";
    setTimeout(() => ($("copyBtn").textContent = "Copy"), 1500);
  } catch (_) {
    showError("Copy blocked by browser — select the link manually.");
  }
});

urlInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") shortenBtn.click();
});
