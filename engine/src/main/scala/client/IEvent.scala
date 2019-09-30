package client

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/30/19.
  */
trait IEvent {
    def getName(): String
    def getHyperParameters():mutable.Map[Any, Any]
}
