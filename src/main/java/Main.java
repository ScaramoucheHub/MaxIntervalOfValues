import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args)  {
        String[] texts = new String[25];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = generateText("aab", 30_000);
        }

        ExecutorService executorService = Executors.newWorkStealingPool();

        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            final int tmp = i;
            Callable<Integer> task = () -> {
                return findLongestSubstring(texts[tmp]);
            };
            tasks.add(task);
        }

        long startTs = System.currentTimeMillis(); // start time

        List<Future<Integer>> futures = null;
        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Integer> results = new ArrayList<>();
        for (Future<Integer> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                // Задача завершилась с исключением — обрабатываем причину
                Throwable cause = e.getCause();
                System.err.println(cause.getMessage());
            } catch (CancellationException e) {
                // Задача была отменена (например, из-за таймаута в invokeAll)
                System.err.println(e.getMessage());
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }

        int maxSize = results.stream().max(Integer::compare).orElseThrow();

        long endTs = System.currentTimeMillis(); // end time

        System.out.println("Max size: " + maxSize);
        System.out.println("Time: " + (endTs - startTs) + "ms");
        executorService.shutdown();
    }

    public static int findLongestSubstring(String text) {
        int maxSize = 0;
        for (int i = 0; i < text.length(); i++) {
            for (int j = 0; j < text.length(); j++) {
                if (i >= j) {
                    continue;
                }
                boolean bFound = false;
                for (int k = i; k < j; k++) {
                    if (text.charAt(k) == 'b') {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound && maxSize < j - i) {
                    maxSize = j - i;
                }
            }
        }
        return maxSize;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
