package model.pipelines.unsupervised

object test {
    def main(args: Array[String]): Unit ={
        val input_features = List("num_file_creations", "root_shell")
        print(input_features.zipWithIndex.map{case(e, i) =>
            "(dense['" + e + "']-feature_mean[" + (i+1) + "])/feature_std[" + (i+1) + "]"}.mkString(","))
        print(input_features.map(x => "AVG(dense['" + x + "'])").mkString(","))
    }
}
