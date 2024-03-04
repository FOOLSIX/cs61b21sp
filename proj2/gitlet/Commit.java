package gitlet;

import java.io.Serializable;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /*
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String MESSAGE;
    /** The date of this Commit. */
    private final Date DATE;
    public final List<Commit> FATHER;
    /** Save all filenames and map to its blob's hashcode */
    public final Map<String, String> FILENAME_TO_BLOBHASH = new HashMap<>();
    public final String SHA1_HASHCODE;

    public Commit(String msg, Date d, List<Commit> fa) {
        MESSAGE = msg;
        DATE = d;
        for (Commit f : fa) {
            FILENAME_TO_BLOBHASH.putAll(f.FILENAME_TO_BLOBHASH);
        }
        FATHER = fa;
        SHA1_HASHCODE = Utils.sha1(msg, d, fa);
    }

    public Commit() {
        MESSAGE = "initial commit";
        DATE = new Date(1970, 1, 1, 0,0, 0);
        FATHER = null;
        SHA1_HASHCODE = Utils.sha1(MESSAGE, DATE);
    }

}
