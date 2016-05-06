package astrac.springy.testkit

import java.io.{File, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import org.elasticsearch.client.Client
import org.elasticsearch.client.Requests
import org.elasticsearch.common.Priority
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.node.NodeBuilder._
import scala.util.Random

class EmbeddedElasticsearch(port: Int) {

  private val clusterName = "elastic_http_" + Random.nextInt(1000)
  private val dataDir = Files.createTempDirectory("elasticsearch_data_").toFile
  private val settings = Settings.settingsBuilder
    .put("path.home", dataDir.toString)
    .put("cluster.name", clusterName)
    .put("http.enabled", true)
    .put("http.port", port)
    .put("index.number_of_shards", 1)
    .put("index.number_of_replicas", 0)
    .put("discovery.zen.ping.multicast.enabled", false)
    .build

  private lazy val node = nodeBuilder().local(true).settings(settings).build
  def client: Client = node.client

  def start(): Unit = {
    node.start()

    val actionGet = client.admin.cluster.health(
      Requests
        .clusterHealthRequest("_all")
        .timeout(TimeValue.timeValueSeconds(30))
        .waitForGreenStatus()
        .waitForEvents(Priority.LANGUID)
        .waitForRelocatingShards(0)
    ).actionGet

    if (actionGet.isTimedOut) sys.error("The ES cluster didn't go green within the extablished timeout")
  }

  def stop(): Unit = {
    node.close()
    deleteDirectory(dataDir)
  }

  def deleteDirectory(dir: File): Unit = {
    Files.walkFileTree(dir.toPath(), new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })

    ()
  }

  def createAndWaitForIndex(index: String): Unit = {
    client.admin.indices.prepareCreate(index).execute.actionGet()
    client.admin.cluster.prepareHealth(index).setWaitForActiveShards(1).execute.actionGet()
  }
}
