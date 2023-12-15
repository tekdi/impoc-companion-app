package com.example.helloworldgrpc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.helloworldgrpc.adapter.CustomAdapter;
import com.example.helloworldgrpc.dto.FilesContainer;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class UploadActivity extends AppCompatActivity {
    private final int STORAGE_PERMISSION_CODE = 200;
    long fileSize = 0;
    File file;
    ManagedChannel channel = null;
//    String serverIp = "172.132.45.171";
    String serverIp = "65.2.123.74";
//    int portToConnect = 50051;
    int portToConnect = 50052;
    ProgressBar pbProgress;
    List<String> alFilesToUploadList = new ArrayList<>();
    List<Integer> alUploadedFilesStatus = new ArrayList<>();
    List<FilesContainer> alFilesContainer = new ArrayList<>();
    ListView rcvFiles;
    CustomAdapter adapter;
    TextView frameEmpty;

    String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/DownloadedFromMetricsLocal";
    String folderMovePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/UploadedFiles";
    File[] listOfFiles;
    Button btnUpload;

    public static long getFileSize(File file) {
        if (file.exists()) {
            return file.length();
        } else {
            return 0;
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_files);

        pbProgress = findViewById(R.id.pb_progress);
        rcvFiles = findViewById(R.id.rcv_files);
        frameEmpty = findViewById(R.id.frame_empty);
        btnUpload = findViewById(R.id.btUpload);

        this.setTitle("Grpc Upload");

        channel = ManagedChannelBuilder.forAddress(serverIp, portToConnect)
                .usePlaintext()
                .build();

        if (isStoragePermissionGranted()) {
            Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkForServerConnection() {
        try {
            FileServiceGrpc.FileServiceBlockingStub stub = FileServiceGrpc.newBlockingStub(channel);
            HelloRequestMetric request = HelloRequestMetric.newBuilder().setName("Hi").build();
            HelloReplyMetric reply = stub.sayHelloMetric(request);

            if (!reply.getMessage().isEmpty()) {
                Toast.makeText(this, "Server is Up " + reply.getMessage(), Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Please check the server", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void checkForUploads(View view) {
        if (channel.isShutdown()) {
            channel = ManagedChannelBuilder.forAddress(serverIp, portToConnect)
                    .usePlaintext()
                    .build();
        }
        pbProgress.setVisibility(View.VISIBLE);
        boolean serverConnection = checkForServerConnection();

        do {
            if (serverConnection) {
                Toast.makeText(this, "Channel Ready", Toast.LENGTH_SHORT).show();
                if (!channel.isTerminated() && !channel.isShutdown()) {
                    pbProgress.setVisibility(View.GONE);
                    listFilesToUI();
                    break;
                }
            } else {
                Toast.makeText(this, "Server is down", Toast.LENGTH_SHORT).show();
            }
            Log.d("Server Status", String.valueOf(serverConnection));
            Handler handler = new Handler();
            handler.postDelayed(this::checkForServerConnection, 2000);

        } while (true);


    }

    private boolean isStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void listFilesToUI() {

        File directory = new File(folderPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(this, "Download folder creation failed", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        File directoryUploaded = new File(folderMovePath);
        if (!directoryUploaded.exists()) {
            if (!directoryUploaded.mkdirs()) {
                Toast.makeText(this, "Upload folder creation failed", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        listOfFiles = directory.listFiles();
        alFilesToUploadList.clear();
        alUploadedFilesStatus.clear();
        if (listOfFiles.length > 0) {

            rcvFiles.setVisibility(View.VISIBLE);
            frameEmpty.setVisibility(View.GONE);
            btnUpload.setEnabled(true);

            for (int j = 0; j <= listOfFiles.length - 1; j++) {
                alFilesToUploadList.add(listOfFiles[j].getName());
                alUploadedFilesStatus.add(0);
            }

            adapter = new CustomAdapter(this, alFilesToUploadList, alUploadedFilesStatus);
            rcvFiles.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            frameEmpty.setVisibility(View.VISIBLE);
            rcvFiles.setVisibility(View.GONE);
            btnUpload.setEnabled(false);
        }
    }

    public void filesLooper(View view) {

        for (int i = 0; i <= listOfFiles.length - 1; i++) {
            pbProgress.setVisibility(View.VISIBLE);

            file = new File(folderPath + "/" + listOfFiles[i].getName());

            fileSize = getFileSize(file);

            FilesContainer fileContainer = new FilesContainer(
                    file,
                    fileSize,
                    i
            );

            alFilesContainer.add(fileContainer);

            Log.d("GRPC_TEST", "Returned file size local " + fileSize + " Filename " + listOfFiles[i].getName());

            fileUploadClient(file, i == listOfFiles.length - 1);

        }

    }

    public void fileUploadClient(File fileInput, boolean stopProgressBar) {

        FileServiceGrpc.FileServiceStub stub = FileServiceGrpc.newStub(channel);
        StreamObserver<ChunkMetric> requestObserver = stub.uploadFileMetric(new StreamObserver<ReplyMetric>() {
            @Override
            public void onNext(ReplyMetric value) {
                if (!value.getMessage().isEmpty()) {
                    System.out.println("GRPC_TEST File upload Data " + value.getMessage());
                    runOnUiThread(() -> {
                        for (int i = 0; i <= alFilesContainer.size() - 1; i++) {
                            if (Long.parseLong(value.getMessage()) == alFilesContainer.get(i).getFileSize()
                                    && fileInput.getName().equals(alFilesContainer.get(i).getFile().getName())) {
                                deleteFile(alFilesContainer.get(i).getFile(), i);
                                break;
                            }
                        }
                    });
                } else {
                    System.out.println("GRPC_TEST File upload failed");
                }
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> pbProgress.setVisibility(View.GONE));
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                runOnUiThread(() -> {
                    if (stopProgressBar) {
                        pbProgress.setVisibility(View.GONE);
                    }
                });
                if (stopProgressBar) {
                    channel.shutdown();
                }
            }
        });

        String filename = fileInput.getName();
        byte[] byteConvertedFilename = filename.getBytes(StandardCharsets.UTF_8);

        ChunkMetric firstChunk = ChunkMetric.newBuilder()
                .setBuffer(ByteString.copyFrom(byteConvertedFilename))
                .build();
        requestObserver.onNext(firstChunk);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                ChunkMetric chunk = ChunkMetric.newBuilder()
                        .setBuffer(ByteString.copyFrom(buffer, 0, bytesRead))
                        .build();
                requestObserver.onNext(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        requestObserver.onCompleted();

    }

    public void copyFile(File sourceFile, File destFile, int i) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            if (sourceFile.delete()) {
                Log.d("GRPC_TEST", "File deleted successfully " + sourceFile.getName());
            } else {
                Log.d("GRPC_TEST", "Problem in File delete " + sourceFile.getName());
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public void deleteFile(File file, int i) {
        if (file.exists()) {
            String folderMovePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/UploadedFiles";

            File fileToMove = new File(folderMovePath + "/" + file.getName());
            try {
                alUploadedFilesStatus.add(alFilesContainer.get(i).getIndexToUpdate(), 1);
                adapter.notifyDataSetChanged();
                copyFile(file, fileToMove, i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}