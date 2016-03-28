package astrac.springy

package object elasticsearch {
  val UnsafeClient = new IndexDocumentSupport
      with GetDocumentSupport
      with UpdateDocumentSupport
      with DeleteDocumentSupport
      with SearchSupport {}
}
