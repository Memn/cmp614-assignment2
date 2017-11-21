import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

class ToeflQuestionAnswerer {

    private static final ToeflQuestionAnswerer instance = new ToeflQuestionAnswerer();
    private static int guessed = 0;

    static ToeflQuestionAnswerer getInstance() {
        return instance;
    }

    static int getGuessed() {
        return guessed;
    }

    List<String[]> readToeflQuestions(String toeflQuestionsFilePath) throws IOException {
        List<String[]> toeflQuestions = new ArrayList<>();
        try (LineIterator it = FileUtils.lineIterator(new File(toeflQuestionsFilePath), "UTF-8")) {
            while (it.hasNext()) {
                String[] question = StringUtils.split(it.nextLine(), '|');
                if (question.length == 5) { // 1 question + 4 choices
                    toeflQuestions.add(question);
                } else {
                    System.err.println("Wrong size of question: " + Arrays.toString(question));
                }
            }
        }
        return toeflQuestions;
    }

    int[] answerToeflQuestions(float[][] reduced,
                               List<String[]> toeflQuestions,
                               Map<String, Integer> vocabulary) throws IOException {

        int[] answers = new int[toeflQuestions.size()];
        int i = 0;
        for (String[] question : toeflQuestions) {
            answers[i++] = answerQuestion(question, reduced, vocabulary);
        }

        return answers;
    }

    private int answerQuestion(String[] question, float[][] reduced, Map<String, Integer> vocabulary) {
        float highestSimilarity = Float.MIN_VALUE;
        int choice = Integer.MIN_VALUE;
        if (vocabulary.get(question[0]) == null) {
            return makeRandomGuess();
        }
        for (int i = 1; i < 5; i++) {
            float similarity;
            if (vocabulary.get(question[i]) == null) {
                similarity = Float.MIN_VALUE;
            } else {
                similarity = calculateSimilarity(vocabulary.get(question[0]), vocabulary.get(question[i]), reduced);
            }
            if (highestSimilarity < similarity) {
                highestSimilarity = similarity;
                choice = i;
            }
        }
        return choice == Integer.MIN_VALUE ? makeRandomGuess() : choice;
    }

    int makeRandomGuess() {
        guessed++;
        return new Random().nextInt(4) + 1;
    }

    private float calculateSimilarity(int question,
                                      int choice,
                                      float[][] reduced) {

        return Math.max(cosineSimilarity(reduced[question], reduced[choice]), 0);
    }

    private float cosineSimilarity(float[] vectorA, float[] vectorB) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}
