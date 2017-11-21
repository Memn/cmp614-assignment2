import java.io.IOException;
import java.util.Map;
import java.util.Random;

public abstract class AbstractMatrixBuilder {
    abstract float[][] build(String wiki, Map<String, Integer> vocabulary, int k, float epsilon) throws IOException;

    protected void updateReducedMatrix(float[][] reduced,
                                       int wordIndex,
                                       int termOccurrences,
                                       float[] randomVector) {
        for (int i = 0; i < randomVector.length; i++) {
            reduced[wordIndex][i] = reduced[wordIndex][i] + (termOccurrences * randomVector[i]);
        }
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
}
