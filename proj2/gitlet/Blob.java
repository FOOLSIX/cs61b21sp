package gitlet;
import java.io.File;
import java.io.Serializable;
public class Blob implements Serializable {
    private final File DIR;
    final String FILE_NAME;
    final byte[] CONTENT;
    final String SHA1_HASHCODE;
    public Blob(File d) {
        DIR = d;
        FILE_NAME = DIR.getName();
        CONTENT = Utils.readContents(d);
        SHA1_HASHCODE = Utils.sha1(FILE_NAME, CONTENT);
    }
    public void save() {
        Utils.writeObject(Utils.join(Repository.BLOB_DIR, SHA1_HASHCODE), this);
    }
}
