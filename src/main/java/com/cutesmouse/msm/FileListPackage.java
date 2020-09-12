package com.cutesmouse.msm;

import java.io.File;

public class FileListPackage {
    public final File f;
    public FileListPackage(File f) {
        this.f = f;
    }

    @Override
    public String toString() {
        return f.getName();
    }
}
