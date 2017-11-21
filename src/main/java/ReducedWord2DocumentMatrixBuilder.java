import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

class ReducedWord2DocumentMatrixBuilder extends AbstractMatrixBuilder {
    private static final ReducedWord2DocumentMatrixBuilder instance = new ReducedWord2DocumentMatrixBuilder();

    static ReducedWord2DocumentMatrixBuilder getInstance() {
        return instance;
    }

    float[][] build(String wikiArticlesFilePath,
                    Map<String, Integer> vocabulary,
                    int k,
                    float epsilon) throws IOException {

        float x = (float) (Math.sqrt(k) / epsilon);
        float oneProbability = epsilon / (2 * k);

        float[][] reduced = new float[vocabulary.size()][k]; //sparse

        try (LineIterator it = FileUtils.lineIterator(new File(wikiArticlesFilePath), "UTF-8")) {
            while (it.hasNext()) {

                PatriciaTrie<Integer> wordCounts = countWordsIfInVocabulary(it.nextLine(), vocabulary);

                if (wordCounts.size() == 0) {
                    continue;
                }


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


}
