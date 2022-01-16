package org.uniroma2.it.ZKUtilTests;

import org.apache.zookeeper.ZKUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class ValidateFileInputExistsTest {

    private Path filePath;
    private String result;
    private String expectedResult1;
    private String expectedResult2;

    @Parameters
    public static Collection getParams(){
        return Arrays.asList(new Object[][] {
                {"file1", null, -1},
                {"\u0001", -1, -1}
        });
    }

    public ValidateFileInputExistsTest(String fileName, Integer expected1, Integer expected2) throws IOException {
        this.configure(fileName, expected1, expected2);
    }

        private void configure(String fileName, Integer expected1, Integer expected2) throws IOException {
        if (fileName != null) {
            this.filePath = Paths.get(fileName);
            if (fileName.equals("file1")) Files.createFile(this.filePath).toFile();
            if (expected1!=null)
                this.expectedResult1 = "File '" + this.filePath.toAbsolutePath() + "' does not exist.";
            /**else if (expected2!=null)
                this.expectedResult2 = "File '" + this.filePath.toAbsolutePath() + "' does not exist.";*/
            else this.expectedResult1 = null;
        }
    }

    @Test
    public void validateExists() {
        result = ZKUtil.validateFileInput(this.filePath.toString());
        assertEquals(result, this.expectedResult1);
    }

    @After
    public void cleanUp() {
        try {
            if (filePath!=null)
                Files.deleteIfExists(this.filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
