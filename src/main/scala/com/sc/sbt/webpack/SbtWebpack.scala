package com.sc.sbt.webpack

import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.jse.JsTaskImport.JsTaskKeys.timeoutPerSource
import com.typesafe.sbt.jse.SbtJsTask
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.jse.SbtJsEngine.autoImport.JsEngineKeys.{command, engineType, npmNodeModules}
import sbt.Keys.{resourceManaged, streams, baseDirectory, state}
import SbtWeb.autoImport.{WebKeys, Plugin, Assets}
import WebKeys.{webTarget, webJarsNodeModules, nodeModuleDirectories}
import sbt._

object Import {
  val webpack = TaskKey[Pipeline.Stage]("webpack", "Run webpack in the asset pipeline.")
  lazy val webpackConfig = SettingKey[Option[File]]("webpackConfig", "The location of a webpack configuration file.")
}

object SbtWebpack extends AutoPlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  import Import.{webpack, webpackConfig}

  override def projectSettings = Seq(
    webpackConfig in webpack := None,
    resourceManaged in webpack := webTarget.value / webpack.key.label,
    webpack := runWebpack.dependsOn(webJarsNodeModules in Plugin).dependsOn(npmNodeModules in Assets).value
  )
  
  def runWebpack: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>
        val configFile = if((webpackConfig in webpack).value == None) baseDirectory.value / "webpack.config.js" else (webpackConfig in webpack).value.get
        val webpackjsShell = baseDirectory.value / "node_modules" / "webpack" / "bin" / "webpack.js"
        val outputDir = (resourceManaged in webpack).value
      
        streams.value.log.info("Bundling assets with Webpack")
        
        SbtWeb.syncMappings(
            streams.value.cacheDirectory,
            mappings,
            outputDir
        )
        
        val cacheDirectory = streams.value.cacheDirectory / webpack.key.label
        
        val runUpdate = FileFunction.cached(cacheDirectory, FilesInfo.hash) {
        _ =>
            SbtJsTask.executeJs(
            state.value,
            (engineType in webpack).value,
            (command in webpack).value,
            (nodeModuleDirectories in Plugin).value.map(_.getPath),
            webpackjsShell,
            Seq("--output-path", outputDir.getAbsolutePath, "--config", configFile.getAbsolutePath), 
            (timeoutPerSource in webpack).value * mappings.size)
        
            outputDir.***.get.toSet
       }

       runUpdate(outputDir.***.get.toSet).filter(_.isFile).pair(relativeTo(outputDir)).toSeq
  }
  
}
