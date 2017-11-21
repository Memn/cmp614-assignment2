import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReducedWord2WordMatrixBuilder extends AbstractMatrixBuilder {

    private static final ReducedWord2WordMatrixBuilder instance = new ReducedWord2WordMatrixBuilder();

    public static ReducedWord2WordMatrixBuilder getInstance() {
        return instance;
    }

    public float[][] build(String wiki, Map<String, Integer> vocabulary, int k, float epsilon) throws IOException {
        float[][] reduced = new float[vocabulary.size()][k];

        float x = (float) (Math.sqrt(k) / epsilon);
        float oneProbability = epsilon / (2 * k);

        float[][] randomVectors = generateRandomVectors(vocabulary, x, generateRandomIV(oneProbability), k);

        int windowSize = 5;

        try (LineIterator it = FileUtils.lineIterator(new File(wiki), "UTF-8")) {
            while (it.hasNext()) {
                String[] corpus = StringUtils.split(it.nextLine());

                if (corpus.length < 1) {
                    continue;
                }

                for (int i = 0; i < corpus.length; i++) {

                    updateOccurrencesInWindow(vocabulary, reduced, corpus, i, windowSize, randomVectors);

                }

            }
        }
        return reduced;
    }

    private float[][] generateRandomVectors(Map<String, Integer> vocabulary,
                                            float x,
                                            short[] randomIV,
                                            int size) {
        float[][] randomVectors = new float[vocabulary.size()][];
        vocabulary.forEach((word, index) -> randomVectors[index] = generateRandomVector(x, randomIV, size));
        return randomVectors;
    }

    void updateOccurrencesInWindow(Map<String, Integer> vocabulary,
                                   float[][] reduced,
                                   String[] corpus,
                                   int self,
                                   int windowSize,
                                   float[][] randomVectors) {
        updateOccurrencesInWindow(vocabulary, reduced, corpus, self - windowSize, self + windowSize, self, randomVectors);

    }

    private void updateOccurrencesInWindow(Map<String, Integer> vocabulary,
                                           float[][] reduced,
                                           String[] corpus,
                                           int start,
                                           int end,
                                           int self,
                                           float[][] randomVectors) {
        if (start < 0) {
            start = 0;
        }
        if (end >= corpus.length) {
            end = corpus.length - 1;
        }
        for (int i = start; i <= end; i++) {
            if (i != self) {
                Integer selfIndex = vocabulary.get(corpus[self]);
                Integer theOtherIndex = vocabulary.get(corpus[i]);
                updateMatrix(reduced[selfIndex], randomVectors[theOtherIndex]);
            }
        }
    }

    private void updateMatrix(float[] wordVector, float[] randomVector) {
        for (int i = 0; i < randomVector.length; i++) {
            wordVector[i] = wordVector[i] + randomVector[i];
        }
    }

}
