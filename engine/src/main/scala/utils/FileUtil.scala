package utils

import java.io.File

/**
  * Created by yizhouyan on 9/12/19.
  */
object FileUtil {
    def getRecursiveListOfFiles(dir: File): Array[String] = {
        val these = dir.listFiles
        these.filter(_.isFile).map(dir + "/" + _.getName) ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
    }
}
