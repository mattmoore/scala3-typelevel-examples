package containers

import cats.syntax.all.*
import com.dimafeng.testcontainers.JdbcDatabaseContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresContainer {
  def apply(): PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:latest"),
    databaseName = "testcontainer-scala",
    username = "scala",
    password = "password",
    commonJdbcParams = JdbcDatabaseContainer.CommonParams(
      initScriptPath = "database/INIT.sql".some,
    ),
  )
}
