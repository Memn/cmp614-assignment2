import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

class ReducedMatrixBuilder {
    private static final ReducedMatrixBuilder instance = new ReducedMatrixBuilder();

    static ReducedMatrixBuilder getInstance() {
        return instance;
    }

    float[][] build(String wikiArticlesFilePath,
                    Map<String, Integer> vocabulary,
                    int k,
                    float epsilon) throws IOException {


        float[][] reduced = new float[vocabulary.size()][k]; //sparse

        try (LineIterator it = FileUtils.lineIterator(new File(wikiArticlesFilePath), "UTF-8")) {
            while (it.hasNext()) {

                PatriciaTrie<Integer> wordCounts = countWordsIfInVocabulary(it.nextLine(), vocabulary);

                if (wordCounts.size() == 0) {
                    continue;
                }

                float x = (float) (Math.sqrt(k) / epsilon);
                float oneProbability = epsilon / (2 * k);

                float[] randomVector = generateRandomVector(x, generateRandomIV(oneProbability), k);

                wordCounts.forEach((word, count) -> updateReducedMatrix(reduced, vocabulary.get(word), count, randomVector));

            }
        }
        return reduced;

    }

    private PatriciaTrie<Integer> countWordsIfInVocabulary(String document, Map<String, Integer> vocabulary) {
        PatriciaTrie<Integer> trie = new PatriciaTrie<>();
        String[] corpus = StringUtils.split(document);

        if (corpus.length < 1) {
            return trie;
        }

        for (String word : corpus) {

            if (vocabulary.containsKey(word)) {
                trie.put(word, trie.getOrDefault(word, 0) + 1);
            }

        }

        return trie;
    }

    float[] generateRandomVector(float x, short[] randomIV, int size) {
        float[] vector = new float[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            vector[i] = x * randomIV[rand.nextInt(randomIV.length)];
        }

        return vector;
    }

    short[] generateRandomIV(float oneProbability) {
        short[] iv = new short[100];
        int numOnes = (int) (oneProbability * 100);

        for (int i = 0; i < 100; i++) {
            if (i >= 2 * numOnes) {
                iv[i] = (short) 0;
            } else {
                if (i >= numOnes) {
                    iv[i] = (short) -1;
                } else {
                    iv[i] = (short) 1;
                }
            }
        }

        return iv;
    }

    private void updateReducedMatrix(float[][] reduced,
                                     int wordIndex,
                                     int termOccurrences,
                                     float[] randomVector) {
        for (int i = 0; i < randomVector.length; i++) {
            reduced[wordIndex][i] = reduced[wordIndex][i] + (termOccurrences * randomVector[i]);
        }
    }


}
