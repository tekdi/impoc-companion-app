package com.example.helloworldgrpc.dto;

import java.io.File;

public class FilesContainer {
    private File file;
    private long fileSize;
    private int indexToUpdate;

    public FilesContainer(File file, long fileSize, int indexToUpdate) {
        this.file = file;
        this.fileSize = fileSize;
        this.indexToUpdate = indexToUpdate;
    }


    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getIndexToUpdate() {
        return indexToUpdate;
    }

    public void setIndexToUpdate(int indexToUpdate) {
        this.indexToUpdate = indexToUpdate;
    }
}
