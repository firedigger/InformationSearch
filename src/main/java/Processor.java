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

    public Processor(File file) throws IOException {
        dir = file;
        morphology = new RussianMorphology();
        index = new Index();
    }

    public void process() throws IOException
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            index.saveToFile("index.dat");
            return;
        }

        int current = 0;
        for (File file : files)
        {
            String content = Files.toString(file, Charset.defaultCharset());
            content = processContent(content);
            String[] words = content.split("\\s+");

            HashSet<String> allTerms = new HashSet<>();

            for (String word : words) {
                word = word.trim();
                if (word.isEmpty())
                    continue;

                for (String form : morphology.getNormalForms(word))
                {
                    allTerms.add(form);
                }
            }

            int number = index.addFile(file.getName());
            for (String term : allTerms)
            {
                index.addWord(term, number);
            }

            ++current;
            System.out.println(current + " out of " + files.length);
        }


        index.saveToFile("index.dat");
    }

    private String processContent(String content)
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
