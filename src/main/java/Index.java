import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class Index implements Serializable
{
    HashMap<String, WordInfo> headers;
    List<String> files;
    MyList list;
    int pos;

    public Index() throws FileNotFoundException {
        headers = new HashMap<>(1000);
        files = new ArrayList<>();
        list = new MyList();
        pos = 0;
    }

    public Index(String filename) throws IOException, ClassNotFoundException
    {
        this();
        load(filename);
    }

    public int addFile (String filename)
    {
        int size = files.size();
        files.add(filename);
        return size;
    }

    public void addWord(String word, int value) throws IOException
    {
        if (!headers.containsKey(word))
            headers.put(word, new WordInfo());
        WordInfo current = headers.get(word);

        list.at(value);
        list.at(current.lastPos);

        current.count++;
        current.lastPos = pos;

        pos++;
    }

    public Set<String> getDocuments(String word)
    {
        WordInfo info = headers.get(word);
        if (info == null)
            return Collections.emptySet();

        HashSet<String> result = new HashSet<>(info.count);

        int curPos = info.lastPos;
        for (int i = 0; i < info.count; ++i)
        {
            result.add(files.get(list.at(curPos * 2)));
            curPos = list.at(curPos * 2 + 1);
        }

        return result;
    }

    public void saveToFile(String filename) throws IOException
    {
        FileOutputStream file = new FileOutputStream(filename);

        file.write(ByteBuffer.allocate(4).putInt(headers.size()).array());
        for (Map.Entry<String, WordInfo> entry : headers.entrySet())
        {
            ByteBuffer buffer = ByteBuffer.allocate(4 + entry.getKey().length() * 2 + 4 + 4);
            buffer.putInt(entry.getKey().length());
            for (char c : entry.getKey().toCharArray())
                buffer.putChar(c);
            buffer.putInt(entry.getValue().count);
            buffer.putInt(entry.getValue().lastPos);

            file.write(buffer.array());
        }

        file.write(ByteBuffer.allocate(4).putInt(files.size()).array());
        for (String document : files)
        {
            ByteBuffer buffer = ByteBuffer.allocate(4 + document.length() * 2);

            buffer.putInt(document.length());
            for (char c : document.toCharArray())
                buffer.putChar(c);

            file.write(buffer.array());
        }

        file.write(ByteBuffer.allocate(4).putInt(list.size()).array());
        for (int i = 0; i < list.size(); ++i)
            file.write(ByteBuffer.allocate(4).putInt(list.at(i)).array());

        file.close();

        System.out.println("Index has been saved.");
    }

    public void load(String filename) throws IOException, ClassNotFoundException
    {
        File file = new File(filename);
        FileInputStream stream = new FileInputStream(file);
        MappedByteBuffer reader = stream.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());

        int headerSize = reader.getInt();
        for (int i = 0; i < headerSize; ++i)
        {
            int length = reader.getInt();
            StringBuilder word = new StringBuilder();
            for (int j = 0; j < length; ++j)
                word.append(reader.getChar());

            int cnt = reader.getInt();
            int lastPos = reader.getInt();

            headers.put(word.toString(), new WordInfo(cnt, lastPos));
        }

        int docsCount = reader.getInt();
        for (int i = 0; i < docsCount; ++i) {
            int length = reader.getInt();
            StringBuilder document = new StringBuilder();
            for (int j = 0; j < length; ++j)
                document.append(reader.getChar());
            files.add(document.toString());
        }

        int listSize = reader.getInt();
        for (int i = 0; i < listSize; ++i)
            list.pushback(reader.getInt());

        System.out.println("Index has been loaded.");
    }

    class WordInfo implements Serializable
    {
        public int lastPos;
        public int count;

        public WordInfo ()
        {
            lastPos = count = 0;
        }

        public WordInfo (int count, int lastPos)
        {
            this.lastPos = lastPos;
            this.count = count;
        }
    }

    class MyList          //C++ style vector
    {
        private static final double GROWTH_COEFFICIENT = 1.6;

        int size;
        int[] data;

        public MyList()
        {
            size = 0;
            data = new int[1];
        }

        public int size()
        {
            return size;
        }

        public int at (int i)
        {
            return data[i];
        }

        public void pushback (int value)
        {
            if (size == data.length)
            {
                int[] newData = new int[(int) (data.length * GROWTH_COEFFICIENT)];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }

            data[size] = value;
            ++size;
        }
    }
}
