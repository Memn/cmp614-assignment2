import java.io.IOException;
import java.util.Map;

public class ReducedMatrixBuilder {
    public static float[][] build(String wiki,
                                  Map<String, Integer> vocabulary,
                                  int k,
                                  float epsilon,
                                  boolean word2Word) throws IOException {
        if (word2Word) {
            System.err.println("Word2Word iomplementation has some errors.");
            System.err.println("Word2Document will be runned instead");
        }
        return ReducedWord2DocumentMatrixBuilder.getInstance().build(wiki, vocabulary, k, epsilon);
    }
}
