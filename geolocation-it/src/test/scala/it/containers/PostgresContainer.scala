package geolocation.it.containers

import cats.syntax.all.*
import com.dimafeng.testcontainers.JdbcDatabaseContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresContainer {
  def apply(): PostgreSQLContainer.Def =
    PostgreSQLContainer.Def(
      dockerImageName = DockerImageName
        .parse("postgis/postgis:latest")
        .asCompatibleSubstituteFor("postgres"),
      databaseName = "geolocation",
      username = "scala",
      password = "scala",
      commonJdbcParams = JdbcDatabaseContainer.CommonParams(
        initScriptPath = "database/init.sql".some,
      ),
    )
}
