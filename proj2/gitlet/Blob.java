package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class Blob implements Serializable {
    private final File DIR;
    public final String FILE_NAME;
    private final byte[] CONTENT;
    public final String sha1HashCode;
    public Blob(File d) {
        DIR = d;
        FILE_NAME = DIR.getName();
        CONTENT = Utils.readContents(d);
        sha1HashCode = Utils.sha1(DIR, CONTENT);
    }
}
