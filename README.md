# river-core

A collection of Boot tasks to support [Perun](https://github.com/hashobject/perun) 

## Usage

FIXME: explanation

Run the `river-core-pre` task:

    $ boot river-core-pre

To use this in your project, add `[river-core "0.1.0-SNAPSHOT"]` to your `:dependencies`
and then require the task:

    (require '[river-core.core :refer [river-core-pre]])

Other tasks include: `river-core-simple`, `river-core-post`, `river-core-pass-thru`.

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
