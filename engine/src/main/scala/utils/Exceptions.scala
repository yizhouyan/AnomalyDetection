package utils

final case class FileNameNotSetException(private val message: String = "",
                                         private val cause: Throwable = None.orNull)
        extends Exception(message, cause)

final case class FileTypeNotSupportedException(private val message: String = "",
                                               private val cause: Throwable = None.orNull)
        extends Exception(message, cause)

final case class NoFileUnderInputFolderException(private val message: String = "",
                                                 private val cause: Throwable = None.orNull)
        extends Exception(message, cause)