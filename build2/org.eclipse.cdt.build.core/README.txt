This is the core of the new CDT build system.

The objective is to provide an extendable framework that can adapt standard build
functionality for different toolchains, as well as provide a reusable framework
for controlling external builds for different managed build systems.

The standard external build system that includes:
- external build command manager and build output stream management
- error parser framework
- build output parsing for scanner discovery
- scanner info provider

We reuse the following build functionality from cdt.core:
- build console
