(function () {
  var REPO = 'CodingfulAlt/Cairn';
  var APK_PREFIX = 'https://github.com/' + REPO + '/releases/download/';
  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function each(selector, fn, scope) {
    Array.prototype.forEach.call((scope || document).querySelectorAll(selector), fn);
  }

  // Point every Download link at the newest APK, so a release never needs a site change.
  var release = new Promise(function (resolve) {
    if (!window.fetch) return resolve();
    var timer = setTimeout(resolve, 1500);
    fetch('https://api.github.com/repos/' + REPO + '/releases/latest', { headers: { Accept: 'application/vnd.github+json' } })
      .then(function (res) { return res.ok ? res.json() : null; })
      .then(function (data) {
        // only ever link to an APK on this repo's own releases
        var apk = data && data.assets && data.assets.filter(function (a) {
          return /^[\w.-]+\.apk$/i.test(a.name) && String(a.browser_download_url).indexOf(APK_PREFIX) === 0;
        })[0];
        if (!apk) return;
        each('[data-dl]', function (a) { a.href = apk.browser_download_url; });
        if (/^v?\d+(\.\d+){0,3}$/.test(data.tag_name)) {
          each('[data-version]', function (el) { el.textContent = data.tag_name; });
        }
        each('[data-size]', function (el) { el.textContent = (apk.size / 1048576).toFixed(1) + ' MB'; });
        each('[data-file]', function (el) { el.textContent = apk.name; });
      })
      .catch(function () {})
      .then(function () { clearTimeout(timer); resolve(); });
  });

  // terminals type their commands the first time they scroll into view
  function typeLine(line, done) {
    var text = line.getAttribute('data-text');
    var i = 0;
    line.textContent = '';
    line.classList.add('on', 'typing');
    (function step() {
      line.textContent = text.slice(0, ++i);
      if (i < text.length) {
        setTimeout(step, 24 + Math.random() * 40);
      } else {
        line.classList.remove('typing');
        setTimeout(done, 280);
      }
    })();
  }
  function runTerminal(term) {
    var cmds = term.querySelectorAll('.cmd');
    each('.cmd', function (c) { c.setAttribute('data-text', c.textContent); }, term);
    var i = 0;
    (function next() {
      if (i < cmds.length) return typeLine(cmds[i++], next);
      each('.out', function (o, k) { setTimeout(function () { o.classList.add('on'); }, k * 200); }, term);
      setTimeout(function () { term.classList.add('done'); }, 600);
    })();
  }

  if ('IntersectionObserver' in window && !reduce) {
    var reveal = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        e.target.classList.add('in');
        reveal.unobserve(e.target);
      });
    }, { rootMargin: '0px 0px -8% 0px', threshold: 0.1 });
    each('[data-reveal]', function (el) { reveal.observe(el); });

    var typer = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        typer.unobserve(e.target);
        release.then(function () { setTimeout(function () { runTerminal(e.target); }, 500); });
      });
    }, { threshold: 0.6 });
    each('[data-type]', function (el) { typer.observe(el); });
  } else {
    each('[data-reveal]', function (el) { el.classList.add('in'); });
    each('[data-type]', function (el) { el.classList.add('done'); });
  }

  // header shadow, reading progress and the active nav link
  var header = document.querySelector('.top');
  var bar = document.querySelector('.progress');
  var dock = document.querySelector('.dock');
  var pill = dock && dock.querySelector('.dock-pill');
  var lastDock = null;

  function spy(selector) {
    var links = Array.prototype.slice.call(document.querySelectorAll(selector));
    var targets = links.map(function (a) { return document.querySelector(a.getAttribute('href')); });
    return function (atBottom) {
      var current = -1;
      targets.forEach(function (t, i) { if (t && t.getBoundingClientRect().top < window.innerHeight * 0.35) current = i; });
      if (atBottom) current = targets.length - 1;
      links.forEach(function (a, i) { a.classList.toggle('active', i === current); });
      return links[current] || null;
    };
  }
  var spyNav = spy('.nav a');
  var spyDock = spy('.dock a');

  var ticking = false;
  function onScroll() {
    ticking = false;
    var y = window.scrollY;
    var max = document.documentElement.scrollHeight - window.innerHeight;
    var atBottom = max > 0 && y >= max - 4;
    header.classList.toggle('scrolled', y > 8);
    bar.style.transform = 'scaleX(' + (max > 0 ? Math.min(y / max, 1) : 0) + ')';
    spyNav(atBottom);
    var active = spyDock(atBottom);
    if (pill && active && active !== lastDock) {
      lastDock = active;
      pill.style.setProperty('--x', active.offsetLeft + 'px');
      dock.classList.add('ready');
    }
  }
  window.addEventListener('resize', function () { lastDock = null; onScroll(); });
  window.addEventListener('scroll', function () {
    if (!ticking) { ticking = true; window.requestAnimationFrame(onScroll); }
  }, { passive: true });
  onScroll();

  // the hero stones stack on load and again on every tap
  if (!reduce) {
    each('[data-stack]', function (btn) {
      var svg = btn.querySelector('.stack-svg');
      svg.classList.add('play');
      btn.addEventListener('click', function () {
        svg.classList.remove('play');
        void svg.getBoundingClientRect();
        svg.classList.add('play');
      });
    });
  }

  // the desktop icon also tilts toward the pointer
  var iconBtn = document.querySelector('.icon-btn');
  if (iconBtn && !reduce) {
    iconBtn.addEventListener('pointermove', function (e) {
      var r = iconBtn.getBoundingClientRect();
      iconBtn.style.setProperty('--ry', ((e.clientX - r.left) / r.width - 0.5) * 16 + 'deg');
      iconBtn.style.setProperty('--rx', (0.5 - (e.clientY - r.top) / r.height) * 16 + 'deg');
    });
    iconBtn.addEventListener('pointerleave', function () {
      iconBtn.style.setProperty('--rx', '0deg');
      iconBtn.style.setProperty('--ry', '0deg');
    });
  }

  // phone mockups: with a mouse the hovered one comes forward and tilts, the other steps back
  var row = document.querySelector('.shots-row');
  if (row && !reduce) {
    each('.shot', function (shot) {
      var img = shot.querySelector('img');
      function reset() {
        shot.classList.remove('is-active');
        row.classList.remove('has-active');
        img.style.setProperty('--rx', '0deg');
        img.style.setProperty('--ry', '0deg');
      }
      shot.addEventListener('pointerenter', function (e) {
        if (e.pointerType !== 'mouse') return;
        shot.classList.add('is-active');
        row.classList.add('has-active');
      });
      shot.addEventListener('pointermove', function (e) {
        if (e.pointerType !== 'mouse') return;
        var r = shot.getBoundingClientRect();
        img.style.setProperty('--ry', ((e.clientX - r.left) / r.width - 0.5) * 14 + 'deg');
        img.style.setProperty('--rx', (0.5 - (e.clientY - r.top) / r.height) * 8 + 'deg');
      });
      shot.addEventListener('pointerleave', reset);
      shot.addEventListener('pointercancel', reset);
    });
  }

  // touch screens: tap a phone and it zooms up from where it sits, tap anywhere or go back to close
  var lightbox = document.querySelector('.lightbox');
  var touchScreen = window.matchMedia ? window.matchMedia('(hover: none)') : { matches: false };
  if (row && lightbox) {
    var lbImg = lightbox.querySelector('img');
    var lbBg = lightbox.querySelector('.lightbox-bg');
    var lbClose = lightbox.querySelector('.lightbox-close');
    var thumb = null;
    var closing = false;
    var shots = Array.prototype.slice.call(row.querySelectorAll('.shot'));

    var applyMode = function () {
      var on = touchScreen.matches;
      row.classList.toggle('zoomable', on);
      shots.forEach(function (shot) {
        var badge = shot.querySelector('.zoom-badge');
        if (on && !badge) {
          badge = document.createElement('span');
          badge.className = 'zoom-badge';
          badge.innerHTML = '<svg aria-hidden="true"><use href="#i-zoom"/></svg>';
          shot.querySelector('.shot-float').appendChild(badge);
        } else if (!on && badge) {
          badge.remove();
        }
        if (on) {
          shot.setAttribute('role', 'button');
          shot.setAttribute('tabindex', '0');
          shot.setAttribute('aria-label', 'Open screenshot: ' + shot.querySelector('img').alt);
        } else {
          shot.removeAttribute('role');
          shot.removeAttribute('tabindex');
          shot.removeAttribute('aria-label');
        }
      });
    };
    applyMode();
    if (touchScreen.addEventListener) touchScreen.addEventListener('change', applyMode);

    // where the big image has to start from so it looks like the small one
    var offsetFrom = function (el) {
      var a = el.getBoundingClientRect();
      var b = lbImg.getBoundingClientRect();
      return 'translate(' + (a.left - b.left) + 'px, ' + (a.top - b.top) + 'px) scale(' + a.width / b.width + ')';
    };

    var open = function (shot) {
      thumb = shot.querySelector('img');
      lbImg.src = thumb.currentSrc || thumb.src;
      lbImg.alt = thumb.alt;
      lightbox.hidden = false;
      document.documentElement.classList.add('lb-open');
      lbClose.focus({ preventScroll: true });
      try { history.pushState({ cairnLightbox: true }, ''); } catch (err) {}
      if (reduce || !lbImg.animate) return;
      var run = function () {
        lbImg.animate([{ transform: offsetFrom(thumb) }, { transform: 'none' }], { duration: 420, easing: 'cubic-bezier(.2,.8,.2,1)' });
        lbBg.animate([{ opacity: 0 }, { opacity: 1 }], { duration: 320, easing: 'ease-out' });
        lbClose.animate([{ opacity: 0, transform: 'scale(.6)' }, { opacity: 1, transform: 'none' }], { duration: 320, delay: 120, easing: 'cubic-bezier(.34,1.56,.64,1)', fill: 'backwards' });
      };
      if (lbImg.complete) run(); else lbImg.onload = function () { lbImg.onload = null; run(); };
    };

    var close = function (fromHistory) {
      if (lightbox.hidden || closing) return;
      closing = true;
      var done = function () {
        lightbox.hidden = true;
        document.documentElement.classList.remove('lb-open');
        closing = false;
        if (thumb) thumb.closest('.shot').focus({ preventScroll: true });
      };
      if (!fromHistory && history.state && history.state.cairnLightbox) {
        try { history.back(); } catch (err) {}
      }
      if (reduce || !lbImg.animate) return done();
      var anim = lbImg.animate([{ transform: 'none' }, { transform: offsetFrom(thumb) }], { duration: 320, easing: 'cubic-bezier(.4,0,.2,1)', fill: 'forwards' });
      lbBg.animate([{ opacity: 1 }, { opacity: 0 }], { duration: 300, easing: 'ease-in', fill: 'forwards' });
      lbClose.animate([{ opacity: 1 }, { opacity: 0 }], { duration: 160, fill: 'forwards' });
      anim.onfinish = function () {
        done();
        lightbox.getAnimations && lightbox.getAnimations({ subtree: true }).forEach(function (a) { a.cancel(); });
      };
    };

    shots.forEach(function (shot) {
      shot.addEventListener('click', function () {
        if (row.classList.contains('zoomable')) open(shot);
      });
      shot.addEventListener('keydown', function (e) {
        if (row.classList.contains('zoomable') && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault();
          open(shot);
        }
      });
    });
    lightbox.addEventListener('click', function () { close(false); });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') close(false);
    });
    window.addEventListener('popstate', function () { close(true); });
  }

  // FAQ works as an accordion: opening one answer closes the others. Every click takes over from
  // whatever animation is running, so fast clicking never leaves two answers open.
  var faqs = Array.prototype.slice.call(document.querySelectorAll('.faq details'));
  var running = [];
  function setFaq(d, open) {
    var i = faqs.indexOf(d);
    var body = d.querySelector('.faq-a');
    d.classList.toggle('is-open', open);
    if (reduce || !body.animate) {
      d.open = open;
      return;
    }
    // start from the height it has on screen right now, even halfway through an animation
    var from = d.open ? body.getBoundingClientRect().height : 0;
    var fromOpacity = d.open ? parseFloat(getComputedStyle(body).opacity) : 0;
    if (running[i]) running[i].cancel();
    d.open = true;
    var to = open ? body.getBoundingClientRect().height : 0;
    var anim = body.animate(
      { height: [from + 'px', to + 'px'], opacity: [fromOpacity, open ? 1 : 0] },
      { duration: open ? 380 : 300, easing: open ? 'cubic-bezier(.2,.8,.2,1)' : 'cubic-bezier(.4,0,.2,1)' }
    );
    running[i] = anim;
    anim.onfinish = function () {
      running[i] = null;
      if (!open) d.open = false;
    };
  }
  faqs.forEach(function (d) {
    d.querySelector('summary').addEventListener('click', function (e) {
      e.preventDefault();
      var open = !d.classList.contains('is-open');
      if (open) faqs.forEach(function (other) { if (other !== d && other.classList.contains('is-open')) setFaq(other, false); });
      setFaq(d, open);
    });
  });

  // copy buttons on the terminals
  function fallbackCopy(text) {
    var t = document.createElement('textarea');
    t.value = text;
    t.setAttribute('readonly', '');
    t.style.position = 'fixed';
    t.style.opacity = '0';
    document.body.appendChild(t);
    t.select();
    try { document.execCommand('copy'); } catch (err) {}
    document.body.removeChild(t);
  }
  each('[data-copy]', function (btn) {
    btn.addEventListener('click', function () {
      var term = btn.closest('.term');
      var text = Array.prototype.map.call(term.querySelectorAll('.cmd'), function (c) {
        return c.getAttribute('data-text') || c.textContent;
      }).join('\n');
      function copied() {
        btn.textContent = 'Copied';
        btn.classList.add('ok');
        setTimeout(function () { btn.textContent = 'Copy'; btn.classList.remove('ok'); }, 1600);
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(copied, function () { fallbackCopy(text); copied(); });
      } else {
        fallbackCopy(text);
        copied();
      }
    });
  });
})();
