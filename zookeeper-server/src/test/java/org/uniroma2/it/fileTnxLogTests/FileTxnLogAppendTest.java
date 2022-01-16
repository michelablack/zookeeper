package org.uniroma2.it.fileTnxLogTests;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.io.FileUtils;
import org.apache.jute.Record;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.TxnDigest;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.LoggerFactory;
import utils.MemoryAppender;

import static org.junit.Assert.*;


@RunWith(value = Parameterized.class)
public class FileTxnLogAppendTest {
    private TxnHeader header;
    private Record record;
    private TxnDigest digest;
    private boolean expectedResult;
    private static MemoryAppender memoryAppender;
    boolean reinsert;
    private static final String PATH = "/appendTest";
    private static final String PATH_NAME = "log_dir";

    private File logDir;
    private FileTxnLog txnLog;
    private Logger logger;


    @Before
    public void setUp() {
        logger = (Logger) LoggerFactory.getLogger(FileTxnLog.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.addAppender(memoryAppender);
        logger.setLevel(Level.WARN);
        memoryAppender.start();
    }

    @Parameters
    public static Collection<Object[]> getParams(){
        char[] fill = new char[4];
        Arrays.fill(fill, 'v');
        String VALID_WORD = String.valueOf(fill);
        Record record = new CreateTxn(PATH, VALID_WORD.getBytes(), null, false, 0);
        TxnDigest txnDigest = new TxnDigest();

        return Arrays.asList(new Object[][] {
                {new TxnHeader(1L, 0, 1L, 1000L, 1), record, txnDigest, false, false},
                {null, record, txnDigest, false, false},
                {new TxnHeader(1L, 0, 1L, 1000L, 1), null, txnDigest, true, false},
                {new TxnHeader(1L, 0, -1L, 1000L, 1), record, txnDigest, true, true}
        });
    }


    public FileTxnLogAppendTest(TxnHeader header, Record record, TxnDigest digest, boolean reinsert, boolean expectedResult)
            throws IOException {
        this.configure(header, record, digest, reinsert, expectedResult);
    }


    private void configure(TxnHeader header, Record record, TxnDigest digest,
                           boolean reinsert, boolean expectedResult) {
        this.header = header;
        this.record = record;
        this.digest = digest;
        this.expectedResult = expectedResult;
        this.reinsert = reinsert;
        this.logDir = new File(PATH_NAME);
        this.logDir.mkdir();

        this.txnLog = new FileTxnLog(this.logDir);

    }


    @After
    public void cleanEnv() {
        try {
            if(this.logDir != null)
                FileUtils.deleteDirectory(this.logDir);

            if(this.header != null) {
                File f = new File("log."+this.header.getClientId());
                f.delete();
            }
        } catch (IOException e) {
            this.logger.debug("Failed to delete directory\n");
        }
        memoryAppender.stop();
        this.logger.detachAppender(memoryAppender);
        memoryAppender.reset();
    }


    @Test
    public void testAppend() throws IOException {
        if(this.header == null)
            assertFalse(this.txnLog.append(this.header, this.record));
        else {
            assertTrue(this.txnLog.append(this.header, this.record));
            assertEquals(memoryAppender.contains("Current zxid " + this.header.getZxid() +
                    " is <= 0 for " +
                    Request.op2String(this.header.getType()), Level.WARN), this.expectedResult);
        }
        this.txnLog.close();
    }


    // Test added in order to kill mutations
    @Test
    public void testMoreAppends() throws IOException {
        TxnHeader beforeHeader;
        if (this.reinsert) {
            beforeHeader = new TxnHeader(this.header.getClientId(), this.header.getCxid(),
                    this.header.getZxid()+1L, this.header.getTime(), this.header.getType());
            if(this.header == null)
                assertFalse(this.txnLog.append(this.header, this.record));
            else {
                assertTrue(this.txnLog.append(this.header, this.record));
                assertTrue(this.txnLog.append(beforeHeader, this.record));
            }
        }

        this.txnLog.close();
    }
    @Test
    public void testAppendWithDigest() throws IOException {
        if(this.header == null)
            assertFalse(this.txnLog.append(this.header, this.record, this.digest));
        else {
            assertTrue(this.txnLog.append(this.header, this.record, this.digest));
        }
        this.txnLog.close();
    }
}
