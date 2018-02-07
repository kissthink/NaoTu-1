package com.ks.naotu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import droidninja.filepicker.utils.Orientation;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, OnJsInterfaceCallback {

    JsToJava jsToJava = new JsToJava();
    private static final int RC_FILE_PICKER_PERM = 321;
    @BindView(R.id.vweb)
    WebView vweb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        vweb.loadUrl("file:///android_asset/jsmind/index.html");
        WebSettings settings = vweb.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setAppCacheEnabled(true);
        settings.setBuiltInZoomControls(false);
        jsToJava.setListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_mind:
                onPickDoc();
                break;
            case R.id.action_load_test:
                doOpenMindTest();
                break;
            case R.id.action_save_img:
                doSaveImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doOpenMindTest(){
        vweb.loadUrl("javascript:open_json()");
    }

    public void doSaveImage(){
        vweb.loadUrl("javascript:screen_shot()");
    }

    @AfterPermissionGranted(RC_FILE_PICKER_PERM)
    public void pickDocClicked(View view) {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            onPickDoc();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_doc_picker),
                    RC_FILE_PICKER_PERM,
                    FilePickerConst.PERMISSIONS_FILE_PICKER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String path = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS).get(0);
                    doOpenMind(path);
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
                    try {
                        Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
                        String path = UriUtils.getImageAbsolutePath(this, uri);
                        doOpenMind(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void doOpenMind(String path) {
        vweb.loadUrl("javascript:open_loacl('" + path + "')");
    }

    public void onPickDoc() {
        String[] jmind = {".jm"};
        String[] txt = {".txt"};
        String[] kmind = {".km"};
        String[] md = {".md"};
        String[] fmind = {".mm"};
        String[] xmind = {".xmind"};
        String[] mmap = {".mmap"};
        FilePickerBuilder.getInstance()
                .setMaxCount(1)
                .showFolderView(true)
//                .setSelectedFiles(docPaths)
                .setActivityTheme(R.style.FilePickerTheme)
                .addFileSupport("JsMind", jmind)
                .addFileSupport("大纲文本", txt)
                .addFileSupport("Markdown", md)
                .addFileSupport("Freemind", fmind)
                .addFileSupport("KityMinder", kmind)
                .addFileSupport("XMind", xmind)
                .addFileSupport("MindManger", mmap)
                .enableDocSupport(false)
                .sortDocumentsBy(SortingTypes.name)
                .withOrientation(Orientation.UNSPECIFIED)
                .pickFile(this);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_FILE_PICKER_PERM) {
            onPickDoc();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        vweb.addJavascriptInterface(jsToJava, "jsmind");
    }

    @Override
    protected void onStop() {
        super.onStop();
        vweb.removeJavascriptInterface("jsmind");
    }

    @Override
    public void onDocumentReady() {

    }

    @Override
    public void onGetHtml(String html) {

    }

    @Override
    public void onSave(String html, String text, String bodyHtml, String headHtml) {

    }

    @Override
    public void onLog(String html) {

    }

    @Override
    public void onBoldChanged(boolean isBold) {

    }

    @Override
    public void onEditorImageClick(String img) {

    }

    @Override
    public void onViewImageClick(int postion, String imgs) {

    }

    @Override
    public void onViewMp3Click(String guid) {

    }

    @Override
    public void onViewPDFClick(String guid) {

    }

    @Override
    public void onToast(String msg) {

    }

    @Override
    public void onNoteReading(String text) {

    }

    @Override
    public void onDocumentTextReady(String text) {

    }

    @Override
    public void onHtml2Image(String text) {

    }

    @Override
    public void onHtml2Image(String noteid, boolean tocat) {

    }

    @Override
    public void onHtml2PDF(String noteid) {

    }
}
