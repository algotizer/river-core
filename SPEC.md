# River Core SPEC

This document defines metadata used by River Core provided perun tasks and
 function.

- **Set by** fields are set by the mentioned tasks.
- **Required by** fields are mandatory. Global metadata can alternatively be
set for the tasks using task options.
- **Used by** fields are optional.

## Global metadata

- **:languages**
    - Required by: *lang* a set of languages which site will be generated, the first
    language would be used as default language for generating redirect page.

## Resources metadata (Post, Page etc)

All resources have a filename which is used as a key to identify the resource.
A type is used to identify the resource handling

- **:lang**
    - Used by: *permalink* for generating locale related link
- **:type**
    - Required by: *atom-feed*
    - Used by: *rss* either this or description is required
- **:prev**
    - Set by: *prev-next*
    - Used by:
        - *collection*
        - *render*
- **:next**
    - Set by: *prev-next*
    - Used by:
        - *collection*
        - *render*

## Page metadata

Page is a resource with **:type** `page`. They don't need to access all resources
info, but have a own renderer. Aggregated resources' info may append to
page metadata using other tasks.

- **:renderer**
    - Required by: *render-page* with `render-renderer` to have different renderer
    each page.
- **:renderer-deps**
    - Used by: *render-page* with `render-renderer` to have different render
    dependencies each page.
- **:content**
    - Set by: *markdown*
    - Used by: *render*
