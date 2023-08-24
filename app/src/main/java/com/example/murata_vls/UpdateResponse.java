package com.example.murata_vls;

public class UpdateResponse {
    private boolean success;
    private String ref_no;
    private int camera_flag;
    public boolean isSuccess() {
        return success;
    }
    public String getRef_no() {
        return ref_no;
    }
    public int getCameraFlag(){
        return camera_flag;}
}
