package com.example.helloworldgrpc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DownloadActivityTest {
    DownloadActivity.DownloadTask downloadTask;
    DownloadActivity.ListingTask listingTask;
    DownloadActivity activity;

    @Before
    public void initiate() {
        activity = new DownloadActivity();
        downloadTask = new DownloadActivity().new DownloadTask();
        listingTask = new DownloadActivity().new ListingTask();
    }

    @Test
    public void testListingEmptyRequest() {
        Boolean result = listingTask.doInBackground("172.132.45.171", "50056");
        assertFalse(result);
    }

    @Test
    public void testListingValidRequestAndDataReturned() {
        Boolean result = listingTask.doInBackground("172.132.45.176", "50051");

//        assertTrue(result);
        assertFalse(DownloadActivity.nameList.isEmpty());
    }//1

    @Test
    public void testDownloadValidRequestAndDownload() {
        Boolean result1 = listingTask.doInBackground("172.132.45.176", "50051");
        Boolean result = downloadTask.doInBackground("172.132.45.176", "50051", "Demo.csv", "0");
        assertTrue(result);
//        assertTrue(DownloadActivity.nameList.isEmpty());
    }//11

    @Test
    public void testListingValidRequestAndDataEmpty() {
        Boolean result = listingTask.doInBackground("172.132.45.176", "50051");

        assertFalse(result && DownloadActivity.nameList.isEmpty());
//        assertTrue(DownloadActivity.nameList.isEmpty());
    }//2

//    @Test
//    public void testIfListingListIsEmpty() {
//        Boolean result = listingTask.doInBackground("172.132.45.176", "50051");
//        assertTrue(DownloadActivity.nameList.isEmpty());
//    }

//    @Test
//    public void testIfDownloadListIsNotEmpty() {
//        Boolean result = listingTask.doInBackground("172.132.45.176", "50051");
//        assertFalse(DownloadActivity.nameList.isEmpty());
//    }

//    @Test
//    public void testEmptyFileList() {
//        DownloadActivity activity = new DownloadActivity();
//        assertFalse(activity.nameList.isEmpty());
//    }

//    @Test
//    public void testUIUpdate() {
//        // Assuming CustomAdapter and ListView are properly set up in DownloadActivity
//        DownloadActivity activity = new DownloadActivity();
////        activity.runOnUiThread(() -> {
////        });
//        activity.nameList.add("File1");
//        activity.nameStatus.add(0);
//        activity.adapter.notifyDataSetChanged();
//        assertFalse(activity.nameList.isEmpty());
//    }


    @Test
    public void testDownloadEmptyRequest() {
        Boolean result = downloadTask.doInBackground("172.132.45.171", "50056", "Demo.csv", "0");
        assertFalse(result);
    }//10


//    @Test
//    public void testIfDownloadListIsCompleted() {
//        DownloadActivity.ListingTask listingTask = new DownloadActivity().new ListingTask();
//        Boolean result = listingTask.doInBackground("172.132.45.176", "50051");
//        assertTrue(DownloadActivity.nameList.isEmpty());
//    }

//    @Test
//    public void isListviewEmpty(){
//        assertNotNull(activity.listView);
//    }
}
