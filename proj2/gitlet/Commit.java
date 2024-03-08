package gitlet;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a gitlet commit object.
 * does at a high level.
 *
 * @author 2580368016
 */
public class Commit implements Serializable {
    /*
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    final String MESSAGE;
    /**
     * The date of this Commit.
     */
    final Date DATE;
    final List<String> FATHER;
    /**
     * Save all filenames and map to its blob's hashcode
     */
    Map<String, String> FILENAME_TO_BLOBHASH;
    final String SHA1_HASHCODE;

    public Commit(String msg, String fa) {
        MESSAGE = msg;
        DATE = new Date();
        FILENAME_TO_BLOBHASH = new HashMap<>();
        Commit father = getCommit(fa);
        FILENAME_TO_BLOBHASH.putAll(father.FILENAME_TO_BLOBHASH);
        FATHER = Collections.singletonList(fa);
        HashSet<String> hashArgs = new HashSet<>();
        hashArgs.add(MESSAGE);
        hashArgs.add(DATE.toString());
        hashArgs.add(fa);
        hashArgs.addAll(FILENAME_TO_BLOBHASH.values());
        SHA1_HASHCODE = Utils.sha1(hashArgs.toArray());
    }
    public Commit(String msg, Map<String, String> filenameToBlobHash, List<String> fa) {
        MESSAGE = msg;
        DATE = new Date();
        FILENAME_TO_BLOBHASH = filenameToBlobHash;
        FATHER = fa;
        HashSet<String> hashArgs = new HashSet<>();
        hashArgs.add(MESSAGE);
        hashArgs.add(DATE.toString());
        hashArgs.addAll(fa);
        hashArgs.addAll(FILENAME_TO_BLOBHASH.values());
        SHA1_HASHCODE = Utils.sha1(hashArgs.toArray());
    }


    public Commit() {
        MESSAGE = "initial commit";
        DATE = new Date(0);
        FATHER = null;
        FILENAME_TO_BLOBHASH = new HashMap<>();
        HashSet<String> hashArgs = new HashSet<>();
        SHA1_HASHCODE = Utils.sha1(MESSAGE, DATE.toString());
    }

    public void save() {
        Utils.writeObject(Utils.join(Repository.COMMIT_DIR, SHA1_HASHCODE), this);
    }

    public void printCommit() {
        System.out.println("===");
        System.out.println("commit " + SHA1_HASHCODE);
        if (FATHER != null && FATHER.size() > 1) {
            for (int i = 0; i < FATHER.size(); ++i) {
                System.out.print(FATHER.get(i).substring(0, 6));
                if (i == FATHER.size() - 1) {
                    System.out.print('\n');
                } else {
                    System.out.print(' ');
                }
            }
        }

        Formatter formatter = new Formatter();
        formatter.format("%ta %tb %te %tT %tY %tz", DATE, DATE, DATE, DATE, DATE, DATE);
        System.out.println("Date: " + formatter);
        System.out.println(MESSAGE + '\n');

    }

    public static Commit getCommit(String sha1HashCode) {
        return Utils.readObject(Utils.join(Repository.COMMIT_DIR, sha1HashCode), Commit.class);
    }

}
