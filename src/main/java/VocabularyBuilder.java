import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

class VocabularyBuilder {

    private static final int VOCABULARY_SIZE = 50000;
    private static final int MIN_OCCURRENCES = 50;

    private static final VocabularyBuilder instance = new VocabularyBuilder();

    static VocabularyBuilder getInstance() {
        return instance;
    }

    Map<String, Integer> build(String wikiArticlesFilePath) throws IOException {
        return build(wikiArticlesFilePath, VOCABULARY_SIZE, MIN_OCCURRENCES);
    }

    private Map<String, Integer> build(String wikiArticlesFilePath,
                                       int vocabularySize,
                                       int minOccurrences) throws IOException {
        PatriciaTrie<Integer> vocabulary = new PatriciaTrie<>();
        try (LineIterator it = FileUtils.lineIterator(new File(wikiArticlesFilePath), "UTF-8")) {
            while (it.hasNext()) {

                PatriciaTrie<Integer> wordCounts = countWords(it.nextLine());
                for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
                    String word = entry.getKey();
                    Integer occurrences = entry.getValue();
                    vocabulary.put(word, vocabulary.getOrDefault(word, 0) + occurrences);
                }

            }
        }
        return filterVocabulary(vocabularySize, minOccurrences, vocabulary);
    }

    private PatriciaTrie<Integer> countWords(String document) {
        PatriciaTrie<Integer> trie = new PatriciaTrie<>();
        String[] corpus = StringUtils.split(document);

        if (corpus.length < 1) {
            return trie;
        }

        for (String word : corpus) {

            trie.put(word, trie.getOrDefault(word, 0) + 1);

        }

        return trie;
    }

    private Map<String, Integer> filterVocabulary(int vocabularySize,
                                                  int minOccurrences,
                                                  PatriciaTrie<Integer> vocabulary) {
        vocabulary.values().removeIf(value -> (value < minOccurrences));
        vocabulary.keySet().removeIf(key -> StringUtils.containsAny(key, "0123456789"));
        vocabulary.keySet().removeIf(this::isAllCharsSame);
        Map<String, Integer> collected = vocabulary.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(vocabularySize)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // word to index map
        int index = 0;
        for (String word : collected.keySet()) {
            collected.put(word, index++);
        }

        return collected;
    }

    private boolean isAllCharsSame(String key) {
        if (key.length() != 0) {
            char first = key.charAt(0);
            for (char c : key.toCharArray()) {
                if (first != c) {
                    return false;
                }
            }
        }
        return true;
    }


}
