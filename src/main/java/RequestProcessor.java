import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DELL on 10/22/2014.
 */
public class RequestProcessor
{
    static final String AND = "AND";
    static final String OR = "OR";
    static final String DEL = "/(?<dir>[+-]?)(?<len>\\d*)";

    private Index myIndex;

    public RequestProcessor (Index index)
    {
        myIndex = index;
    }

    public Set<String> process_request (String request) throws Exception
    {
        String[] split = (request + " AND").split(" ");

        int last_operation = 0;
        Set<String> result = null;
        List<String> subrequest = new ArrayList<>();

        for (String value : split)
        {
            value = value.trim();
            if (value.isEmpty())
                continue;

            if (value.equals(AND) || value.equals(OR))
            {
                Set<String> current = process_subrequest(subrequest);

                switch (last_operation)
                {
                    case 0:
                        result = current;
                        break;
                    case 1:   //AND
                        result.retainAll(current);
                        break;
                    case 2:   //OR
                        result.addAll(current);
                        break;
                }

                switch (value)
                {
                    case AND:
                        last_operation = 1;
                        break;
                    case OR:
                        last_operation = 2;
                        break;
                }

                subrequest = new ArrayList<>();
            }
            else
                subrequest.add(value);
        }

        return result;
    }

    public Set<String> process_subrequest (List<String> request) throws Exception
    {
        if (request.size() % 2 == 0)
            throw new Exception("ERROR : bad request");

        Set<Index.Data> current = myIndex.get_documents(request.get(0));

        for (int i = 1; i < request.size(); i += 2) {
            Set<Index.Data> next = myIndex.get_documents(request.get(i + 1));

            Matcher match = Pattern.compile(DEL).matcher(request.get(i));
            if (!match.matches())
                throw new Exception("ERROR: bad request");

            String dir = match.group("dir");
            String len = match.group("len");

            boolean isLower = dir.equals("-") || dir.isEmpty();
            boolean isUpper = dir.equals("+") || dir.isEmpty();
            int length = Integer.parseInt(len);

            Set<Index.Data> newSet = new HashSet<>();
            for (Index.Data curData : current)
                for (Index.Data nextData : next)
                    if (curData.document == nextData.document)
                    {
                        List<Integer> positions = new ArrayList<>();

                        for (int first = 0; first < curData.positions.size(); first++)
                            for (int second = 0; second < nextData.positions.size(); second++)
                            {
                                int dist = nextData.positions.at(second) - curData.positions.at(first);
                                if (isLower && 0 < -dist && -dist <= length || isUpper && 0 < dist && dist <= length)
                                    positions.add(second);
                            }
                        if (!positions.isEmpty())
                            newSet.add(new Index.Data(curData.document, positions));
                    }
                    else
                        System.out.println(curData.document + " " + nextData.document);
            current = newSet;
        }

        Set<String> result = new HashSet<>();
        for (Index.Data data : current)
            result.add(myIndex.files.get(data.document));

        return result;
    }
}
