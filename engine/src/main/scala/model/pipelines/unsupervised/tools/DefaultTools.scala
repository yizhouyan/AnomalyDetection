package model.pipelines.unsupervised.tools

object DefaultTools {
    implicit class ArrayExtension[T <% Ordered[T]](val array: Array[T]) {
        def argSort = {
            array.zipWithIndex.sortBy(_._1).map(_._2).toArray
        }
    }
}
