package org.uniroma2.it.fileTnxLogTests;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.server.DataTree;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class FileTnxLogTruncateTest {
    private long zxid;

    private static final String PATH = "/truncateTest";
    private static final String PATH_NAME = "log_dir";

    private File logDir;
    private Logger logger;
    private FileTxnLog txnLog;
    private FileTxnLog txnLog2;


    @Parameters
    public static Collection<Long> getParams(){
        return Arrays.asList(1L, -1L, 0L);
    }


    public FileTnxLogTruncateTest(Long zxid)
            throws IOException {
        this.configure(zxid);
    }


    private void configure(Long zxid) throws IOException {
        this.zxid = zxid;
        this.logDir = new File(PATH_NAME);
        this.logDir.mkdir();
        this.logger = Logger.getLogger("TXN");

        this.txnLog = new FileTxnLog(this.logDir);
        this.txnLog2 = new FileTxnLog(this.logDir);

        // append a new record in the log
        this.txnLog.append(new TxnHeader(1, 1, 1, 1000L, 1),
                new CreateTxn(PATH, "".getBytes(), null, false, 0));
        this.txnLog2.append(new TxnHeader(1, 2, 2, 1000L, 1),
                new CreateTxn(PATH, "v".getBytes(), null, false, 0));
        this.txnLog.commit();

    }


    @After
    public void cleanEnv() {
        try {
            if(this.logDir != null)
                FileUtils.deleteDirectory(this.logDir);
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Failed to delete directory\n");
        }
    }


    @Test
    public void truncate() {
        try {
            assertTrue(this.txnLog.truncate(this.zxid));
            assertTrue(this.txnLog2.truncate(this.zxid));
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "An error occurred during log truncation");
        }
    }

    /**@Test
    public void deleteAndTruncate() {
        try {
            assertTrue(this.txnLog.truncate(this.zxid));
            FileUtils.deleteDirectory(this.logDir);
            assertFalse(this.txnLog2.truncate(this.zxid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
