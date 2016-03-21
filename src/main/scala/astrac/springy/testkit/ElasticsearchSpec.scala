package astrac.springy.testkit

import astrac.springy.Executor
import org.scalatest.time.Millis
import org.scalatest.time.Span
import org.scalatest.{ Assertions, BeforeAndAfterAll, BeforeAndAfterEach, Suite }
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }

trait ElasticsearchSpec extends Assertions with AsyncAssertions with ScalaFutures with BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  override implicit def patienceConfig = PatienceConfig(timeout = Span(30000, Millis), interval = Span(250, Millis))

  val esPort = 12000 + (Thread.currentThread.getId % 100).toInt

  val es: EmbeddedElasticsearch = new EmbeddedElasticsearch(esPort)
  val springy: Executor.JavaApi = new Executor.JavaApi(es.client)

  def commitEs() = es.client.admin.indices.prepareRefresh().execute.actionGet

  override def beforeAll(): Unit = {
    super.beforeAll()
    es.start()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    es.stop()
  }
}
