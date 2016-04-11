sb-webpack
==========

[sbt-web] plugin for bundling web assets using webpack.

Add plugin
----------

Add the plugin to `project/plugins.sbt`.

```scala
addSbtPlugin("com.sc.sbt" % "sbt-webpack" % "1.0.0")
```

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

    lazy val root = (project.in file(".")).enablePlugins(SbtWeb)

As with all sbt-web asset pipeline plugins you must declare their order of execution:

```scala
pipelineStages := Seq(webpack)
```

Add webpack as a devDependancy to your package.json file (located at the root of your project):
```json
{
  "devDependencies": {
    "webpack": "^1.9.11"
  }
}
```

Configuration
-------------

```scala
webpackConfig in webpack := [location of config file]
```
(if not set, defaults to baseDirectory / "webpack.config.js")


Contribution policy
-------------------

Contributions via GitHub pull requests are gladly accepted from their original
author.


License
-------

This code is licensed under the [Apache 2.0 License][apache].


[sbt-web]: https://github.com/sbt/sbt-web
[apache]: http://www.apache.org/licenses/LICENSE-2.0.html
