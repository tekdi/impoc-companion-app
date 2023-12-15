package com.example.helloworldgrpc;

import static org.junit.Assert.assertTrue;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class UploadActivityTest {

    UploadActivity uploadActivity;

    @Before
    public void initiate() {
        uploadActivity = new UploadActivity();
    }

    @Test
    private void checkForServerConnection() {
        assertTrue(uploadActivity.checkForServerConnection());
    }
}