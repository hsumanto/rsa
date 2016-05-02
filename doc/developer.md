# Development

> This document contains instructions for building and running RSA for
> development. If you are setting up a production environment, see
> [`production.md`](production.md).

Before building, it's a good idea to start
[`docker-proxy` with SSL support][dp]: this allows caching of libraries
used by Gradle. The rsa's build script has special support for `docker-proxy`
to allow caching of SSL requests.

Use [docker-compose][dc] to build the RSA.

```
sudo docker-compose build
```

The following services are provided:

 - `postgres`: A metadata store instance for the RSA.
 - `data`: A data-only container with volumes for tile storage.
 - `dev`: A development environment that drops you into a shell in the source
    directory.
 - `rsa`: A runtime environment that lets you test the RSA.

Start a new local RSA cluster with:

```
sudo docker-compose up rsa seed
sudo docker-compose up rsa worker
sudo docker-compose up rsa worker
sudo docker-compose up rsa web
```

Start as many workers as you like, but make sure you have at least two: the
first one doesn't do any actual work. All of the cluster nodes will write store
data in the `postgres` and `data` containers. They have volumes mapped to the
live source directory, so any changes you make to your code will show up in the
running instances (perhaps after a compile and restart).

To develop new code, start the dev environment:

```
sudo docker-compose run --rm dev
```

Then you can build with [Gradle]. For example, to start a [continuous build][cb]
of the query engine:

```
cd rsaquery
gradle --daemon --continuous compileJava
```

After the first dev build, you will have `.classpath.txt` files in your source
directory. These can be used for autocompletion, e.g. using [Atom's
autocomplete-java package][aj].

[dp]: https://github.com/silarsis/docker-proxy
[dc]: https://docs.docker.com/compose/
[Gradle]: https://gradle.org/
[cb]: https://docs.gradle.org/current/userguide/continuous_build.html
[aj]: https://atom.io/packages/autocomplete-java
