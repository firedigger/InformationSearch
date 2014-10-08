import java.io.*;
import java.util.*;

public class Start
{
    static final String INDEX = "-index";
    static final String SEARCH = "-search";

    static final String AND = "AND";
    static final String OR = "OR";
    static final String EXIT = "exit";

    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        if (args.length == 0)
        {
            System.out.println("Error: no parameters");
            return;
        }

        if (args[0].equals(INDEX))
        {
            createIndex();
        }
        else
        if (args[0].equals(SEARCH))
        {
            search();
        }
        else
        {
            System.out.println("Error: unknown parameter");
        }
    }

    static void createIndex() throws IOException
    {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String directory = console.readLine();

        File folder = new File(directory);
        Processor processor = new Processor(folder);
        processor.process();
    }

    static void search() throws IOException, ClassNotFoundException
    {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        String indexPath = console.readLine();
        Index index = new Index(indexPath);

        while (true) {
            String request = console.readLine();
            Set<String> result = new HashSet<>();

            if (request.equals(EXIT))
                break;

            if (request.contains(AND))
            {
                String[] words = request.split(AND);

                boolean isFirst = true;
                for (String word : words)
                {
                    Set<String> docs = index.getDocuments(word.trim());

                    if (isFirst)
                    {
                        result = docs;
                        isFirst = false;
                    }
                    else
                    {
                        result.retainAll(docs);
                    }
                }
            }
            else
            {
                String[] words = request.split(OR);

                for (String word : words)
                {
                    result.addAll(index.getDocuments(word.trim()));
                }
            }

            if (result == null)
            {
                System.out.println("No files have been found.");
            }
            else
            {
                if (result.size() == 0)
                    System.out.println("No documents have been found");
                else
                {
                    System.out.println("Found " + result.size() + " documents:");
                    for (String num : result)
                    {
                        System.out.print(num + " ");
                    }
                    System.out.println();
                }
            }
        }
    }
}
