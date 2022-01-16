package org.uniroma2.it.fileTnxLogTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.cli.AclParser;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import org.apache.zookeeper.server.persistence.TxnLog.TxnIterator;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.TxnDigest;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.internal.matchers.Null;

/* Test the read from a log */


@RunWith(value = Parameterized.class)
public class FIleTxnLogReadTest {
    private long zxid;
    private boolean fastForward;
    private static String PATH_NAME = "path";
    private static String PATH = "/readTest";
    private FileTxnLog fTxnLog;
    private File logDir;
    private Logger logger;
    private long size;


    @Parameters
    public static Collection<Object[]> getParams(){
        return Arrays.asList(new Object[][] {
                {0L, true, 3L},
                {1L, false, 1L},
                {-1L, true, 0L}
        });
    }


    public FIleTxnLogReadTest(long zxid, boolean fastForward, long size)
            throws IOException {
        this.configure(zxid, fastForward, size);
    }


    private void configure(long zxid, boolean fastForward, long size)
            throws IOException {
        this.logDir = new File(PATH_NAME);
        this.logDir.mkdir();

        this.zxid = zxid;
        this.fastForward = fastForward;
        this.size = size;
        this.fTxnLog = new FileTxnLog(this.logDir);
        this.logger = Logger.getLogger("TXNR");


        Map<TxnHeader, CreateTxn> entries = new HashMap<>();

        for (long i=0; i<size; i++) {
            TxnHeader header = new TxnHeader(1L, 0, i, 1000L, 1);
            CreateTxn txn = new CreateTxn(PATH, "vvvv".getBytes(), null,
                    false, 0);
            entries.put(header, txn);
        }
        for(TxnHeader header : entries.keySet()) {
            this.fTxnLog.append(header, entries.get(header), new TxnDigest());
            System.out.println(header.getZxid());
            this.fTxnLog.commit();
        }
    }


    @After
    public void cleanEnv() {
        try {
            FileUtils.deleteDirectory(this.logDir);
            this.fTxnLog.close();
        } catch (IOException e) {
            this.logger.log(Level.WARNING, "Error while cleaning env \n");
        }
    }

    
    @Test
    public void testRead() throws IOException {
        TxnIterator iterator = this.fTxnLog.read(this.zxid);
        TxnIterator iterator2 = this.fTxnLog.read(this.zxid, this.fastForward);
        for(long i = 0; i < this.size-1; i++) {
            assertNotNull(iterator.getHeader());
            assertNotNull(iterator.getDigest());
            assertNotNull(iterator.getTxn());

            assertTrue(iterator.next());
            assertTrue(iterator2.next());
        }
        assertFalse(iterator.next());
        assertFalse(iterator2.next());

        // close resource
        iterator.close();
        iterator2.close();
    }

}
