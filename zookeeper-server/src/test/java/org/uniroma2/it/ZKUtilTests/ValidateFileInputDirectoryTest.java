package org.uniroma2.it.ZKUtilTests;

import org.apache.zookeeper.ZKUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class ValidateFileInputDirectoryTest {
    private Path filePath;
    private String result;
    private String expectedResult;

    @Parameters
    public static Collection getParams(){
        return Arrays.asList(new Object[][] {
                {"file1", true},
                {"file2", false}
        });
    }

    public ValidateFileInputDirectoryTest(String fileName, boolean expected){
        this.configureDirectory(fileName, expected);
    }

    private void configureDirectory(String fileName, boolean expected) {
        this.filePath = Paths.get(fileName);
        try {
            if (!expected) {
                Files.createDirectories(this.filePath);
                this.expectedResult = "'" + this.filePath.toAbsolutePath()
                        + "' is a direcory. it must be a file.";
            }
            else {
                Files.createFile(this.filePath);
                this.expectedResult = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validateDirectory() {
        result = ZKUtil.validateFileInput(this.filePath.toString());
        assertEquals(this.expectedResult, result);
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