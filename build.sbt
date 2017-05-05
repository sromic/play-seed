name := "play-seed"

version := "1.0"

lazy val `playseed` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

packageName in Universal := "play-seed"

libraryDependencies ++= Seq(
	cache,
	evolutions,
	specs2                                                                                  % Test,
	filters,
	"mysql"               % "mysql-connector-java"                    % "6.0.6",
	"com.typesafe.play"   % "play-slick_2.11"                         % "2.1.0",
	"com.typesafe.play"   % "play-slick-evolutions_2.11"              % "2.1.0",
	"com.mohiva"          % "play-silhouette_2.11"                    % "4.0.0",
	"com.mohiva"          % "play-silhouette-testkit_2.11"            % "4.0.0"             % Test,
	"com.mohiva"          % "play-silhouette-password-bcrypt_2.11"    % "4.0.0",
	"com.mohiva"          %% "play-silhouette-crypto-jca"             % "4.0.0",
	"com.mohiva"          %% "play-silhouette-persistence"            % "4.0.0",
	"org.webjars"         %% "webjars-play"                           % "2.5.0-2",
	"com.adrianhurt"      %% "play-bootstrap"                         % "1.0-P25-B3",
	"net.codingwell"      %% "scala-guice"                            % "4.0.1",
	"net.ceedubs"         % "ficus_2.11"                              % "1.1.2"
)

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

routesImport += "utils.route.Binders._"

scalacOptions ++= Seq(
	"-deprecation", // Emit warning and location for usages of deprecated APIs.
	"-feature", // Emit warning and location for usages of features that should be imported explicitly.
	"-unchecked", // Enable additional warnings where generated code depends on assumptions.
	"-Xfatal-warnings", // Fail the compilation if there are any warnings.
	"-Xlint", // Enable recommended additional warnings.
	"-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
	"-Ywarn-dead-code", // Warn when dead code is identified.
	"-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
	"-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
	"-Ywarn-numeric-widen" // Warn when numerics are widened.
)

// Docker settings
maintainer in Docker := "template"
packageName in Docker := "play-seed"