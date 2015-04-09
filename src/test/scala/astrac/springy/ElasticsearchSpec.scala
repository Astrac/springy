package astrac.springy

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.scalatest.time.Millis
import org.scalatest.time.Span
import org.scalatest.{ Assertions, BeforeAndAfterAll, BeforeAndAfterEach, Suite }
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }

trait ElasticsearchSpec extends Assertions with AsyncAssertions with ScalaFutures with BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  override implicit def patienceConfig = PatienceConfig(timeout = Span(30000, Millis), interval = Span(250, Millis))

  val esPort = 12000 + (Thread.currentThread.getId % 100).toInt

  var es: EmbeddedElasticsearch = _
  var executor: Executor.JavaApi = _

  override def beforeAll() {
    super.beforeAll()
    es = new EmbeddedElasticsearch(esPort)
    es.start()

    executor = new Executor.JavaApi(es.client)
  }

  override def afterAll() {
    es.stop()
  }
}
