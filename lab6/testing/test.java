package capers;

import org.junit.*;

import java.io.File;

public class test {
    @Test
    public void mkdirTest() {
       CapersRepository.setupPersistence();
    }
    @Test
    public void testStory() {
        Utils.join(CapersRepository.CAPERS_FOLDER, "story").delete();
        Main.main(new String[]{"story", "s1"});
        Main.main(new String[]{"story", "s2"});
        Main.main(new String[]{"story", "s3"});
    }
}
