package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author 2580368016
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STATUS_FILE = join(GITLET_DIR, "Status");
    public static final File STAGED_DIR = join(GITLET_DIR, "staged");
    public static final File REMOVED_DIR = join(GITLET_DIR, "removed");
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File COMMIT_DIR = join(OBJECT_DIR, "commits");
    public static final File BLOB_DIR = join(OBJECT_DIR, "blobs");
    static Status currentStatus = new Status();

    public static void loadStatus() {
        currentStatus = readObject(STATUS_FILE, Status.class);
    }

    public static void saveStatus() {
        writeObject(STATUS_FILE, currentStatus);
    }

    public static void initRepository() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        STAGED_DIR.mkdir();
        REMOVED_DIR.mkdir();
        OBJECT_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();

        Commit initCommit = new Commit();
        initCommit.save();
        currentStatus.branchNameToCommit.put(currentStatus.head, initCommit.SHA1_HASHCODE);

        saveStatus();
    }

    public static void add(String fileName) {
        loadStatus();

        File fileToBeAdded = join(CWD, fileName);
        File stagingFile = join(STAGED_DIR, fileName);
        Blob blob = new Blob(fileToBeAdded);
        Commit cur = currentStatus.getCurrentCommit();

        if (cur.FILENAME_TO_BLOBHASH.containsKey(fileName)
                && Objects.equals(cur.FILENAME_TO_BLOBHASH.get(fileName), blob.SHA1_HASHCODE)) {

            if (stagingFile.exists()) {
                stagingFile.delete();
                currentStatus.stagingArea.remove(fileName);
            }

        } else {
            writeContents(stagingFile, readContents(fileToBeAdded));
            currentStatus.stagingArea.add(fileName);
        }
        currentStatus.deletedArea.remove(fileName);
        saveStatus();
    }

    public static void commit(String message) {
        loadStatus();
        Commit lastCommit = currentStatus.getCurrentCommit();
        if (currentStatus.stagingArea.size() == 0 && currentStatus.deletedArea.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(message,
                new ArrayList<>(Collections.singletonList(lastCommit.SHA1_HASHCODE)));
        currentStatus.updateHead(newCommit.SHA1_HASHCODE);


        for (String removedFile : currentStatus.deletedArea) {
            if (!join(STAGED_DIR, removedFile).exists()) {
                newCommit.FILENAME_TO_BLOBHASH.remove(removedFile);
            }
        }
        currentStatus.deletedArea.clear();

        List<String> stagedDir = Utils.plainFilenamesIn(STAGED_DIR);
        if (stagedDir != null) {
            for (String filename : stagedDir) {
                File blobFile = join(Repository.STAGED_DIR, filename);
                Blob blob = new Blob(blobFile);
                newCommit.FILENAME_TO_BLOBHASH.put(filename, blob.SHA1_HASHCODE);
                blob.save();
                join(Repository.STAGED_DIR, filename).delete();
            }
        }
        currentStatus.stagingArea.clear();

        newCommit.save();
        saveStatus();
    }

    public static void rm(String filename) {
        loadStatus();
        File fileToBeRemoved = join(CWD, filename);
        File stagedFile = join(STAGED_DIR, filename);
        Commit cur = currentStatus.getCurrentCommit();
        if (stagedFile.exists()) {
            stagedFile.delete();
            currentStatus.stagingArea.remove(filename);
        } else if (cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
            currentStatus.deletedArea.add(filename);
            if (join(CWD, filename).exists()) {
                writeContents(stagedFile, readContents(fileToBeRemoved));
                join(CWD, filename).delete();
            }
        } else {
            System.out.println("No reason to remove the file.\n");
        }
        saveStatus();
    }

    public static void log() {
        loadStatus();
        Commit curCommit = currentStatus.getCurrentCommit();
        while (true) {
            curCommit.printCommit();
            if (curCommit.FATHER == null) {
                break;
            }
            curCommit = Commit.getCommit(curCommit.FATHER.get(0));
        }
    }

    public static void globalLog() {
        loadStatus();
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        if (filenames == null) {
            return;
        }
        for (String filename : filenames) {
            Commit commit = readObject(join(COMMIT_DIR, filename), Commit.class);
            commit.printCommit();
        }
    }

    public static void find(String msg) {
        loadStatus();
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        if (filenames == null) {
            System.out.println("Found no commit with that message.");
            return;
        }
        boolean find = false;
        for (String filename : filenames) {
            Commit commit = readObject(join(COMMIT_DIR, filename), Commit.class);
            if (Objects.equals(commit.MESSAGE, msg)) {
                find = true;
                System.out.println(commit.SHA1_HASHCODE);
            }
        }
        if (!find) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        loadStatus();

        System.out.println("=== Branches ===");
        for (String branch : currentStatus.branchNameToCommit.keySet()) {
            if (Objects.equals(branch, currentStatus.head)) {
                System.out.print('*');
            }
            System.out.println(branch);
        }
        System.out.print("\n");

        System.out.println("=== Staged Files ===");
        for (String stagedFile : currentStatus.stagingArea) {
            System.out.println(stagedFile);
        }
        System.out.print("\n");

        System.out.println("=== Removed Files ===");
        for (String removedFile : currentStatus.deletedArea) {
            System.out.println(removedFile);
        }
        System.out.print("\n");
        //TODO: extra part
        System.out.println("=== Modifications Not Staged For Commit ===\n");
        System.out.println("=== Untracked Files ===\n");
    }

    private static void checkoutNotFoundCommit() {
        System.out.println("No commit with that id exists.");
        System.exit(0);
    }
    public static void checkout1(String filename) {
        loadStatus();
        checkout2(currentStatus.getCurrentCommit().SHA1_HASHCODE, filename);

    }

    public static void checkout2(String commitID, String filename) {
        File file = null;
        if(commitID.length() == 40) {
            file = join(COMMIT_DIR, commitID);
            if (!file.exists()) {
                checkoutNotFoundCommit();
            }
        } else {
            if (Utils.plainFilenamesIn(COMMIT_DIR) == null) {
                checkoutNotFoundCommit();
            }
            for (String ID : Utils.plainFilenamesIn(COMMIT_DIR)) {
                String shortID = ID.substring(0, commitID.length() - 1);
                if (Objects.equals(shortID, commitID)) {
                    file = join(COMMIT_DIR, ID);
                    break;
                }
            }
        }
        if (file == null) {
            checkoutNotFoundCommit();
        }
        Commit cur = readObject(file, Commit.class);

        if (!cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            
            Blob blob = readObject(join(BLOB_DIR,
                    cur.FILENAME_TO_BLOBHASH.get(filename)), Blob.class);
            writeContents(join(CWD, blob.FILE_NAME), blob.CONTENT);
        }
    }

    public static void checkout3(String branchName) {
        loadStatus();

        if (!currentStatus.branchNameToCommit.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (Objects.equals(branchName, currentStatus.head)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit cur = currentStatus.getCurrentCommit();
            Commit branch = readObject(join(COMMIT_DIR,
                    currentStatus.branchNameToCommit.get(branchName)), Commit.class);

            List<String> cwdFiles = plainFilenamesIn(CWD);
            if (cwdFiles != null) {
                for (String file : cwdFiles) {
                    if (branch.FILENAME_TO_BLOBHASH.containsKey(file)
                            && !cur.FILENAME_TO_BLOBHASH.containsKey(file)) {
                        System.out.println("There is an untracked file in the way;"
                                + " delete it, or add and commit it first.");
                        return;
                    }
                }
            }

            for (var e : branch.FILENAME_TO_BLOBHASH.entrySet()) {
                Blob blob = readObject(join(BLOB_DIR, e.getValue()), Blob.class);
                writeContents(join(CWD, e.getKey()), blob.CONTENT);
            }

            for (var e : cur.FILENAME_TO_BLOBHASH.entrySet()) {
                File file = join(CWD, e.getKey());
                if (file.exists() && !branch.FILENAME_TO_BLOBHASH.containsKey(e.getKey())) {
                    file.delete();
                }
            }

            currentStatus.head = branchName;
            currentStatus.branchNameToCommit.put(branchName, branch.SHA1_HASHCODE);
            saveStatus();
        }
    }

    public static void branch(String branchName) {
        loadStatus();
        if (currentStatus.branchNameToCommit.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        currentStatus.branchNameToCommit.put(branchName,
                currentStatus.getCurrentCommit().SHA1_HASHCODE);
        saveStatus();
    }

    public static void rmBranch(String branchName) {
        loadStatus();
        if (!currentStatus.branchNameToCommit.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (Objects.equals(currentStatus.head, branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        currentStatus.branchNameToCommit.remove(branchName);
        saveStatus();
    }
}
