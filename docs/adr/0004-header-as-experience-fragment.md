# ADR 0004 — Header is an Experience Fragment referenced from the template structure

Status: accepted · 2026-09-22

The header lives once, in `/content/experience-fragments/aemproof/sa/ar/site/header/master`,
and the `page-content` template's structure references it through
`aemproof/components/experiencefragment` (super type: Core Components Experience Fragment v2).
The structure node is not marked `editable`, so page authors cannot remove or replace it; only
template editors decide its placement, and content authors change the header in one place.

This is the standard Cloud Service pattern and keeps localisation open: a `/sa/en` site would
get its own header variation without touching the template.

Right-to-left rendering is done in CSS (`html:lang(ar) { direction: rtl }`) rather than by
overriding the Core Components page scripts, so the page component stays a pure proxy.
