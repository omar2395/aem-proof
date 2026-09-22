/**
 * API status: turn ISO timestamps into "منذ ٣ دقائق"-style relative text without losing the
 * machine-readable datetime attribute. Uses Intl.RelativeTimeFormat in the page language.
 */
(function () {
    'use strict';

    function relative(iso, lang) {
        var then = Date.parse(iso);
        if (isNaN(then) || typeof Intl === 'undefined' || !Intl.RelativeTimeFormat) {
            return null;
        }
        var rtf = new Intl.RelativeTimeFormat(lang, { numeric: 'auto' });
        var diff = (then - Date.now()) / 1000;
        var units = [['day', 86400], ['hour', 3600], ['minute', 60], ['second', 1]];
        for (var i = 0; i < units.length; i++) {
            if (Math.abs(diff) >= units[i][1] || units[i][0] === 'second') {
                return rtf.format(Math.round(diff / units[i][1]), units[i][0]);
            }
        }
        return null;
    }

    function init() {
        var lang = document.documentElement.lang || 'ar';
        document.querySelectorAll('.cmp-api-status time[datetime]').forEach(function (t) {
            var text = relative(t.getAttribute('datetime'), lang);
            if (text) {
                t.setAttribute('title', t.getAttribute('datetime'));
                t.textContent = text;
            }
        });
    }

    if (document.readyState !== 'loading') {
        init();
    } else {
        document.addEventListener('DOMContentLoaded', init);
    }
})();
