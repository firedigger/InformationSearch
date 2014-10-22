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

    public Index() throws FileNotFoundException
    {
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

    public int add_file(String filename)
    {
        int size = files.size();
        files.add(filename);
        return size;
    }

    public void add_word(String word, List<Integer> positions, int value) throws IOException
    {
        if (!headers.containsKey(word))
            headers.put(word, new WordInfo());
        WordInfo current = headers.get(word);

        int lastPos = current.lastPos;

        current.count++;
        current.lastPos = list.size();

        new Data(value, positions).append_to(list);
        list.push_back(lastPos);
    }

    public Set<Data> get_documents(String word) {
        WordInfo data = headers.get(word);
        if (data == null)
            return Collections.emptySet();

        HashSet<Data> result = new HashSet<>(data.count);

        for (int i = 0, curPos = data.lastPos; i < data.count; i++)
        {
            MyList.MyListReader reader = list.getReader(curPos);
            result.add(Data.read_from(reader));
            curPos = reader.read_int();
        }

        return result;
    }

    public void save_to_file(String filename) throws IOException
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
        for (int i = 0; i < docsCount; ++i)
        {
            int length = reader.getInt();
            StringBuilder document = new StringBuilder();
            for (int j = 0; j < length; ++j)
                document.append(reader.getChar());
            files.add(document.toString());
        }

        int listSize = reader.getInt();
        for (int i = 0; i < listSize; ++i)
            list.push_back(reader.getInt());

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


    static class Data
    {
        public int document;
        public MyList positions;

        Data (int document, List<Integer> positions)
        {
            this.document = document;
            this.positions = new MyList(positions.size());
            for (int pos : positions)
                this.positions.push_back(pos);
        }

        public static Data read_from(MyList.MyListReader from)
        {
            int documentNumber = from.read_int();
            int capacity = from.read_int();

            ArrayList<Integer> positions = new ArrayList<>();
            for (int i = 0; i < capacity; i++)
                positions.add(from.read_int());

            return new Data(documentNumber, positions);
        }

        public void append_to(MyList dist)
        {
            dist.push_back(document);
            dist.push_back(positions.size());
            for (int i = 0; i < positions.size(); i++)
                dist.push_back(positions.at(i));
        }

    }


    static class MyList          //C++ style vector
    {
        private static final double GROWTH_COEFFICIENT = 1.6;

        int size;
        int[] data;

        public MyList()
        {
            size = 0;
            data = new int[1];
        }

        public MyList(int capacity)
        {
            size = 0;
            data = new int[capacity];
        }

        public int size()
        {
            return size;
        }

        public int at (int i)
        {
            return data[i];
        }

        public void push_back(int value)
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

        public MyListReader getReader (int position)
        {
            return new MyListReader(position);
        }

        public MyListReader getReader ()
        {
            return getReader(0);
        }

        class MyListReader
        {
            private int position;

            public MyListReader(int position) { this.position = position; }

            public int read_int() { return MyList.this.at(position++); }
        }
    }
}
