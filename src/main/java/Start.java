import java.io.*;
import java.util.*;

public class Start
{
    static final String INDEX = "-index";
    static final String SEARCH = "-search";
    static final String EXIT = "exit";

    public static void main(String[] args) throws Exception
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

    static void search() throws Exception
    {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        String indexPath = console.readLine();
        Index index = new Index(indexPath);
        RequestProcessor processor = new RequestProcessor(index);

        while (true)
        {
            String request = console.readLine();

            if (request.equals(EXIT))
                break;

            Set<String> result = processor.process_request(request);

            if (result == null)
                System.out.println("No files were found.");
            else
            {
                if (result.size() == 0)
                    System.out.println("No documents were found");
                else
                {
                    System.out.println("Found " + result.size() + " documents:");
                    for (String num : result)
                        System.out.print(num + " ");
                    System.out.println();
                }
            }
        }
    }
}
