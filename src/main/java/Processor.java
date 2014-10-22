import com.google.common.io.Files;
import org.apache.lucene.morphology.russian.RussianMorphology;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class Processor
{
    private File dir;
    private RussianMorphology morphology;
    private Index index;

    public Processor(File file) throws IOException
    {
        dir = file;
        morphology = new RussianMorphology();
        index = new Index();
    }

    public void process() throws IOException
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            index.save_to_file("index.dat");
            return;
        }

        int processedFiles = 0;
        for (File file : files)
        {
            String content = Files.toString(file, Charset.defaultCharset());
            content = process_content(content);
            String[] words = content.split("\\s+");

            Map<String, List<Integer>> allTerms = new HashMap<>();

            int current_position = 0;
            for (String word : words) {
                word = word.trim();
                if (word.isEmpty())
                    continue;

                for (String form : morphology.getNormalForms(word))
                {
                    List<Integer> current;

                    if (!allTerms.containsKey(form))
                    {
                        current = new ArrayList<>();
                        allTerms.put(form, current);
                    }
                    else
                        current = allTerms.get(form);
                    current.add(current_position);
                }

                current_position++;
            }

            int number = index.add_file(file.getName());
            for (Map.Entry<String, List<Integer>> term : allTerms.entrySet())
                index.add_word(term.getKey(), term.getValue(), number);

            System.out.println(++processedFiles + " out of " + files.length);
        }

        index.save_to_file("index.dat");
    }

    private String process_content(String content)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < content.length(); ++i)
        {
            char symbol = content.charAt(i);

            if (symbol >= 'а' && symbol <= 'я' || symbol >= 'А' && symbol <= 'Я')
                builder.append(Character.toLowerCase(symbol));
            else
                builder.append(' ');
        }

        return builder.toString();
    }
}
