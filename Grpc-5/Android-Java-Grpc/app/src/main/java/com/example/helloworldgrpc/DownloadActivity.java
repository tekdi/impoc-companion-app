package com.example.helloworldgrpc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.helloworldgrpc.adapter.CustomAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DownloadActivity extends AppCompatActivity {
    static List<String> nameList = new ArrayList<>();
    static List<Integer> nameStatus = new ArrayList<>();
    public ListView listView;
    CustomAdapter adapter;
    private EditText hostEdit;
    private EditText portEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        hostEdit = (EditText) findViewById(R.id.host);
        portEdit = (EditText) findViewById(R.id.port);
        adapter = new CustomAdapter(this, nameList, nameStatus);
        listView.setAdapter(adapter);
    }

    public void sendGrpcMessage(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        new ListingTask()
                .execute(
                        hostEdit.getText().toString(),
                        portEdit.getText().toString());

        Log.d("InBackground", "host");

    }

    public void downloadGrpcMessage(View view) {
        if (!nameList.isEmpty()) {
            startDownload();
        } else {
            Toast.makeText(this, "No any file to download", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDownload() {
        for (int i = 0; i < nameList.size(); i++) {
            new DownloadTask()
                    .execute(
                            hostEdit.getText().toString(),
                            portEdit.getText().toString(),
                            nameList.get(i),
                            String.valueOf(i));
        }
    }

    public void goToUpload(View view) {
        startActivity(new Intent(this, UploadActivity.class));
    }

    public static class FileCreator {
        public static boolean createFile(String folderPath, String fileName, Iterator<Chunk> fileContent) {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return false;
                }
            }

            File file = new File(folder, fileName);

            try {
                if (file.createNewFile()) {

                    FileOutputStream fos = new FileOutputStream(file);
                    Chunk chunk = fileContent.next();
                    byte[] data = chunk.getBuffer().toByteArray();
                    fos.write(data);
                    fos.close();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private class ListingTask extends AsyncTask<String, Void, FileList> {
        public ListingTask() {
        }

        @Override
        protected FileList doInBackground(String... params) {

            try {
                nameList.clear();
                nameStatus.clear();
                String host = params[0];
                String port = params[1];
                ManagedChannel channel = ManagedChannelBuilder.forAddress(host, Integer.parseInt(port)).usePlaintext().build();
                GreeterGrpc.GreeterBlockingStub greeterBlockingStub = GreeterGrpc.newBlockingStub(channel);

                EmptyRequest request = EmptyRequest.newBuilder().build();

                return greeterBlockingStub.getFilesToDownload(request);
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Log.d("Listing error", "" + e);
                pw.flush();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FileList data) {
            super.onPostExecute(data);
            if (data != null) {
                for (int i = 0; i < data.getFileNameCount(); i++) {
                    nameList.add(data.getFileName(i));
                    nameStatus.add(0);
                    Log.d("Listing Item", data.getFileName(i));
                }
                if (nameList != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, Iterator<Chunk>> {

        private ManagedChannel channel;

        public DownloadTask() {
        }

        @Override
        protected Iterator<Chunk> doInBackground(String... params) {


            String host = params[0];
            String port = params[1];
            String name = params[2];
            int index = Integer.parseInt(params[3]);
            try {
                Log.d("InBackground", host);
                channel = ManagedChannelBuilder.forAddress(host, Integer.parseInt(port)).usePlaintext().build();
                GreeterGrpc.GreeterBlockingStub greeterBlockingStub = GreeterGrpc.newBlockingStub(channel);
                FileRequest fileRequest = FileRequest.newBuilder().setFilename(name).build();
                Iterator<Chunk> reply = greeterBlockingStub.downloadFile(fileRequest);

                String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/DownloadedFromMetricsLocal";
                boolean success = FileCreator.createFile(folderPath, name, reply);
                if (success) {
                    runOnUiThread(() -> {
                        channel.shutdown();
                        nameStatus.add(index, 1);
                        if (nameList.size() - 1 == index) {
                            nameList.clear();
                            nameStatus.clear();
                        }
                        adapter.notifyDataSetChanged();
                    });
                } else {
                    Log.d("Download Item", "Unable to create file");
                }
                Log.d("Download Item", "status - " + success);
                return reply;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Iterator<Chunk> data) {
            super.onPostExecute(data);
            if (data != null)
                Log.d("Download Item", data.toString());
            else
                Log.d("Download Item", "null");
        }
    }
}
