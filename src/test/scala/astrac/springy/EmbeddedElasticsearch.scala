package astrac.springy

import java.nio.file.Files
import org.apache.commons.io.FileUtils
import org.elasticsearch.client.Client
import org.elasticsearch.client.Requests
import org.elasticsearch.common.Priority
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.node.NodeBuilder._
import scala.util.Random

class EmbeddedElasticsearch(port: Int) {

  private val clusterName = "elastic_http_" + Random.nextInt(1000)
  private val dataDir = Files.createTempDirectory("elasticsearch_data_").toFile
  private val settings = ImmutableSettings.settingsBuilder
    .put("path.data", dataDir.toString)
    .put("cluster.name", clusterName)
    .put("http.enabled", true)
    .put("http.port", port)
    .put("index.store.type", "memory")
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
        .waitForRelocatingShards(0)).actionGet

    if (actionGet.isTimedOut) sys.error("The ES cluster didn't go green within the extablished timeout")
  }

  def stop(): Unit = {
    node.close()

    FileUtils.deleteDirectory(dataDir)
  }

  def createAndWaitForIndex(index: String): Unit = {
    client.admin.indices.prepareCreate(index).execute.actionGet()
    client.admin.cluster.prepareHealth(index).setWaitForActiveShards(1).execute.actionGet()
  }
}
