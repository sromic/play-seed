addSbtPlugin("com.typesafe.play"    % "sbt-plugin"          % "2.5.14")

//gzip
addSbtPlugin("com.typesafe.sbt"     % "sbt-gzip"            % "1.0.0")

//scala formatter
addSbtPlugin("com.geirsson"         % "sbt-scalafmt"        % "0.6.8")

// SBT Plugin to load environment variables from .env into the JVM System Environment
addSbtPlugin("au.com.onegeek"       %% "sbt-dotenv"         % "1.1.36")

addSbtPlugin("com.typesafe.sbt"     % "sbt-native-packager" % "1.1.4")