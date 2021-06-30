
resolvers += Classpaths.typesafeReleases
resolvers += Resolver.mavenCentral
resolvers += Resolver.jcenterRepo
resolvers += "jitpack" at "https://jitpack.io"

dependsOn(RootProject(hgRoot / "fud14.1.pure-generator-plugin.sbt"))
