package client

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/30/19.
  */
trait IModel {
    def getName(): String
    def getFilePath(): String
}
