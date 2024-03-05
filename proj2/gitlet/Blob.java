package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class Blob implements Serializable {
    private final File DIR;
    public final String FILE_NAME;
    private final byte[] CONTENT;
    public final String SHA1_HASHCODE;
    public Blob(File d) {
        DIR = d;
        FILE_NAME = DIR.getName();
        CONTENT = Utils.readContents(d);
        SHA1_HASHCODE = Utils.sha1(DIR.toString(), CONTENT);
    }
    public void save() {
        Utils.writeObject(Utils.join(Repository.OBJECT_DIR, SHA1_HASHCODE), this);
    }
}
