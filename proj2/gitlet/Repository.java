package gitlet;

import java.io.File;
import java.util.*;

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
        Commit cur = currentStatus.getCommit(currentStatus.head);

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
        Commit lastCommit = currentStatus.getCommit(currentStatus.head);
        if (currentStatus.stagingArea.size() == 0 && currentStatus.deletedArea.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(message, lastCommit.SHA1_HASHCODE);
        currentStatus.updateHead(newCommit.SHA1_HASHCODE);

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

        for (String removedFile : currentStatus.deletedArea) {
            if (!join(STAGED_DIR, removedFile).exists()) {
                newCommit.FILENAME_TO_BLOBHASH.remove(removedFile);
            }
        }
        currentStatus.deletedArea.clear();

        newCommit.save();
        saveStatus();
    }

    public static void rm(String filename) {
        loadStatus();
        File fileToBeRemoved = join(CWD, filename);
        File stagedFile = join(STAGED_DIR, filename);
        Commit cur = currentStatus.getCommit(currentStatus.head);
        if (stagedFile.exists()) {
            stagedFile.delete();
            currentStatus.stagingArea.remove(filename);
        } else if (cur.FILENAME_TO_BLOBHASH.containsKey(filename)
                && !currentStatus.deletedArea.contains(filename)) {
            currentStatus.deletedArea.add(filename);
            if (join(CWD, filename).exists()) {
                writeContents(stagedFile, readContents(fileToBeRemoved));
                join(CWD, filename).delete();
            }
        } else {
            System.out.println("No reason to remove the file.");
        }
        saveStatus();
    }

    public static void log() {
        loadStatus();
        Commit curCommit = currentStatus.getCommit(currentStatus.head);
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
            Commit commit = Commit.getCommit(filename);
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
            Commit commit = Commit.getCommit(filename);
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

        System.out.println("=== Modifications Not Staged For Commit ===\n");
        System.out.println("=== Untracked Files ===\n");
    }

    private static void notFoundCommit() {
        System.out.println("No commit with that id exists.");
        System.exit(0);
    }
    public static void checkout1(String filename) {
        loadStatus();
        checkout2(currentStatus.getCommit(currentStatus.head).SHA1_HASHCODE, filename);

    }

    public static void checkout2(String commitID, String filename) {
        File file = null;
        if (commitID.length() == 40) {
            file = join(COMMIT_DIR, commitID);
            if (!file.exists()) {
                notFoundCommit();
            }
        } else {
            if (Utils.plainFilenamesIn(COMMIT_DIR) == null) {
                notFoundCommit();
            }
            for (String id : Utils.plainFilenamesIn(COMMIT_DIR)) {
                String shortID = id.substring(0, commitID.length());
                if (Objects.equals(shortID, commitID)) {
                    file = join(COMMIT_DIR, id);
                    break;
                }
            }
        }
        if (file == null) {
            notFoundCommit();
        }
        Commit cur = readObject(file, Commit.class);

        if (!cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            
            Blob blob = Blob.getBlob(cur.FILENAME_TO_BLOBHASH.get(filename));
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
            Commit cur = currentStatus.getCommit(currentStatus.head);
            Commit branch = currentStatus.getCommit(branchName);
            //try to delete tracked files
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
            //restore files
            for (var e : branch.FILENAME_TO_BLOBHASH.entrySet()) {
                Blob blob = Blob.getBlob(e.getValue());
                writeContents(join(CWD, e.getKey()), blob.CONTENT);
            }
            //delete tracked files
            for (var e : cur.FILENAME_TO_BLOBHASH.entrySet()) {
                File file = join(CWD, e.getKey());
                if (file.exists() && !branch.FILENAME_TO_BLOBHASH.containsKey(e.getKey())) {
                    file.delete();
                }
            }
            //clear staged area
            for (String filename : plainFilenamesIn(STAGED_DIR)) {
                join(STAGED_DIR, filename).delete();
            }
            currentStatus.stagingArea.clear();
            currentStatus.deletedArea.clear();

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
                currentStatus.getCommit(currentStatus.head).SHA1_HASHCODE);
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

    public static void reset(String commitID) {
        if (!join(COMMIT_DIR, commitID).exists()) {
            notFoundCommit();
        }
        branch(commitID);
        loadStatus();
        String originBranch = currentStatus.head;
        currentStatus.branchNameToCommit.replace(commitID, commitID);
        saveStatus();
        checkout3(commitID);
        loadStatus();
        currentStatus.head = originBranch;
        currentStatus.branchNameToCommit.replace(originBranch, commitID);
        saveStatus();
        rmBranch(commitID);
    }

    private static void getAncestor(String commitID, HashSet<String> commits) {
        commits.add(commitID);
        Commit cur = Commit.getCommit(commitID);
        if (cur.FATHER == null) {
            return;
        }
        for (String fa : cur.FATHER) {
            getAncestor(fa, commits);
        }
    }
    private static String getSameAncestorHelper(String commit, HashSet<String> commits) {
        Queue<String> q = new LinkedList<>();
        q.add(commit);
        while (!q.isEmpty()) {
            Commit cur = Commit.getCommit(q.poll());
            if (commits.contains(cur.SHA1_HASHCODE)) {
                return cur.SHA1_HASHCODE;
            }
            q.addAll(cur.FATHER);
        }
        return null;
    }
    private static String getSameAncestor(String branch1, String branch2) {
        String commit1 =  currentStatus.getCommit(branch1).SHA1_HASHCODE;
        String commit2 =  currentStatus.getCommit(branch2).SHA1_HASHCODE;
        HashSet<String> commit1Ancestors = new HashSet<>();
        getAncestor(commit1, commit1Ancestors);
        return getSameAncestorHelper(commit2, commit1Ancestors);
    }
    public static String checkMerge(String branchName) {
        if (!currentStatus.branchNameToCommit.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (Objects.equals(branchName, currentStatus.head)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (currentStatus.stagingArea.size() != 0 || currentStatus.deletedArea.size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        Commit branch = currentStatus.getCommit(branchName);
        Commit cur = currentStatus.getCommit(currentStatus.head);
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles != null) {
            for (String file : cwdFiles) {
                if (!cur.FILENAME_TO_BLOBHASH.containsKey(file)
                        && branch.FILENAME_TO_BLOBHASH.containsKey(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        String ancestor = getSameAncestor(currentStatus.head, branchName);
        if (Objects.equals(branch.SHA1_HASHCODE, ancestor)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (Objects.equals(cur.SHA1_HASHCODE, ancestor)) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        return ancestor;
    }
    private static boolean isEqualFile(Commit commit1, Commit commit2, String filename) {
        return Objects.equals(commit1.FILENAME_TO_BLOBHASH.get(filename),
                commit2.FILENAME_TO_BLOBHASH.get(filename));
    }
    public static void merge(String branchName) {
        loadStatus();
        String ancestor = checkMerge(branchName);
        Commit cur = currentStatus.getCommit(currentStatus.head);
        Commit branch = currentStatus.getCommit(branchName);
        Commit ancestorCommit = Commit.getCommit(ancestor);
        HashMap<String, String> filenamesToBlob = new HashMap<>();
        filenamesToBlob.putAll(cur.FILENAME_TO_BLOBHASH);
        boolean conflict = false;
        for (Map.Entry<String, String> e : branch.FILENAME_TO_BLOBHASH.entrySet()) {
            String filename = e.getKey();
            String blobHash = e.getValue();
            if (cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
                //Any files that have been modified in the given branch since the split point,
                // but not modified in the current branch since the split point
                // should be changed to their versions in the given branch
                //These files should then all be automatically staged.
                if (isEqualFile(cur, ancestorCommit, filename)
                        && !isEqualFile(branch, cur, filename)) {
                    filenamesToBlob.replace(filename, blobHash);
                    writeContents(join(CWD, filename), Blob.getBlob(blobHash).CONTENT);
                //conflict case
                } else if (!isEqualFile(cur, ancestorCommit, filename)
                        && !isEqualFile(branch, ancestorCommit, filename)) {
                    File file = join(CWD, filename);
                    byte[] headContent;
                    if (cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
                        headContent = Blob.getBlob(cur.FILENAME_TO_BLOBHASH.get(filename)).CONTENT;
                    } else {
                        headContent = new byte[0];
                    }
                    var branchContent = Blob.getBlob(branch.FILENAME_TO_BLOBHASH.get(filename)).CONTENT;
                    writeContents(file, "<<<<<<< HEAD\n", headContent,
                            "=======\n", branchContent,
                            ">>>>>>>\n");
                    conflict = true;
                }
            } else {
                //Any files that were not present at the split point
                //and are present only in the given branch should be checked out and staged.
                if (!isEqualFile(branch, ancestorCommit, filename)) {
                    Blob blob = Blob.getBlob(blobHash);
                    filenamesToBlob.put(filename, blobHash);
                    writeContents(join(CWD, blob.FILE_NAME), blob.CONTENT);
                }
            }
        }
        for (Map.Entry<String, String> e : cur.FILENAME_TO_BLOBHASH.entrySet()) {
            String filename = e.getKey();
            if (!branch.FILENAME_TO_BLOBHASH.containsKey(filename)) {
                //Any files present at the split point, unmodified in the current branch,
                // and absent in the given branch should be removed (and untracked).
                if (isEqualFile(cur, ancestorCommit, filename)) {
                    join(CWD, filename).delete();
                    filenamesToBlob.remove(filename);
                } else if (ancestorCommit.FILENAME_TO_BLOBHASH.containsKey(filename)){
                    //conflict case
                    var headContent = Blob.getBlob(cur.FILENAME_TO_BLOBHASH.get(filename)).CONTENT;
                    writeContents(join(CWD, filename), "<<<<<<< HEAD\n", headContent,
                            "=======\n", ">>>>>>>\n");
                    conflict = true;
                }
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        Commit newCommit = new Commit("Merged " + branchName + " into "
                + currentStatus.head + ".", filenamesToBlob,
                List.of(new String[]{cur.SHA1_HASHCODE, branch.SHA1_HASHCODE}));
        newCommit.save();
        currentStatus.branchNameToCommit.replace(currentStatus.head, newCommit.SHA1_HASHCODE);
        saveStatus();
    }
}
