/**
 * Offers grid: reveal cards as they enter the viewport. The stagger comes from the
 * data-index the HTL prints; this script only toggles the class. Falls back to visible.
 */
(function () {
    'use strict';

    var ITEM = '.cmp-offers-grid__item';

    function reveal(items) {
        if (!('IntersectionObserver' in window)) {
            items.forEach(function (el) { el.classList.add('is-visible'); });
            return;
        }
        var io = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    io.unobserve(entry.target);
                }
            });
        }, { rootMargin: '0px 0px -8% 0px', threshold: 0.12 });
        items.forEach(function (el) { io.observe(el); });
        // Safety net: never leave a card hidden (print, prerender, very tall grids).
        window.setTimeout(function () {
            items.forEach(function (el) { el.classList.add('is-visible'); });
        }, 1800);
    }

    function init() {
        reveal(Array.prototype.slice.call(document.querySelectorAll(ITEM)));
    }

    if (document.readyState !== 'loading') {
        init();
    } else {
        document.addEventListener('DOMContentLoaded', init);
    }

    // Re-run when the AEM editor refreshes a component (REFRESH_SELF in _cq_editConfig)
    if (window.Granite && window.Granite.author && window.Granite.author.MessageChannel) {
        document.addEventListener('cq-editables-updated', init);
    }
})();
