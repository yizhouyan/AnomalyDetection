package model.utils

import java.util.UUID

object Utils {
    def getRandomFilePath(pathPrefix: String="", filePrefix: String=""): String ={
        java.nio.file.Paths.get(pathPrefix, filePrefix + "_" + UUID.randomUUID()).toString
    }
}
