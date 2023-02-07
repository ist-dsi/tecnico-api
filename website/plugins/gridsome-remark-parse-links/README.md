# gridsome-remark-parse-links

Since links in the documentation need to work both on GitLab and on Gridsome, the `.md` extension needs to be removed for relative links.

This remark plugin takes care of that.  
This plugin also converts any relative paths into absolute root paths (without the domain).
