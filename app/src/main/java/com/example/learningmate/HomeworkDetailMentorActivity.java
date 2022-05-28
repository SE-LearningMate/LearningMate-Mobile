package com.example.learningmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeworkDetailMentorActivity extends AppCompatActivity {

    private TextView assignmentTitle;
    private TextView dueDate;
    private TextView assignmentContent;
    private TextView postedFileName;
    private TextView submittedFileName;
    private Homework homework;
    private File file;
    private long downloadId;
    private DownloadManager downloadManager;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.arg1 == 1) {
                assignmentTitle.setText(homework.getTitle());
                dueDate.setText(homework.getDueDate());
                assignmentContent.setText(homework.getBody());
                postedFileName.setText(file.getFileName());
                if (homework.getSubmitId() == null || homework.getSubmitId().equals("null")) {
                    submittedFileName.setText("");
                }
                postedFileName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), TestWebViewActivity.class);
                        intent.putExtra("url", file.getFileUrl());
                        startActivity(intent);
                    }
                });
                postedFileName.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Uri download = Uri.parse(file.getFileUrl());
                        DownloadManager.Request request = new DownloadManager.Request(download);
                        request.setTitle(file.getFileName());
                        request.setDescription("파일 다운로드");
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setAllowedOverRoaming(true);
                        request.setAllowedOverMetered(true);

                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getFileName());
                        }
                        downloadId = downloadManager.enqueue(request);

                        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                        registerReceiver(downloadReceiver, intentFilter);
                        return true;
                    }
                });
            }
        }
    };
    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Toast.makeText(HomeworkDetailMentorActivity.this, "다운로드 성공", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_detail_mentor);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        assignmentTitle = findViewById(R.id.detail_homework_mentor_name_tv);
        dueDate = findViewById(R.id.detail_homework_mentor_duration_tv);
        assignmentContent = findViewById(R.id.detail_homework_mentor_content_tv);
        postedFileName = findViewById(R.id.detail_mentor_content_file_name_tv);
        submittedFileName = findViewById(R.id.detail_homework_mentor_file_tv);
        Intent intent = getIntent();
        if (intent != null) {
            homework = (Homework) intent.getSerializableExtra("data");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getFileInfo();
                }
            }).start();
        }

    }

    public void getFileInfo() {
        try {
            String url = "http://windry.dothome.co.kr/se_learning_mate/controller/file_controller.php";
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("mentor_uid", User.currentUser.getUserId())
                    .add("file_id", homework.getFileId() + "")
                    .add("category", "0")
                    .build();

            Request request = new Request
                    .Builder()
                    .url(url)
                    .post(formBody).build();

            Response response = client
                    .newCall(request)
                    .execute();

            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    String data = body.string();
                    if (data.equals("Not Found File Data")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HomeworkDetailMentorActivity.this, "파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    } else {
                        JSONObject jsonObject = new JSONObject(data);
                        file = new File(
                                jsonObject.get("file_id").toString(),
                                jsonObject.get("file_uploader").toString(),
                                Integer.parseInt(jsonObject.get("file_category").toString()),
                                jsonObject.get("upload_date").toString(),
                                jsonObject.get("file_name").toString(),
                                jsonObject.get("file_hash_name").toString(),
                                jsonObject.get("file_url").toString(),
                                jsonObject.get("file_size").toString()
                        );
                        Message message = handler.obtainMessage();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    }

                    Log.d("data", data);
                }
            } else {
                Log.d("response", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}